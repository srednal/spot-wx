package com.srednal.spotwx;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.lang.Math.round;

public class WeatherReport {

  private static final Logger logger = LogManager.getLogger();

  // Weather condition codes, per open-meteo docs(abbreviated for shorter output)
  private static final Map<Integer, String> WX_CODES;

  static {
    WX_CODES = new HashMap<>();
    WX_CODES.put(0, "Clear");
    WX_CODES.put(1, "Clear");
    WX_CODES.put(2, "Pt Cloudy");
    WX_CODES.put(3, "Overcast");
    WX_CODES.put(45, "Fog");
    WX_CODES.put(48, "Fog");
    WX_CODES.put(51, "Lt Drizzle");
    WX_CODES.put(53, "Drizzle");
    WX_CODES.put(55, "Hvy Drizzle");
    WX_CODES.put(56, "Lt Frz Drizzle");
    WX_CODES.put(57, "Hvy Frz Drizzle");
    WX_CODES.put(61, "Lt Rain");
    WX_CODES.put(63, "Rain");
    WX_CODES.put(65, "Hvy Rain");
    WX_CODES.put(66, "Lt Frz Rain");
    WX_CODES.put(67, "Frz Rain");
    WX_CODES.put(71, "Lt Snow");
    WX_CODES.put(73, "Snow");
    WX_CODES.put(75, "Hvy Snow");
    WX_CODES.put(77, "Snow Grains");
    WX_CODES.put(80, "Lt Showers");
    WX_CODES.put(81, "Showers");
    WX_CODES.put(82, "Hvy Showers");
    WX_CODES.put(85, "Lt Snow Showers");
    WX_CODES.put(86, "Snow Showers");
    WX_CODES.put(95, "T-storm");
    WX_CODES.put(96, "T-storm");
    WX_CODES.put(99, "T-storm");
  }

  private static final String[] DIRECTIONS = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};

  private static final long TOP_OF_HOUR_SEC = 15 * 60;

  public static String wxCondition(int code) {
    return WX_CODES.getOrDefault(code, String.valueOf(code));
  }

  public static String direction(float degrees) {
    int l = round(degrees * DIRECTIONS.length / 360) % DIRECTIONS.length;
    return DIRECTIONS[l];
  }

  private final List<Hourly> hourlies;
  private final List<Daily> dailies;

  public WeatherReport(WeatherJson json) {
    hourlies = Hourly.getReports(json);
    dailies = Daily.getReports(json);
  }

  private <A> String concatWithNewline(List<A> l) {
    return l.stream().map(A::toString).reduce((a, b) -> a + "\n" + b).orElse("Unavailable");
  }

  public String hourlyReport() {
    return concatWithNewline(hourlies.subList(0, 2));
  }

  public String dailyReport() {
    return concatWithNewline(dailies.subList(0, 2));
  }

  private record Hourly(LocalDateTime time, int weatherCode, float temperature, float precipitation,
                        int precipitationProbability, int windDirection, float windSpeed) {

    static List<Hourly> getReports(WeatherJson json) {
      WeatherJson.Hourly h = json.getHourly();
      List<Hourly> reports = new ArrayList<>();

      // hourly reports start at the current hour, truncated
      // if it's past the 'top of the hour', then the first report
      // is 'old news' and mostly unhelpful, so skip it
      int firstReport = 0;
      try {
        Instant reportBaseTime = h.getInstant(0);
        // for reportBaseTime = 13:00 (report timezone) => add 15 min => 13:15
        // if now is 13:20 => isAfter 13:15 => skip first report
        if (Instant.now().isAfter(reportBaseTime.plusSeconds(TOP_OF_HOUR_SEC)) && h.getLength() > 1) {
          firstReport = 1;
        }
      } catch (DateTimeException e) {
        // just go with what we got
        logger.error("Unable to parse time {} {}", h.getTime(0), json.getTimezone(), e);
      }

      for (int i = firstReport; i < h.getLength(); i++) {
        reports.add(new Hourly(
            h.getDateTime(i),
            h.getWeather_code(i),
            h.getTemperature_2m(i),
            h.getPrecipitation(i),
            h.getPrecipitation_probability(i),
            h.getWind_direction_10m(i),
            h.getWind_speed_10m(i)));
      }
      return reports;
    }

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("MM/dd HH:mm");

    @Override
    public String toString() {
      return "%s: %s | %.0fF | %.1f\" (%d%%) | %s@%.0fmph".formatted(
          TIME_FMT.format(time),
          WeatherReport.wxCondition(weatherCode),
          temperature,
          precipitation,
          precipitationProbability,
          WeatherReport.direction(windDirection),
          windSpeed
      );
    }
  }

  private record Daily(LocalDate date, int weatherCode, float temperatureMax, float temperatureMin, float precipitation,
                       int precipitationProbability, int windDirection, float windSpeed) {
    static List<Daily> getReports(WeatherJson json) {
      WeatherJson.Daily d = json.getDaily();
      List<Daily> reports = new ArrayList<>();
      for (int i = 0; i < d.getLength(); i++) {
        reports.add(new Daily(
            d.getDate(i),
            d.getWeather_code(i),
            d.getTemperature_2m_max(i),
            d.getTemperature_2m_min(i),
            d.getPrecipitation_sum(i),
            d.getPrecipitation_probability_max(i),
            d.getWind_direction_10m_dominant(i),
            d.getWind_speed_10m_max(i)));
      }
      return reports;
    }

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM/dd");

    @Override
    public String toString() {
      //   10/19: Fog | 59/31F | 0" (5%) | E@10mph
      //   10/20: Overcast | 64/37F | 0" (5%) | S@14mph
      // Large/long output ~124 chars (under 140 limit)
      //   10/19: Hvy Frz Drizzle | 111/-99F | 10.9" (100%) | SE@110mph
      //   10/20: Lt Snow Showers | 111/-99F | 10.0" (100%) | SW@114mph
      return "%s: %s | %.0f/%.0fF | %.1f\" (%d%%) | %s@%.0fmph".formatted(
          DATE_FMT.format(date),
          WeatherReport.wxCondition(weatherCode),
          temperatureMax,
          temperatureMin,
          precipitation,
          precipitationProbability,
          WeatherReport.direction(windDirection),
          windSpeed);
    }
  }
}
package com.srednal.spotwx;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.iakovlev.timeshape.TimeZoneEngine;
import org.apache.http.client.utils.URIBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.ZoneId;

public class WeatherQuery {

  private static final Logger logger = LogManager.getLogger();

  private static final TimeZoneEngine zoneEngine = TimeZoneEngine.initialize();

  // i.e. "America/Denver"
  private static ZoneId getTimeZone(Position pos) {
    return zoneEngine.query(pos.latitude, pos.longitude).orElseGet(() -> {
      logger.error("Unable to find TimeZone for position {} - using system tz", pos);
      return ZoneId.systemDefault();
    });
  }

  public static WeatherReport getWeatherReport(Position pos) throws IOException {
    // retrieve weather for the next 2 days and the next 3 hours
    URL url;
    try {
      url = new URIBuilder()
          .setScheme("https")
          .setHost("api.open-meteo.com")
          .setPath("v1/forecast")
          .addParameter("latitude", String.valueOf(pos.latitude))
          .addParameter("longitude", String.valueOf(pos.longitude))
          .addParameter("temperature_unit", "fahrenheit")
          .addParameter("lwind_speed_unit", "mph")
          .addParameter("lprecipitation_unit", "inch")
          .addParameter("timezone", String.valueOf(getTimeZone(pos)))
          .addParameter("forecast_days", "2")
          .addParameter("daily", "weather_code")
          .addParameter("daily", "temperature_2m_max")
          .addParameter("daily", "temperature_2m_min")
          .addParameter("daily", "precipitation_sum")
          .addParameter("daily", "precipitation_probability_max")
          .addParameter("daily", "wind_direction_10m_dominant")
          .addParameter("daily", "wind_speed_10m_max")
          .addParameter("forecast_hours", "3")
          .addParameter("hourly", "weather_code")
          .addParameter("hourly", "temperature_2m")
          .addParameter("hourly", "precipitation")
          .addParameter("hourly", "precipitation_probability")
          .addParameter("hourly", "wind_direction_10m")
          .addParameter("hourly", "wind_speed_10m")
          .build().toURL();
    } catch (URISyntaxException | MalformedURLException e) {
      logger.fatal("Error building URI (should not happen!)", e);
      throw new RuntimeException(e);
    }

    logger.info("Fetching weather from {}", url);

    try (InputStream is = url.openStream()) {
      ObjectMapper mapper = new ObjectMapper()
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

      WeatherJson response = mapper.readValue(is, WeatherJson.class);

      return new WeatherReport(response);
    }
  }

  /**
   * Fetch the weather reports etc and print
   */
  public static void main(String[] args) {
    try {
      Position pos = new Position(40.3746, -105.5231); // Estes Park
//      Position pos = new Position(35.733224, -78.872085); // Apex, NC
//      Position pos = new Position(51.523479, -0.128771); // London
      System.out.println(getTimeZone(pos));
      System.out.println(pos);
      System.out.println("---");
      WeatherReport wr = WeatherQuery.getWeatherReport(pos);
      System.out.println(wr.hourlyReport());
      System.out.println("---");
      System.out.println(wr.dailyReport());
    } catch (Exception e) {
      System.err.println(e);
    }
  }
}
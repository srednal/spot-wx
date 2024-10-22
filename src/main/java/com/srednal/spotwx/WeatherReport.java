package com.srednal.spotwx;

import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.round;

public class WeatherReport {

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

  public WeatherReport(String time, int weather_code, float temperature_2m_max, float temperature_2m_min, float precipitation_sum, int precipitation_probability_max, int wind_direction_10m_dominant, float wind_speed_10m_max) {
    this.time = time;
    this.weather_code = weather_code;
    this.temperature_2m_max = temperature_2m_max;
    this.temperature_2m_min = temperature_2m_min;
    this.precipitation_sum = precipitation_sum;
    this.precipitation_probability_max = precipitation_probability_max;
    this.wind_direction_10m_dominant = wind_direction_10m_dominant;
    this.wind_speed_10m_max = wind_speed_10m_max;
  }

  public static String wxCondition(int code) {
    return WX_CODES.getOrDefault(code, String.valueOf(code));
  }

  private static final String[] DIRECTIONS = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};

  public static String windDirection(float degrees) {
    int l = round(degrees * DIRECTIONS.length / 360) % DIRECTIONS.length;
    return DIRECTIONS[l];
  }

  public String time;
  public int weather_code;
  public float temperature_2m_max;
  public float temperature_2m_min;
  public float precipitation_sum;
  public int precipitation_probability_max;
  public int wind_direction_10m_dominant;
  public float wind_speed_10m_max;

  public String getDay() {
    // 2024-10-20
    return time.substring(5).replace('-', '/');
  }

  public String getWeather() {
    return wxCondition(weather_code);
  }

  public String getTempRange() {
    return "%.0f/%.0fF".formatted(temperature_2m_max, temperature_2m_min);
  }

  public String getPrecipitation() {
    return "%.1f\"".formatted(precipitation_sum);
  }

  public String getPrecipitationProbability() {
    return "%d%%".formatted(precipitation_probability_max);
  }

  public String getWindDirection() {
    return windDirection(wind_direction_10m_dominant);
  }

  public String getWindSpeed() {
    return "%.0fmph".formatted(wind_speed_10m_max);
  }

//   10/19: Fog | 59/31F | 0" (5%) | E@10mph
//   10/20: Overcast | 64/37F | 0" (5%) | S@14mph
// Large/long output ~124 chars (under 140 limit)
//   10/19: Hvy Frz Drizzle | 111/-99F | 10.9" (100%) | SE@110mph
//   10/20: Lt Snow Showers | 111/-99F | 10.0" (100%) | SW@114mph
  @Override
  public String toString() {
    return "%s: %s | %s | %s (%s) | %s@%s".formatted(getDay(), getWeather(), getTempRange(), getPrecipitation(), getPrecipitationProbability(), getWindDirection(), getWindSpeed());
  }
}

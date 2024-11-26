package com.srednal.spotwx;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.DateTimeException;
import java.time.ZoneId;

/**
 * Model for json response from Weather
 */
public class WeatherJson {

  private String latitude;
  private String longitude;
  private float elevation; // meters
  private String timezone;
  private Hourly hourly;
  private Daily daily;

  public String getLatitude() {
    return latitude;
  }

  public void setLatitude(String latitude) {
    this.latitude = latitude;
  }

  public String getLongitude() {
    return longitude;
  }

  public void setLongitude(String longitude) {
    this.longitude = longitude;
  }

  public float getElevation() {
    return elevation;
  }

  public void setElevation(float elevation) {
    this.elevation = elevation;
  }

  public String getTimezone() {
    return timezone;
  }

  private ZoneId zoneId = null;

  public ZoneId getZoneId() throws DateTimeException {
    if (zoneId == null) {
      zoneId = ZoneId.of(timezone);
    }
    return zoneId;
  }

  public void setTimezone(String timezone) {
    this.timezone = timezone;
  }

  public Hourly getHourly() {
    return hourly;
  }

  public void setHourly(Hourly hourly) {
    this.hourly = hourly;
  }

  public Daily getDaily() {
    return daily;
  }

  public void setDaily(Daily daily) {
    this.daily = daily;
  }

  public static class Hourly {
    private String[] time;
    private int[] weather_code;
    private float[] temperature_2m;
    private float[] precipitation;
    private int[] precipitation_probability;
    private int[] wind_direction_10m;
    private float[] wind_speed_10m;

    public int getLength() {
      return time.length;
    }

    public String[] getTime() {
      return time;
    }

    public String getTime(int i) {
      return time[i];
    }

    public void setTime(String[] time) {
      this.time = time;
    }

    public int[] getWeather_code() {
      return weather_code;
    }

    public int getWeather_code(int i) {
      return weather_code[i];
    }

    public void setWeather_code(int[] weather_code) {
      this.weather_code = weather_code;
    }

    public float[] getTemperature_2m() {
      return temperature_2m;
    }

    public float getTemperature_2m(int i) {
      return temperature_2m[i];
    }

    public void setTemperature_2m_max(float[] temperature_2m) {
      this.temperature_2m = temperature_2m;
    }

    public float[] getPrecipitation() {
      return precipitation;
    }

    public float getPrecipitation(int i) {
      return precipitation[i];
    }

    public void setPrecipitation_sum(float[] precipitation) {
      this.precipitation = precipitation;
    }

    public int[] getPrecipitation_probability() {
      return precipitation_probability;
    }

    public int getPrecipitation_probability(int i) {
      return precipitation_probability[i];
    }

    public void setPrecipitation_probability(int[] precipitation_probability) {
      this.precipitation_probability = precipitation_probability;
    }

    public int[] getWind_direction_10m() {
      return wind_direction_10m;
    }

    public int getWind_direction_10m(int i) {
      return wind_direction_10m[i];
    }

    public void setWind_direction_10m_dominant(int[] wind_direction_10m) {
      this.wind_direction_10m = wind_direction_10m;
    }

    public float[] getWind_speed_10m() {
      return wind_speed_10m;
    }

    public float getWind_speed_10m(int i) {
      return wind_speed_10m[i];
    }

    public void setWind_speed_10m_max(float[] wind_speed_10m) {
      this.wind_speed_10m = wind_speed_10m;
    }
  }

  public static class Daily {
    private String[] time;
    private int[] weather_code;
    private float[] temperature_2m_max;
    private float[] temperature_2m_min;
    private float[] precipitation_sum;
    private int[] precipitation_probability_max;
    private int[] wind_direction_10m_dominant;
    private float[] wind_speed_10m_max;

    public int getLength() {
      return time.length;
    }

    public String[] getTime() {
      return time;
    }

    public String getTime(int i) {
      return time[i];
    }

    public void setTime(String[] time) {
      this.time = time;
    }

    public int[] getWeather_code() {
      return weather_code;
    }

    public int getWeather_code(int i) {
      return weather_code[i];
    }

    public void setWeather_code(int[] weather_code) {
      this.weather_code = weather_code;
    }

    public float[] getTemperature_2m_max() {
      return temperature_2m_max;
    }

    public float getTemperature_2m_max(int i) {
      return temperature_2m_max[i];
    }

    public void setTemperature_2m_max(float[] temperature_2m_max) {
      this.temperature_2m_max = temperature_2m_max;
    }

    public float[] getTemperature_2m_min() {
      return temperature_2m_min;
    }

    public float getTemperature_2m_min(int i) {
      return temperature_2m_min[i];
    }

    public void setTemperature_2m_min(float[] temperature_2m_min) {
      this.temperature_2m_min = temperature_2m_min;
    }

    public float[] getPrecipitation_sum() {
      return precipitation_sum;
    }

    public float getPrecipitation_sum(int i) {
      return precipitation_sum[i];
    }

    public void setPrecipitation_sum(float[] precipitation_sum) {
      this.precipitation_sum = precipitation_sum;
    }

    public int[] getPrecipitation_probability_max() {
      return precipitation_probability_max;
    }

    public int getPrecipitation_probability_max(int i) {
      return precipitation_probability_max[i];
    }

    public void setPrecipitation_probability_max(int[] precipitation_probability_max) {
      this.precipitation_probability_max = precipitation_probability_max;
    }

    public int[] getWind_direction_10m_dominant() {
      return wind_direction_10m_dominant;
    }

    public int getWind_direction_10m_dominant(int i) {
      return wind_direction_10m_dominant[i];
    }

    public void setWind_direction_10m_dominant(int[] wind_direction_10m_dominant) {
      this.wind_direction_10m_dominant = wind_direction_10m_dominant;
    }

    public float[] getWind_speed_10m_max() {
      return wind_speed_10m_max;
    }

    public float getWind_speed_10m_max(int i) {
      return wind_speed_10m_max[i];
    }

    public void setWind_speed_10m_max(float[] wind_speed_10m_max) {
      this.wind_speed_10m_max = wind_speed_10m_max;
    }
  }
}
package com.srednal.spotwx;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * Model for json response from Weather
 */
public class WeatherJson {

  private double latitude;
  private double longitude;
  private double elevation; // meters
  private String timezone;
  private Hourly hourly;
  private Daily daily;

  public double getLatitude() {
    return latitude;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  public double getElevation() {
    return elevation;
  }

  public void setElevation(double elevation) {
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

  public class Hourly {
    private String[] time;
    private int[] weather_code;
    private double[] temperature_2m;
    private double[] precipitation;
    private int[] precipitation_probability;
    private int[] wind_direction_10m;
    private double[] wind_speed_10m;

    public int getLength() {
      return time.length;
    }

    public String[] getTime() {
      return time;
    }

    public String getTime(int i) {
      return time[i];
    }

    private final DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE_TIME.withZone(getZoneId());

    public Instant getInstant(int i) {
      return Instant.from(fmt.parse(getTime(i)));
    }

    public LocalDateTime getDateTime(int i) {
      return LocalDateTime.parse(getTime(i));
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

    public double[] getTemperature_2m() {
      return temperature_2m;
    }

    public double getTemperature_2m(int i) {
      return temperature_2m[i];
    }

    public void setTemperature_2m_max(double[] temperature_2m) {
      this.temperature_2m = temperature_2m;
    }

    public double[] getPrecipitation() {
      return precipitation;
    }

    public double getPrecipitation(int i) {
      return precipitation[i];
    }

    public void setPrecipitation_sum(double[] precipitation) {
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

    public double[] getWind_speed_10m() {
      return wind_speed_10m;
    }

    public double getWind_speed_10m(int i) {
      return wind_speed_10m[i];
    }

    public void setWind_speed_10m_max(double[] wind_speed_10m) {
      this.wind_speed_10m = wind_speed_10m;
    }
  }

  public static class Daily {
    private String[] time;
    private int[] weather_code;
    private double[] temperature_2m_max;
    private double[] temperature_2m_min;
    private double[] precipitation_sum;
    private int[] precipitation_probability_max;
    private int[] wind_direction_10m_dominant;
    private double[] wind_speed_10m_max;

    public int getLength() {
      return time.length;
    }

    public String[] getTime() {
      return time;
    }

    public String getTime(int i) {
      return time[i];
    }

    public LocalDate getDate(int i) {
      return LocalDate.parse(getTime(i));
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

    public double[] getTemperature_2m_max() {
      return temperature_2m_max;
    }

    public double getTemperature_2m_max(int i) {
      return temperature_2m_max[i];
    }

    public void setTemperature_2m_max(double[] temperature_2m_max) {
      this.temperature_2m_max = temperature_2m_max;
    }

    public double[] getTemperature_2m_min() {
      return temperature_2m_min;
    }

    public double getTemperature_2m_min(int i) {
      return temperature_2m_min[i];
    }

    public void setTemperature_2m_min(double[] temperature_2m_min) {
      this.temperature_2m_min = temperature_2m_min;
    }

    public double[] getPrecipitation_sum() {
      return precipitation_sum;
    }

    public double getPrecipitation_sum(int i) {
      return precipitation_sum[i];
    }

    public void setPrecipitation_sum(double[] precipitation_sum) {
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

    public double[] getWind_speed_10m_max() {
      return wind_speed_10m_max;
    }

    public double getWind_speed_10m_max(int i) {
      return wind_speed_10m_max[i];
    }

    public void setWind_speed_10m_max(double[] wind_speed_10m_max) {
      this.wind_speed_10m_max = wind_speed_10m_max;
    }
  }
}
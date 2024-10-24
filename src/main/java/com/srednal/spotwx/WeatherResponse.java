package com.srednal.spotwx;

/**
 * Model for json response from Weather
 */
public class WeatherResponse {
  public WeatherResponse() {
  }

  private Daily daily;

  public Daily getDaily() {
    return daily;
  }

  public void setDaily(Daily daily) {
    this.daily = daily;
  }

  public static class Daily {
    public Daily() {
    }

    private String[] time;
    private int[] weather_code;
    private float[] temperature_2m_max;
    private float[] temperature_2m_min;
    private float[] precipitation_sum;    // round precip to .1"
    private int[] precipitation_probability_max;
    private int[] wind_direction_10m_dominant;
    private float[] wind_speed_10m_max;

    public String[] getTime() {
      return time;
    }

    public void setTime(String[] time) {
      this.time = time;
    }

    public int[] getWeather_code() {
      return weather_code;
    }

    public void setWeather_code(int[] weather_code) {
      this.weather_code = weather_code;
    }

    public float[] getTemperature_2m_max() {
      return temperature_2m_max;
    }

    public void setTemperature_2m_max(float[] temperature_2m_max) {
      this.temperature_2m_max = temperature_2m_max;
    }

    public float[] getTemperature_2m_min() {
      return temperature_2m_min;
    }

    public void setTemperature_2m_min(float[] temperature_2m_min) {
      this.temperature_2m_min = temperature_2m_min;
    }

    public float[] getPrecipitation_sum() {
      return precipitation_sum;
    }

    public void setPrecipitation_sum(float[] precipitation_sum) {
      this.precipitation_sum = precipitation_sum;
    }

    public int[] getPrecipitation_probability_max() {
      return precipitation_probability_max;
    }

    public void setPrecipitation_probability_max(int[] precipitation_probability_max) {
      this.precipitation_probability_max = precipitation_probability_max;
    }

    public int[] getWind_direction_10m_dominant() {
      return wind_direction_10m_dominant;
    }

    public void setWind_direction_10m_dominant(int[] wind_direction_10m_dominant) {
      this.wind_direction_10m_dominant = wind_direction_10m_dominant;
    }

    public float[] getWind_speed_10m_max() {
      return wind_speed_10m_max;
    }

    public void setWind_speed_10m_max(float[] wind_speed_10m_max) {
      this.wind_speed_10m_max = wind_speed_10m_max;
    }

    private WeatherReport[] getReports() {
      int numDays = getTime().length;
      WeatherReport[] models = new WeatherReport[numDays];
      for (int i = 0; i < numDays; i++) {
        models[i] = new WeatherReport(
            time[i],
            weather_code[i],
            temperature_2m_max[i],
            temperature_2m_min[i],
            precipitation_sum[i],
            precipitation_probability_max[i],
            wind_direction_10m_dominant[i],
            wind_speed_10m_max[i]);
      }
      return models;
    }
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (WeatherReport m : daily.getReports()) {
      sb.append(m).append('\n');
    }
    return sb.toString();
  }
}


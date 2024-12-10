package com.srednal.spotwx;

import com.srednal.spotwx.WeatherJson.Daily;
import com.srednal.spotwx.WeatherJson.Hourly;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.time.ZoneId;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class WeatherQueryTest {

  @Test
  void getWeatherURL() {
    URL u = WeatherQuery.getWeatherURL(new Position(40.68956, -74.044093));

    assertEquals("api.open-meteo.com", u.getHost());
    assertEquals("/v1/forecast", u.getPath());

    Map<String, String> params = new HashMap<>();
    for (String p : u.getQuery().split("&")) {
      String[] pv = p.split("=", 2);
      if (params.containsKey(pv[0])) {
        params.put(pv[0], params.get(pv[0]) + "," + pv[1]);
      } else {
        params.put(pv[0], pv[1]);
      }
    }

    assertEquals("40.68956", params.get("latitude"));
    assertEquals("-74.044093", params.get("longitude"));
    assertEquals("America%2FNew_York", params.get("timezone"));

    assertEquals("fahrenheit", params.get("temperature_unit"));
    assertEquals("mph", params.get("wind_speed_unit"));
    assertEquals("inch", params.get("precipitation_unit"));
    assertEquals("2", params.get("forecast_days"));
    assertEquals("3", params.get("forecast_hours"));

    Set<String> daily = new HashSet<>();
    daily.addAll(Arrays.asList(params.get("daily").split(",")));

    assertTrue(daily.contains("weather_code"));
    assertTrue(daily.contains("temperature_2m_max"));
    assertTrue(daily.contains("temperature_2m_min"));
    assertTrue(daily.contains("precipitation_sum"));
    assertTrue(daily.contains("precipitation_probability_max"));
    assertTrue(daily.contains("wind_direction_10m_dominant"));
    assertTrue(daily.contains("wind_speed_10m_max"));

    Set<String> hourly = new HashSet<>();
    hourly.addAll(Arrays.asList(params.get("hourly").split(",")));

    assertTrue(hourly.contains("weather_code"));
    assertTrue(hourly.contains("temperature_2m"));
    assertTrue(hourly.contains("precipitation"));
    assertTrue(hourly.contains("precipitation_probability"));
    assertTrue(hourly.contains("wind_direction_10m"));
    assertTrue(hourly.contains("wind_speed_10m"));
  }

  @Test
  void parseJson() throws IOException {
    WeatherJson json = WeatherQuery.parseJson(ClassLoader.getSystemResourceAsStream("weatherResponse.json"));
    assertEquals(40.362362, json.getLatitude());
    assertEquals(-105.5146, json.getLongitude());
    assertEquals(2300, json.getElevation());
    assertEquals("America/Denver", json.getTimezone());
    assertEquals(ZoneId.of("America/Denver"), json.getZoneId());
  }

  @Test
  void parseJsonHourly() throws IOException {
    WeatherJson json = WeatherQuery.parseJson(ClassLoader.getSystemResourceAsStream("weatherResponse.json"));
    Hourly h = json.getHourly();
    assertArrayEquals(new String[]{"2024-12-09T14:00", "2024-12-09T15:00", "2024-12-09T16:00"}, h.getTime());
    assertArrayEquals(new int[]{3, 3, 71}, h.getWeather_code());
    assertArrayEquals(new double[]{27.4, 25.8, 22.0}, h.getTemperature_2m());
    assertArrayEquals(new double[]{1.3, 0, 6.108}, h.getPrecipitation());
    assertEquals(9.3, h.getWind_speed_10m(1));
    assertEquals(289, h.getWind_direction_10m(2));
  }

  @Test
  void parseJsonDaily() throws IOException {
    WeatherJson json = WeatherQuery.parseJson(ClassLoader.getSystemResourceAsStream("weatherResponse.json"));
    Daily d = json.getDaily();
    assertArrayEquals(new String[]{"2024-12-09", "2024-12-10"}, d.getTime());
    assertArrayEquals(new int[]{71, 3}, d.getWeather_code());
    assertArrayEquals(new double[]{29.3, 23.1}, d.getTemperature_2m_max());
    assertArrayEquals(new double[]{10.7, 10.1}, d.getTemperature_2m_min());
    assertArrayEquals(new int[]{12, 2}, d.getPrecipitation_probability_max());
    assertEquals(6.008, d.getPrecipitation_sum(0));
    assertEquals(24.0, d.getWind_speed_10m_max(1));
  }
}
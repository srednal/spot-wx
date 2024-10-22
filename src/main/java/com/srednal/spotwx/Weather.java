package com.srednal.spotwx;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

public class Weather {

  private static final Logger logger = new Logger("Weather");

  public static WeatherResponse getWeather(double lat, double lon) throws IOException, URISyntaxException {

    URI uri = new URIBuilder()
        .setScheme("https")
        .setHost("api.open-meteo.com")
        .setPath("v1/forecast")
        .addParameter("latitude", String.valueOf(lat))
        .addParameter("longitude", String.valueOf(lon))
        .addParameter("forecast_days", "2")
        .addParameter("temperature_unit", "fahrenheit")
        .addParameter("lwind_speed_unit", "mph")
        .addParameter("lprecipitation_unit", "inch")
        .addParameter("timezone", "America/Denver")
        .addParameter("daily", "weather_code")
        .addParameter("daily", "temperature_2m_max")
        .addParameter("daily", "temperature_2m_min")
        .addParameter("daily", "precipitation_sum")
        .addParameter("daily", "precipitation_probability_max")
        .addParameter("daily", "wind_direction_10m_dominant")
        .addParameter("daily", "wind_speed_10m_max")
        .build();

    logger.log("Fetch weather from " + uri.toASCIIString());

    try (InputStream is = uri.toURL().openStream()) {
      ObjectMapper mapper = new ObjectMapper()
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

      return mapper.readValue(is, WeatherResponse.class);
    }
  }
}

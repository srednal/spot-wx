package com.srednal.spotwx;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.utils.URIBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class Weather {

  private static final Logger logger = LogManager.getLogger();

  public static WeatherResponse getWeather(Position pos) throws IOException {

    URL url;
    try {
      url = new URIBuilder()
          .setScheme("https")
          .setHost("api.open-meteo.com")
          .setPath("v1/forecast")
          .addParameter("latitude", String.valueOf(pos.latitude))
          .addParameter("longitude", String.valueOf(pos.longitude))
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
          .build().toURL();
    } catch (URISyntaxException | MalformedURLException e) {
      logger.fatal("Error building URI (should not happen!)", e);
      throw new RuntimeException(e);
    }

    logger.info("Fetching weather from {}", url);

      try (InputStream is = url.openStream()) {
        ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return mapper.readValue(is, WeatherResponse.class);
      }
  }
}

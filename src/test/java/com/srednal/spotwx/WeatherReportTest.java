package com.srednal.spotwx;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class WeatherReportTest {

  WeatherJson json;

  @BeforeEach
  void setUp() throws IOException {
    json = WeatherQuery.parseJson(ClassLoader.getSystemResourceAsStream("weatherResponse.json"));
  }

  @Test
  void wxCondition() {
    assertEquals("Clear", WeatherReport.wxCondition(1));
    assertEquals("Snow", WeatherReport.wxCondition(73));
    assertEquals("T-storm", WeatherReport.wxCondition(95));
    assertEquals("90", WeatherReport.wxCondition(90));
  }

  @Test
  void direction() {
    assertEquals("N", WeatherReport.direction(0));
    assertEquals("E", WeatherReport.direction(90));
    assertEquals("S", WeatherReport.direction(180));
    assertEquals("W", WeatherReport.direction(270));
    assertEquals("W", WeatherReport.direction(-90));
    assertEquals("NE", WeatherReport.direction(45));
    assertEquals("SE", WeatherReport.direction(135));
    assertEquals("SW", WeatherReport.direction(-135));
    assertEquals("NW", WeatherReport.direction(-45));
    assertEquals("N", WeatherReport.direction(22));
    assertEquals("NE", WeatherReport.direction(23));
    assertEquals("N", WeatherReport.direction(-22));
    assertEquals("NW", WeatherReport.direction(-23));
  }

  @Test
  void topOfHour() {
    String time = "2024-12-09T14:10:00-07:00";
    Instant tenAfter = Instant.parse(time);
    WeatherReport r = new WeatherReport(json, tenAfter);
    // first should be at 14:00
    assertEquals("2024-12-09T14:00", r.get2Hourlies().get(0).time().toString());
  }

  @Test
  void bottomOfHour() {
    String time = "2024-12-09T14:20:00-07:00";
    Instant twentyAfter = Instant.parse(time);
    WeatherReport r = new WeatherReport(json, twentyAfter);
    // first should be at 15:00 - skipped 14:00
    assertEquals("2024-12-09T15:00", r.get2Hourlies().get(0).time().toString());
  }

  @Test
  void hourlyReport() {
    String time = "2024-12-09T14:20:00-07:00";
    Instant twentyAfter = Instant.parse(time);
    WeatherReport r = new WeatherReport(json, twentyAfter);
    assertEquals("" +
            "12/09 15:00: Overcast | 26F | 0.0\" (5%) | NE@9mph\n" +
            "12/09 16:00: Lt Snow | 22F | 6.1\" (10%) | W@12mph",
        r.hourlyReport());
  }

  @Test
  void dailyReport() {
    WeatherReport r = new WeatherReport(json);
    assertEquals("" +
        "12/09: Lt Snow | 29/11F | 6.0\" (12%) | NE@17mph\n" +
        "12/10: Overcast | 23/10F | 0.0\" (2%) | W@24mph",
    r.dailyReport());

  }
}
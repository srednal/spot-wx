package com.srednal.spotwx;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GMailMessageHeaderTest {

  Map<String, String> headers = new HashMap<>();
  String snippet = "";

  GMailMessage message = new GMailMessage(null, headers) {
    @Override
    public String getSnippet() {
      return snippet;
    }
  };

  @BeforeEach
  void setUp() {
    headers.clear();
    snippet = "";
  }

  @Test
  void getHeader() {
    headers.put("one", "two");
    assertEquals("two", message.getHeader("one"));
    assertNull(message.getHeader("two"));
  }

  @Test
  void hasLatLonInHeader() {
    assertFalse(message.hasLatLon());
    headers.put("X-SPOT-Latitude", "12");
    assertFalse(message.hasLatLon());
    headers.put("X-SPOT-Longitude", "34");
    assertTrue(message.hasLatLon());
  }

  @Test
  void hasLatLonInBody() {
    headers.put("X-SPOT-Latitude", "12");
    snippet = "40.1234, -105.4321";
    assertTrue(message.hasLatLon());
  }

  @Test
  void getLatitudeFromHeader() {
    headers.put("X-SPOT-Latitude", "40.1234");
    assertEquals("40.1234", message.getLatitude());
  }

  @Test
  void getLatitudeFromBody() {
    snippet = "40.2468, -105.8642";
    assertEquals("40.2468", message.getLatitude());
  }

  @Test
  void getLongitudeFromHeader() {
    headers.put("X-SPOT-Longitude", "-105.4321");
    assertEquals("-105.4321", message.getLongitude());
  }

  @Test
  void getLongitudeFromBody() {
    snippet = "40.369, -105.963";
    assertEquals("-105.963", message.getLongitude());
  }

  @Test
  void getPosition() {
    snippet = "40.1928, -105.912";
    Position p = message.getPosition();
    assertEquals(40.1928D, p.latitude);
    assertEquals(-105.912D, p.longitude);
  }
}
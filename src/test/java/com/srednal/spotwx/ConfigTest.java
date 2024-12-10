package com.srednal.spotwx;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {

  @Test
  void getDefaultInstance() {
    Config c = Config.getInstance();
    assertTrue(c.isMarkEmailRead());
    assertEquals(120, c.getPollInterval());
    assertEquals("SpotWx", c.getApplicationName());
  }

  @Test
  void readConfigFailsOnMissing() {
    assertThrows(IllegalArgumentException.class, () ->
        Config.readConfig(ConfigTest.class.getResourceAsStream("/config-test.json"), true)
    );
  }

  @Test
  void readConfig() throws IOException {
    Config c = Config.readConfig(ConfigTest.class.getResourceAsStream("/config-test.json"), false);

    // changes
    assertFalse(c.isSendEmail());
    assertEquals("TestSpot", c.getApplicationName());
    assertEquals(90, c.getPollInterval());

    // unchanged are null
    assertNull(c.getMarkEmailReadObj());
    assertNull(c.getTopOfHourMinutesObj());
    assertNull(c.getSecurityDir());
  }

  @Test
  void override() {
    Config c = Config.getInstance();

    Config o = new Config();
    o.setCredentialsFileName("overrideCredFile");
    o.setInitialDelay(135);
    o.setSendEmail(false);

    c.override(o);

    // defaults
    assertTrue(c.isMarkEmailRead());
    assertEquals(120, c.getPollInterval());
    assertEquals("SpotWx", c.getApplicationName());

    // overrides
    assertEquals("overrideCredFile", o.getCredentialsFileName());
    assertEquals(135, o.getInitialDelay());
    assertFalse(o.isSendEmail());
  }
}
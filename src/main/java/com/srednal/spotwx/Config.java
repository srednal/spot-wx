package com.srednal.spotwx;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;

public class Config {

  private static final Logger logger = LogManager.getLogger();
  private static final String configResource = "/config.json";
  private static final String configFileName = "./config.json";

  private static Config instance = null;

  public static Config getInstance() {
    if (instance == null) {
      try {
        // default
        instance = readConfig(Config.class.getResourceAsStream(configResource), true);
      } catch (IOException | IllegalArgumentException e) {
        logger.error("Error reading default config resource", e);
        throw new RuntimeException(e);
      }

      // override file
      File configFile = new java.io.File(configFileName);
      if (configFile.exists()) {
        try {
          logger.info("Reading config from {}", configFile);
          instance.override(readConfig(new FileInputStream(configFile), false));

        } catch (IOException e) {
          logger.error("Error reading config file", e);
          throw new RuntimeException(e);
        }
      }
    }
    return instance;
  }

  static Config readConfig(InputStream stream, boolean validate) throws IOException, IllegalArgumentException {
    try (InputStream in = stream) {
      ObjectMapper mapper = new ObjectMapper();
      Config c = mapper.readValue(in, Config.class);
      if (validate) c.validate();
      return c;
    }
  }

  void override(Config overrides) {
    if (overrides.sendEmail != null) this.sendEmail = overrides.sendEmail;
    if (overrides.markEmailRead != null) this.markEmailRead = overrides.markEmailRead;
    if (overrides.pollInterval != null) this.pollInterval = overrides.pollInterval;
    if (overrides.initialDelay != null) this.initialDelay = overrides.initialDelay;
    if (overrides.securityDir != null) this.securityDir = overrides.securityDir;
    if (overrides.credentialsFileName != null) this.credentialsFileName = overrides.credentialsFileName;
    if (overrides.applicationName != null) this.applicationName = overrides.applicationName;
    if (overrides.latitudeHeader != null) this.latitudeHeader = overrides.latitudeHeader;
    if (overrides.longitudeHeader != null) this.longitudeHeader = overrides.longitudeHeader;
    if (overrides.messengerHeader != null) this.messengerHeader = overrides.messengerHeader;
    if (overrides.latitudeLongitudeRE != null) this.latitudeLongitudeRE = overrides.latitudeLongitudeRE;
    if (overrides.messageTrimRE != null) this.messageTrimRE = overrides.messageTrimRE;
    if (overrides.topOfHourMinutes != null) this.topOfHourMinutes = overrides.topOfHourMinutes;
    if (overrides.hourlyTimeFormat != null) this.hourlyTimeFormat = overrides.hourlyTimeFormat;
    if (overrides.hourlyFormat != null) this.hourlyFormat = overrides.hourlyFormat;
    if (overrides.dailyDateFormat != null) this.dailyDateFormat = overrides.dailyDateFormat;
    if (overrides.dailyFormat != null) this.dailyFormat = overrides.dailyFormat;
  }

  void validate() throws IllegalArgumentException {
    if (this.sendEmail == null) throw new IllegalArgumentException("sendEmail");
    if (this.markEmailRead == null) throw new IllegalArgumentException("markEmailRead");
    if (this.pollInterval == null) throw new IllegalArgumentException("pollInterval");
    if (this.initialDelay == null) throw new IllegalArgumentException("initialDelay");
    if (this.securityDir == null) throw new IllegalArgumentException("securityDir");
    if (this.credentialsFileName == null) throw new IllegalArgumentException("credentialsFileName");
    if (this.applicationName == null) throw new IllegalArgumentException("applicationName");
    if (this.latitudeHeader == null) throw new IllegalArgumentException("latitudeHeader");
    if (this.longitudeHeader == null) throw new IllegalArgumentException("longitudeHeader");
    if (this.messengerHeader == null) throw new IllegalArgumentException("messengerHeader");
    if (this.latitudeLongitudeRE == null) throw new IllegalArgumentException("latitudeLongitudeRE");
    if (this.messageTrimRE == null) throw new IllegalArgumentException("messageTrimRE");
    if (this.topOfHourMinutes == null) throw new IllegalArgumentException("topOfHourMinutes");
    if (this.hourlyTimeFormat == null) throw new IllegalArgumentException("hourlyTimeFormat");
    if (this.hourlyFormat == null) throw new IllegalArgumentException("hourlyFormat");
    if (this.dailyDateFormat == null) throw new IllegalArgumentException("dailyDateFormat");
    if (this.dailyFormat == null) throw new IllegalArgumentException("dailyFormat");
  }

  private Boolean sendEmail;
  private Boolean markEmailRead;
  private Long pollInterval;
  private Long initialDelay;
  // Directory to store authorization tokens
  private String securityDir;
  private String credentialsFileName;
  private String applicationName; // for gmail credentials
  private String latitudeHeader;
  private String longitudeHeader;
  private String messengerHeader;  // like Dave's Spot
  // latitude in group 1, longitude group 2
  private String latitudeLongitudeRE;
  // trim some stuff from the message snippet when logging. RE will be replaced with nothing
  private String messageTrimRE;
  private Long topOfHourMinutes;
  private String hourlyTimeFormat;
  // time, condition, temp, precip, prob, direction, speed
  private String hourlyFormat;
  private String dailyDateFormat;
  // time, condition, temp max, min, precip, prob, direction, speed
  private String dailyFormat;

  public boolean isSendEmail() {
    return sendEmail;
  }

  Boolean getSendEmailObj() {
    return sendEmail;
  }

  public void setSendEmail(boolean sendEmail) {
    this.sendEmail = sendEmail;
  }

  public boolean isMarkEmailRead() {
    return markEmailRead;
  }

  Boolean getMarkEmailReadObj() {
    return markEmailRead;
  }


  public void setMarkEmailRead(boolean markEmailRead) {
    this.markEmailRead = markEmailRead;
  }

  public long getPollInterval() {
    return pollInterval;
  }

  Long getPollIntervalObj() {
    return pollInterval;
  }

  public void setPollInterval(long pollInterval) {
    this.pollInterval = pollInterval;
  }

  public long getInitialDelay() {
    return initialDelay;
  }

  Long getInitialDelayObj() {
    return initialDelay;
  }

  public void setInitialDelay(long initialDelay) {
    this.initialDelay = initialDelay;
  }

  public String getSecurityDir() {
    return securityDir;
  }

  public void setSecurityDir(String securityDir) {
    this.securityDir = securityDir;
  }

  public String getCredentialsFileName() {
    return credentialsFileName;
  }

  public void setCredentialsFileName(String credentialsFileName) {
    this.credentialsFileName = credentialsFileName;
  }

  public String getApplicationName() {
    return applicationName;
  }

  public void setApplicationName(String applicationName) {
    this.applicationName = applicationName;
  }

  public String getLatitudeHeader() {
    return latitudeHeader;
  }

  public void setLatitudeHeader(String latitudeHeader) {
    this.latitudeHeader = latitudeHeader;
  }

  public String getLongitudeHeader() {
    return longitudeHeader;
  }

  public void setLongitudeHeader(String longitudeHeader) {
    this.longitudeHeader = longitudeHeader;
  }

  public String getMessengerHeader() {
    return messengerHeader;
  }

  public void setMessengerHeader(String messengerHeader) {
    this.messengerHeader = messengerHeader;
  }

  public String getLatitudeLongitudeRE() {
    return latitudeLongitudeRE;
  }

  public void setLatitudeLongitudeRE(String latitudeLongitudeRE) {
    this.latitudeLongitudeRE = latitudeLongitudeRE;
  }

  public String getMessageTrimRE() {
    return messageTrimRE;
  }

  public void setMessageTrimRE(String messageTrimRE) {
    this.messageTrimRE = messageTrimRE;
  }

  public long getTopOfHourMinutes() {
    return topOfHourMinutes;
  }

  Long getTopOfHourMinutesObj() {
    return topOfHourMinutes;
  }

  public void setTopOfHourMinutes(long topOfHourMinutes) {
    this.topOfHourMinutes = topOfHourMinutes;
  }

  public String getHourlyTimeFormat() {
    return hourlyTimeFormat;
  }

  public void setHourlyTimeFormat(String hourlyTimeFormat) {
    this.hourlyTimeFormat = hourlyTimeFormat;
  }

  public String getHourlyFormat() {
    return hourlyFormat;
  }

  public void setHourlyFormat(String hourlyFormat) {
    this.hourlyFormat = hourlyFormat;
  }

  public String getDailyDateFormat() {
    return dailyDateFormat;
  }

  public void setDailyDateFormat(String dailyDateFormat) {
    this.dailyDateFormat = dailyDateFormat;
  }

  public String getDailyFormat() {
    return dailyFormat;
  }

  public void setDailyFormat(String dailyFormat) {
    this.dailyFormat = dailyFormat;
  }
}

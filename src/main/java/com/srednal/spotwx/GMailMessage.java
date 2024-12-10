package com.srednal.spotwx;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GMailMessage {

  private static final Logger logger = LogManager.getLogger();
  private static final Config config = Config.getInstance();

  private static final String TO = "To";
  private static final String FROM = "From";
  private static final String REPLY_TO = "Reply-To";
  private static final String SUBJECT = "Subject";
  public static final String[] LAT_LONG_HEADERS = {config.getLongitudeHeader(), config.getLatitudeHeader()};
  // Other spot headers:
  // X-SPOT-Time: 1729189481
  // X-SPOT-Type: Custom

  // pattern to find lat lon in body snippet
  private static final Pattern LAT_LON_RE = Pattern.compile(config.getLatitudeLongitudeRE());

  private final Message message;
  private final Map<String, String> headers;

  GMailMessage(Message message, Map<String, String> headers) {
    this.message = message;
    this.headers = headers;
  }

  public static GMailMessage fetch(Gmail.Users.Messages userMessages, String id) throws IOException {
    // fetch the full message
    Message msg = userMessages.get(GMail.USER, id).execute();

    List<MessagePartHeader> msgHead = msg.getPayload().getHeaders();
    Map<String, String> headers = new HashMap<>();
    msgHead.forEach(h -> headers.put(h.getName(), h.getValue()));
    return new GMailMessage(msg, headers);
  }

  public String getId() {
    return message.getId();
  }

  public boolean hasLatLon() {
    boolean hasHeaders = Arrays.stream(LAT_LONG_HEADERS).allMatch(headers::containsKey);
    boolean bodyMatches = LAT_LON_RE.matcher(getSnippet()).matches();
    logger.debug("LatLon headers: {}; bodyMatches: {}", hasHeaders, bodyMatches);
    return hasHeaders || bodyMatches;
  }

  public String getHeader(String key) {
    return headers.get(key);
  }

  public String getTo() {
    return getHeader(TO);
  }

  public String getFrom() {
    return getHeader(FROM);
  }

  public String getReplyTo() {
    return getHeader(REPLY_TO);
  }

  public String getSubject() {
    return getHeader(SUBJECT);
  }

  public String getLatitude() {
    String lat = getHeader(config.getLatitudeHeader());
    if (lat == null) {
      Matcher m = LAT_LON_RE.matcher(getSnippet());
      if (m.matches()) lat = m.group(1);
    }
    return lat;
  }

  public String getLongitude() {
    String lon = getHeader(config.getLongitudeHeader());
    if (lon == null) {
      Matcher m = LAT_LON_RE.matcher(getSnippet());
      if (m.matches()) lon = m.group(2);
    }
    return lon;
  }

  public Position getPosition() throws NumberFormatException {
    return new Position(Double.parseDouble(getLatitude()), Double.parseDouble(getLongitude()));
  }

  public String getSpotMessenger() {
    return getHeader(config.getMessengerHeader());
  }

  public String getSnippet() {
    // snippet is something like (no CRs, just spaces):
    // Device Name: Dave&#39;s Spot GPS location
    // Date/Time: 11/09/2024 09:03:10 MST
    // The message sender did not share their GPS location with you.
    // Message: <the actual message>
    // You have received this message because ...
    return message.getSnippet();
  }

  @Override
  public String toString() {
    return "%s (%s)\n\tSubject: %s\n\tMessage: %s".formatted(
        getFrom(),
        getSpotMessenger(),
        getSubject(),
        getSnippet().replaceAll(config.getMessageTrimRE(), "")
    );
  }
}
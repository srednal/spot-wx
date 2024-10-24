package com.srednal.spotwx;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GMailMessage {

  private static final String TO = "To";
  private static final String FROM = "From";
  private static final String REPLY_TO = "Reply-To";
  private static final String SUBJECT = "Subject";
  private static final String X_SPOT_LATITUDE = "X-SPOT-Latitude";
  private static final String X_SPOT_LONGITUDE = "X-SPOT-Longitude";
  private static final String X_SPOT_MESSENGER = "X-SPOT-Messenger";  // like Dave's Spot
  public static final String[] LAT_LONG_HEADERS = {X_SPOT_LATITUDE, X_SPOT_LONGITUDE};

  private final Message message;
  private final Map<String, String> headers;

  private GMailMessage(Message message, Map<String, String> headers) {
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

  public boolean hasAllHeaders(String... theHeaders) {
    return Arrays.stream(theHeaders).allMatch(headers::containsKey);
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
    return getHeader(X_SPOT_LATITUDE);
  }

  public String getLongitude() {
    return getHeader(X_SPOT_LONGITUDE);
  }

  public String getSpotMessenger() {
    return getHeader(X_SPOT_MESSENGER);
  }

  public String getSnippet() {
    return message.getSnippet();
  }

  @Override
  public String toString() {
    return "\n\t%s (%s)\n\tSubject: %s\n\tBody: %s".formatted(
        getFrom(),
        getSpotMessenger(),
        getSubject(),
        getSnippet()
    );
  }
}
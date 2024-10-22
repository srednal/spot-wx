package com.srednal.spotwx;

import javax.mail.MessagingException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import static com.srednal.spotwx.GMailMessage.LAT_LONG_HEADERS;

// TODO apple launcher thingy to keep it running
// TODO move to tvMac, review mac's sleep settings

public class SpotWx implements Runnable {

  private static boolean loginOnly = false;
  private static long pollInterval = 120;

  // Directory to store authorization tokens
  static String securityDir = "./security";
  static String credentialsFile = securityDir + "/credentials.json";

  private static final Logger logger = LogManager.getLogger();

  private final GMail gMail;

  public SpotWx(GMail gMail) {
    this.gMail = gMail;
  }

  public static void main(String... args) throws GeneralSecurityException, IOException {
    handleArgs(args);
    logger.info("Connecting");
    GMail gMail = GMail.connect();
    if (loginOnly) return; // just establish security stuff (initial setup)

    ScheduledExecutorService es = Executors.newScheduledThreadPool(1);
    es.scheduleWithFixedDelay(new SpotWx(gMail), pollInterval / 2, pollInterval, TimeUnit.SECONDS);
  }

  public void run() {
    // scan for messages
    // if issues with gmail, will return empty list and we will retry next round
    List<GMailMessage> messages = gMail.getUnreadMessages(LAT_LONG_HEADERS);

    messages.forEach(msg -> {
      Position pos = getPosition(msg);
      if (pos != null) {
        try {
          // query weather
          WeatherResponse wr = Weather.getWeather(pos);
          sendReply(msg, wr);
        } catch (IOException e) {
          // problem talking to the weather api, retry this message
          logger.error("Error communicating with weather api, will retry", e);
          gMail.markUnseen(msg);
        }
      }
    });
  }

  private Position getPosition(GMailMessage msg) {
    Position pos = null;
    String latHeader = msg.getLatitude();
    String lonHeader = msg.getLongitude();
    try {
      pos = new Position(latHeader, lonHeader);
      logger.info("{} {}", pos, gMail.formatLogMessage(msg));
    } catch (NumberFormatException e) {
      // problem with a single message, move on
      // the message has been marked SEEN so will just skip it
      logger.error("Skipping message with malformed Lat={}, Lon={} {}",
          latHeader, lonHeader, gMail.formatLogMessage(msg), e);
    }
    return pos;
  }

  private void sendReply(GMailMessage msg, WeatherResponse wr) {
    try {
      // send reply
      gMail.replyTo(msg, wr.toString());
    } catch (IOException | MessagingException e) {
      // problem talking to the weather api, retry this message
      logger.error("Error replying to email, will retry", e);
      gMail.markUnseen(msg);
    }

    try {
      // mark message read
      gMail.markRead(msg);
    } catch (IOException e) {
      logger.error("Error marking message READ, leaving it unread", e);
    }
  }

  private static void handleArgs(String... args) {
    Arrays.stream(args).forEach(SpotWx::handleArg);
  }

  private static void handleArg(String arg) {

    Pattern p = Pattern.compile("^([A-Za-z]+)=(.+)");
    Matcher m = p.matcher(arg);

    if (m.matches()) {
      String param = m.group(1);
      String val = m.group(2);
      switch (param) {
        case "loginOnly":
          loginOnly = Boolean.parseBoolean(val);
          break;
        case "pollInterval":
          pollInterval = Long.parseLong(val);
          break;
        case "securityDir":
          securityDir = val;
          break;
        case "credentialsFile":
          credentialsFile = val;
          break;
        default:
          logger.error("Unrecognized param: {}={}", param, val);
      }
    } else {
      logger.error("Unrecognized arg: {}", arg);
    }
  }

}
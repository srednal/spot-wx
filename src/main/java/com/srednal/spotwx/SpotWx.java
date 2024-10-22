package com.srednal.spotwx;

import javax.mail.MessagingException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import static com.srednal.spotwx.GMailMessage.LAT_LONG_HEADERS;

// TODO config for polling interval? meh
// TODO apple launcher thingy to keep it running
// TODO move to tvMac, review mac's sleep settings

public class SpotWx implements Runnable {


  private static final Logger logger = LogManager.getLogger();

  private static final long FETCH_INTERVAL_SECONDS = 120;

  private final GMail gMail;

  public SpotWx(GMail gMail) {
    this.gMail = gMail;
  }

  public static void main(String... args) throws GeneralSecurityException, IOException {
    logger.info("Connecting");
    GMail gMail = GMail.connect();
    if (args.length == 1 && args[0].equals("login")) return; // just establish security stuff

    ScheduledExecutorService es = Executors.newScheduledThreadPool(1);
    es.scheduleWithFixedDelay(new SpotWx(gMail), FETCH_INTERVAL_SECONDS / 2, FETCH_INTERVAL_SECONDS, TimeUnit.SECONDS);
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
}
package com.srednal.spotwx;

import com.google.api.services.gmail.model.Message;

import javax.mail.MessagingException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// TODO provide some config for security directory? or is $CWD ok?
// TODO config for polling interval? meh
// TODO apple launcher thingy to keep it running
// TODO logs (stdout) to somewhere
// TODO log4j or something, rotate weekly, keep 6 weeks
// TODO move to tvMac, review mac's sleep settings

public class SpotWx implements Runnable {
  static String X_SPOT_LATITUDE = "X-SPOT-Latitude";
  static String X_SPOT_LONGITUDE = "X-SPOT-Longitude";
  static String X_SPOT_MESSENGER = "X-SPOT-Messenger";  // like Dave's Spot

  private static final Logger logger = new Logger("SpotWx");

  private static final long FETCH_INTERVAL_SECONDS = 120;

  private final GMail gMail;

  public SpotWx(GMail gMail) {
    this.gMail = gMail;
  }

  public static void main(String... args) throws GeneralSecurityException, IOException {
    logger.log("Connecting");
    GMail gMail = GMail.connect();
    if (args.length == 1 && args[0].equals("login")) return; // just establish security stuff

    ScheduledExecutorService es = Executors.newScheduledThreadPool(1);
    es.scheduleWithFixedDelay(new SpotWx(gMail), FETCH_INTERVAL_SECONDS / 2, FETCH_INTERVAL_SECONDS, TimeUnit.SECONDS);
  }

  public void run() {
    try {
      // scan for messages
      List<Message> messages = gMail.getUnreadMessages(X_SPOT_LATITUDE, X_SPOT_LONGITUDE);

      messages.forEach(msg -> {
        try {
          // fetch lon, lat
          Map<String, String> headers = gMail.getHeaders(msg);
          double lat = Double.parseDouble(headers.get(X_SPOT_LATITUDE));
          double lon = Double.parseDouble(headers.get(X_SPOT_LONGITUDE));
          logger.log("Lat = " + lat + ", Lon = " + lon, gMail, msg);
          // query weather
          WeatherResponse wr = Weather.getWeather(lat, lon);
          // send reply
          gMail.replyTo(msg, wr.toString());
          // mark message read
          gMail.markRead(msg);
        } catch (NumberFormatException e) {
          // problem with a single message, move on
          logger.log("Malformed Lat/Long, marking READ", gMail, msg);
          try {
            gMail.markRead(msg);
          } catch (IOException ioe) {
            logger.log(ioe);
          }
        } catch (IOException | URISyntaxException | MessagingException e) {
          // bigger problem - exit
          logger.log(e);
          throw new RuntimeException(e);
        }
      });
    } catch (
        Exception e) {
      logger.log(e);
      throw new RuntimeException(e);
    }
  }
}

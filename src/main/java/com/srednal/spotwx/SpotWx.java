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

// TODO git
// TODO fat jar
// TODO provide some config for security directory?
// TODO config for polling interval
// TODO apple launcher thingy to keep it running
// TODO move to tvMac, review sleep

public class SpotWx {
  static String X_SPOT_LATITUDE = "X-SPOT-Latitude";
  static String X_SPOT_LONGITUDE = "X-SPOT-Longitude";
  static String X_SPOT_MESSENGER = "X-SPOT-Messenger";  // like Dave's Spot

  private static final Logger logger = new Logger("SpotWx");

  private static final long FETCH_INTERVAL_SECONDS = 120;

  public static void main(String... args) throws GeneralSecurityException, IOException {
    if (args.length == 1 && args[0].equals("login")) {
      GMail.connect();
    } else {
      SpotWx poller = new SpotWx();
      ScheduledExecutorService es = Executors.newScheduledThreadPool(1);
      es.scheduleAtFixedRate(poller::poll, FETCH_INTERVAL_SECONDS / 2, FETCH_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }
  }

  private void poll() {

    try {
      // connect
      GMail gMail = GMail.connect();
      // scan for messages
      List<Message> messages = gMail.getUnreadMessages(X_SPOT_LATITUDE, X_SPOT_LONGITUDE);

      messages.forEach(msg -> {
        // fetch lon, lat
        Map<String, String> headers = gMail.getHeaders(msg);
        double lat = Double.parseDouble(headers.get(X_SPOT_LATITUDE));
        double lon = Double.parseDouble(headers.get(X_SPOT_LONGITUDE));

        logger.log("Lat = " + lat + ", Lon = " + lon, gMail, msg);

        try {
          // query weather
          WeatherResponse wr = Weather.getWeather(lat, lon);

          // send reply
          // Note message on device will be "wx: body"
          // so we're prefixing the body with \n for formatting niceties
          gMail.replyTo(msg, "wx", "\n" + wr.toString());

          // mark message read
          gMail.markRead(msg);

        } catch (IOException | URISyntaxException | MessagingException e) {
          logger.log(e);
        }
      });
    } catch (IOException | GeneralSecurityException e) {
      logger.log(e);
    }

  }


}

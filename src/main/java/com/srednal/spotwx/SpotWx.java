package com.srednal.spotwx;

import javax.mail.MessagingException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class SpotWx implements Runnable {

  private static final Logger logger = LogManager.getLogger();
  private static final Config config = Config.getInstance();

  private final GMail gMail;

  public SpotWx(GMail gMail) {
    this.gMail = gMail;
  }

  private static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

  static {
    Runtime.getRuntime().addShutdownHook(new Thread(executorService::close));
    Runtime.getRuntime().addShutdownHook(new Thread(() -> logger.info("Exiting")));
  }

  public static void main(String... args) throws GeneralSecurityException, IOException {
    logger.info("Connecting");
    GMail gMail = GMail.connect();
    if (isLoginOnly(args)) return; // just establish security stuff (initial setup)

    logger.info("Starting poll in {}s with delay {}s", config.getInitialDelay(), config.getPollInterval());
    executorService.scheduleWithFixedDelay(new SpotWx(gMail), config.getInitialDelay(), config.getPollInterval(), TimeUnit.SECONDS);
  }

  private static boolean isLoginOnly(String[] args) {
    return args.length != 0 && "loginOnly".equals(args[0]);
  }

  public void run() {
    try {
      logger.debug("poll in");
      // scan for messages
      // if issues with gmail, will return empty list and we will retry next round
      List<GMailMessage> messages = gMail.getUnreadMessages();

      messages.forEach(msg -> {
        Position pos = getPosition(msg);
        if (pos != null) {
          try {
            // query weather
            WeatherReport wr = WeatherQuery.getWeatherReport(pos);
            sendReply(msg, wr);
          } catch (IOException e) {
            // problem talking to the weather api, retry this message
            logger.error("Error communicating with weather api, will retry", e);
            gMail.markUnseen(msg);
          }
        }
      });
      logger.debug("poll out");
    } catch (Throwable t) {
      // uncaught exception should exit the app
      logger.fatal("Error during poll", t);
      executorService.shutdown();
    }
  }

  Position getPosition(GMailMessage msg) {
    Position pos = null;
    try {
      pos = msg.getPosition();
      logger.info("{}\n\t{}", pos, msg);
    } catch (NumberFormatException e) {
      // problem with a single message, move on
      // the message has been marked SEEN so will just skip it
      logger.error("Skipping message with malformed Lat={}, Lon={} {}",
          msg.getLatitude(), msg.getLongitude(), msg, e);
    }
    return pos;
  }

  private void sendReply(GMailMessage msg, WeatherReport wr) {
    try {
      // send reply - hourly & daily (2 messages due to 140 char limit)
      gMail.replyTo(msg, wr.hourlyReport());
      gMail.replyTo(msg, wr.dailyReport());
    } catch (IOException | MessagingException e) {
      // problem talking to the gmail api, retry this message
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
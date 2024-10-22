package com.srednal.spotwx;

import com.google.api.services.gmail.model.Message;

import java.time.Instant;
import java.util.Map;

import static com.srednal.spotwx.GMail.FROM;
import static com.srednal.spotwx.GMail.SUBJECT;
import static com.srednal.spotwx.SpotWx.X_SPOT_MESSENGER;

public class Logger {

  private final String ctx;

  public Logger(String ctx) {
    this.ctx = ctx;
  }

  public void log(String msg) {
    System.out.printf("%s %s> %s\n", Instant.now(), ctx, msg);
  }

  public void log(String info, GMail gMail, Message msg) {
    Map<String, String> h = gMail.getHeaders(msg);
    log("%s\n\t%s (%s)\n\tSubject: %s\n\tBody: %s".formatted(info, h.get(FROM), h.get(X_SPOT_MESSENGER), h.get(SUBJECT), msg.getSnippet()));
  }

  public void log(Exception ex) {
    log(ex.toString());
  }

}

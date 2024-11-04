package com.srednal.spotwx;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.*;

public class GMail {

  private static final String APPLICATION_NAME = "SpotWx"; // for gmail credentials

  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

  /**
   * Global instance of the scopes required by this quickstart.
   * If modifying these scopes, delete your previously saved tokens/ folder.
   */
  private static final List<String> SCOPES = Arrays.asList(
      GmailScopes.MAIL_GOOGLE_COM,
      GmailScopes.GMAIL_INSERT,
      GmailScopes.GMAIL_LABELS,
      GmailScopes.GMAIL_COMPOSE,
      GmailScopes.GMAIL_MODIFY,
      GmailScopes.GMAIL_READONLY,
      GmailScopes.GMAIL_SEND
  );

  public static final String USER = "me";
  private static final String INBOX = "INBOX";
  private static final String UNREAD = "UNREAD";

  private static final Logger logger = LogManager.getLogger();

  private final Gmail service;

  private GMail(Gmail service) {
    this.service = service;
  }

  public static GMail connect() throws IOException, GeneralSecurityException {
    // Build a new authorized API client service.
    final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

    // Load client secrets.
    File credentialsFile = new java.io.File(SpotWx.credentialsFile);
    if (!credentialsFile.exists()) {
      throw new FileNotFoundException("File not found: " + credentialsFile);
    }
    FileInputStream in = new FileInputStream(credentialsFile);
    GoogleClientSecrets clientSecrets =
        GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

    // Build flow and trigger user authorization request.
    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
        httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
        .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(SpotWx.securityDir)))
        .setAccessType("offline")
        .build();
    LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
    Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

    Gmail svc = new Gmail.Builder(httpTransport, JSON_FACTORY, credential)
        .setApplicationName(APPLICATION_NAME)
        .build();

    return new GMail(svc);
  }

  // Messages we've seen
  private final Set<String> seenMessageIds = new HashSet<>();

  public void markUnseen(GMailMessage msg) {
    seenMessageIds.remove(msg.getId());
  }

  public List<GMailMessage> getUnreadMessages(String... withHeaders) {
    List<GMailMessage> messages = new ArrayList<>();
    try {
      // Note this message contains only id and threadId
      List<Message> sparseMessages =
          userMessages().list(USER).setLabelIds(List.of(INBOX, UNREAD)).execute().getMessages();

      if (sparseMessages == null) sparseMessages = Collections.emptyList();

      // remove any already seen
      List<String> messageIds = sparseMessages.stream().map(Message::getId).toList();
      logger.debug("Queried message ids (UNREAD,INBOX) {}", messageIds);
      logger.debug("Seen message ids {}", seenMessageIds);

      List<String> newMessageIds = messageIds.stream().filter(i -> !seenMessageIds.contains(i)).toList();
      logger.debug("New messages {}", newMessageIds);

      // the ids we fetched have now been seen
      seenMessageIds.clear();
      seenMessageIds.addAll(messageIds);

      if (!newMessageIds.isEmpty()) logger.info("Found {} new messages", newMessageIds.size());

      for (String id : newMessageIds) {
        try {
          // fetch the full message
          GMailMessage msg = GMailMessage.fetch(userMessages(), id);
          // see if it has spot lat/lon headers
          if (msg.hasAllHeaders(withHeaders)) messages.add(msg);
        } catch (IOException e) {
          logger.error("Problem fetching message id {} - will retry", id, e);
          seenMessageIds.remove(id);
        }
      }
    } catch (IOException e) {
      // from initial list(USER) for sparseMessages
      logger.error("Problem listing messages - will retry", e);
    }

    if (messages.isEmpty()) logger.info("No new messages with required headers");

    return messages;
  }

  private Gmail.Users.Messages userMessages() {
    return service.users().messages();
  }

  public void replyTo(GMailMessage msg, String body) throws IOException, MessagingException {
    String from = msg.getTo(); // from the recipient
    String replyTo = msg.getReplyTo();
    logger.info("Reply to {} with {}", replyTo, body);
    Message message = makeMessage(from, replyTo, body);
    userMessages().send(USER, message).execute();
  }

  private Message makeMessage(String from, String to, String body) throws MessagingException, IOException {
    Session session = Session.getDefaultInstance(new Properties(), null);
    MimeMessage email = new MimeMessage(session);
    email.setFrom(new InternetAddress(from));
    email.addRecipient(RecipientType.TO, new InternetAddress(to));
    email.setSubject(null);
    email.setText(body);

    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    email.writeTo(buffer);
    byte[] bytes = buffer.toByteArray();
    String encodedEmail = Base64.encodeBase64URLSafeString(bytes);

    return new Message().setRaw(encodedEmail);
  }

  public void markRead(GMailMessage message) throws IOException {
    logger.info("Marking READ");
    ModifyMessageRequest req = new ModifyMessageRequest().setRemoveLabelIds(Collections.singletonList(UNREAD));
    userMessages().modify(USER, message.getId(), req).execute();
  }
}
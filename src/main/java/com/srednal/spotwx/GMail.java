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

import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.*;

public class GMail {
  /**
   * Application name
   */
  private static final String APPLICATION_NAME = "SpotWx";
  /**
   * Global instance of the JSON factory.
   */
  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
  /**
   * Directory to store authorization tokens for this application.
   */
  private static final String SECURITY_DIRECTORY_PATH = "security";
  private static final String CREDENTIALS_FILE = SECURITY_DIRECTORY_PATH + "/credentials.json";

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

  private static final String USER = "me";
  private static final String INBOX = "INBOX";
  private static final String UNREAD = "UNREAD";
  public static final String TO = "To";
  public static final String FROM = "From";
  public static final String REPLY_TO = "Reply-To";
  public static final String SUBJECT = "Subject";

  private final Logger logger = new Logger("GMail");

  private final Gmail service;

  private GMail(Gmail service) {
    this.service = service;
  }

  private static Credential getCredentials(final NetHttpTransport httpTransport) throws IOException {
    // Load client secrets.

    File credentialsFile = new java.io.File(CREDENTIALS_FILE);
    if (!credentialsFile.exists()) {
      throw new FileNotFoundException("File not found: " + CREDENTIALS_FILE);
    }
    FileInputStream in = new FileInputStream(credentialsFile);
    GoogleClientSecrets clientSecrets =
        GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

    // Build flow and trigger user authorization request.
    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
        httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
        .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(SECURITY_DIRECTORY_PATH)))
        .setAccessType("offline")
        .build();
    LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
    return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
  }

  public static GMail connect() throws IOException, GeneralSecurityException {
    // Build a new authorized API client service.
    final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    Gmail svc = new Gmail.Builder(httpTransport, JSON_FACTORY, getCredentials(httpTransport))
        .setApplicationName(APPLICATION_NAME)
        .build();

    return new GMail(svc);
  }

  public Map<String, String> getHeaders(Message message) {
    MessagePart p = message.getPayload();
    if (p == null) {
      logger.log("No headers, payload null");
      return Collections.emptyMap();
    }
    List<MessagePartHeader> msgHead = p.getHeaders();
    if (msgHead == null) {
      logger.log("No headers, headers null");
      return Collections.emptyMap();
    }

    HashMap<String, String> headers = new HashMap<>();
    msgHead.forEach(h -> headers.put(h.getName(), h.getValue()));
    return headers;
  }

  private boolean hasHeaders(Message message, String... headers) {
    Map<String, String> messageHeaders = getHeaders(message);
    return Arrays.stream(headers).allMatch(messageHeaders::containsKey);
  }

  private static final Set<String> seenMessageIds = new HashSet<>();

  public List<Message> getUnreadMessages(String... withHeaders) throws IOException {

    // Note this message contains only id and threadId
    List<Message> sparseMessages =
        userMessages().list(USER).setLabelIds(List.of(INBOX, UNREAD)).execute().getMessages();

    if (sparseMessages == null) sparseMessages = Collections.emptyList();

    // remove any already seen
    List<String> messageIds = sparseMessages.stream().map(Message::getId).toList();

    List<String> newMessageIds = messageIds.stream().filter(i -> !seenMessageIds.contains(i)).toList();

    // current ids we fetched have been seen
    seenMessageIds.clear();
    seenMessageIds.addAll(messageIds);

    if (newMessageIds.isEmpty()) logger.log("No new messages");

    List<Message> messages = new ArrayList<>();
    for (String id : newMessageIds) {
      // fetch the full message
      Message msg = userMessages().get(USER, id).execute();
      // see if it has spot lat/lon headers
      if (hasHeaders(msg, withHeaders)) messages.add(msg);
    }
    return messages;
  }

  private Gmail.Users.Messages userMessages() {
    return service.users().messages();
  }

  // Email sent from a spot has reply-to @spotxdev.com which doesnt exist
  // should go to @textmyspotx.com
  // per https://www.findmespot.com/en-us/support/spot-x/get-help/messaging/what-email-address-is-used-to-send-email-to-a-spot
  String fixTextAddress(String spotxdev) {
    return spotxdev.replace("spotxdev.com", "textmyspotx.com");
  }

  public void replyTo(Message msg, String subject, String body) throws MessagingException, IOException {
    Map<String, String> headers = getHeaders(msg);
    String from = headers.get(TO); // from the recipient
    String replyTo = headers.get(REPLY_TO);
    logger.log("Reply to %s with %s:%s".formatted(replyTo, subject, body));
    sendMessage(from, replyTo, subject, body);
  }

  public void sendMessage(String from, String to, String subject, String body) throws IOException, MessagingException {

    Properties props = new Properties();
    Session session = Session.getDefaultInstance(props, null);

    MimeMessage email = new MimeMessage(session);

    email.setFrom(new InternetAddress(from));
    email.addRecipient(RecipientType.TO, new InternetAddress(fixTextAddress(to)));
    email.setSubject(subject);
    email.setText(body);

    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    email.writeTo(buffer);
    byte[] bytes = buffer.toByteArray();
    String encodedEmail = Base64.encodeBase64URLSafeString(bytes);
    Message message = new Message();
    message.setRaw(encodedEmail);

    userMessages().send(USER, message).execute();
  }

  public void markRead(Message message) throws IOException {
    logger.log("Marking READ");
    userMessages().modify(USER, message.getId(),
        new ModifyMessageRequest().setRemoveLabelIds(Collections.singletonList(UNREAD))).execute();
  }
}
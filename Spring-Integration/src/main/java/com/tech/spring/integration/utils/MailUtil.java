package com.tech.spring.integration.utils;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.URLEncoder;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.search.FlagTerm;

import org.springframework.integration.mail.ImapMailReceiver;
import org.springframework.integration.mail.support.DefaultMailHeaderMapper;

public class MailUtil {

  public static ImapMailReceiver createImapMailReceiver(String imapURL, String username, String password) {
    String url = String.format(imapURL, URLEncoder.encode(username, UTF_8), URLEncoder.encode(password, UTF_8));
    ImapMailReceiver receiver = new ImapMailReceiver(url);
    receiver.setSimpleContent(true);

    Properties mailProperties = new Properties();
    mailProperties.put("mail.debug", "false");
    mailProperties.put("mail.imaps.connectionpoolsize", "5");
    mailProperties.put("mail.imaps.fetchsize", 4194304);
    mailProperties.put("mail.imaps.connectiontimeout", 15000);
    mailProperties.put("mail.imaps.timeout", 30000);
    receiver.setJavaMailProperties(mailProperties);
    receiver.setSearchTermStrategy((supportedFlags, folder) -> new FlagTerm(new Flags(Flags.Flag.SEEN), false));
    receiver.setAutoCloseFolder(false);
    receiver.setShouldDeleteMessages(false);
    receiver.setShouldMarkMessagesAsRead(true);
    receiver.setEmbeddedPartsAsBytes(false);
    receiver.setHeaderMapper(new DefaultMailHeaderMapper());
    return receiver;
  }

}

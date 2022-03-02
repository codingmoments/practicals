package com.tech.spring.integration.service;

import static com.tech.spring.integration.utils.MailUtil.createImapMailReceiver;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.mail.util.MimeMessageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.dsl.StandardIntegrationFlow;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.dsl.context.IntegrationFlowContext.IntegrationFlowRegistration;
import org.springframework.integration.mail.ImapMailReceiver;
import org.springframework.integration.mail.MailHeaders;
import org.springframework.integration.mail.dsl.Mail;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

@Service
public class MailReaderService {
  private static final Logger LOGGER = LoggerFactory.getLogger(MailReaderService.class);

  private static final String IMAP_URL = "imaps://%s:%s@imap.gmail.com/INBOX";
  private static final String[] USERNAMES = { "nileshwaani@gmail.com", "tech.nileshwani@gmail.com" };
  private static final String[] PASSWORDS = { "pass1", "pass2" };
  private static final String[] CONCERNS = { "100", "200" };
  private static final String CONCERN_ID = "maxxton-concern-id";

  private final IntegrationFlowContext integrationFlowContext;
  private final MessageChannel messageChannel;
  private final ThreadPoolTaskExecutor taskExecutor;

  public MailReaderService(IntegrationFlowContext integrationFlowContext, @Qualifier("imapMailChannel") MessageChannel messageChannel, @Qualifier("mailTaskExecutor") ThreadPoolTaskExecutor taskExecutor) {
    this.integrationFlowContext = integrationFlowContext;
    this.messageChannel = messageChannel;
    this.taskExecutor = taskExecutor;
  }

  @PostConstruct
  public void startReadingEmails() {
    registerImapFlow();
  }
  
  public void stopReadingEmails() {
    for (int i = 0; i < USERNAMES.length; i++) {
      IntegrationFlowRegistration flow = integrationFlowContext.getRegistrationById(USERNAMES[i]);
      if(flow != null) {
        flow.destroy();
      }
    }
  }

  private void registerImapFlow() {
    for (int i = 0; i < USERNAMES.length; i++) {
      final String username = USERNAMES[i];
      final String concern = CONCERNS[i];

      ImapMailReceiver imapMailReceiver = createImapMailReceiver(IMAP_URL, USERNAMES[i], PASSWORDS[i]);

      // @formatter:off
			StandardIntegrationFlow flow = IntegrationFlows.from(Mail.imapInboundAdapter(imapMailReceiver), 
					                                                 consumer -> consumer.autoStartup(true)
					                                             					      .poller(Pollers.fixedRate(5000)
					                                             					              .taskExecutor(taskExecutor)
					                                             					              .maxMessagesPerPoll(10)
					                                             					              .errorHandler(t -> LOGGER.error("Error while polling emails for address " + username, t))
					                                             					              )
					                                             					      
					                                             		)
			    .enrichHeaders(Map.of(CONCERN_ID, concern))
          .channel(messageChannel).get();
			// @formatter:on

      IntegrationFlowContext.IntegrationFlowRegistration existingFlow = integrationFlowContext.getRegistrationById(username);
      if (existingFlow != null) {
        existingFlow.destroy();
      }
      // register the new flow
      integrationFlowContext.registration(flow).id(username).useFlowIdAsPrefix().register();
    }
  }

  @ServiceActivator(inputChannel = "imapMailChannel")
  public void processMessage(Message<?> message) {
    Object concern = message.getHeaders().get(CONCERN_ID);
    String logMessage = null;
    try {
      Object payload = message.getPayload();
      if (payload instanceof MimeMultipart) {
        logMessage = getLogMimeMultipartMessage((MimeMultipart) payload);
      }
      else if (payload instanceof String) {
        logMessage = getLogStringMessage(message);
      }
      LOGGER.info("Received {} for concern {}", logMessage, concern);
      Thread.sleep(20000);
    }
    catch (Exception e) {
      LOGGER.error("Unknown exception occurred.", e);
    }
  }

  private String getLogStringMessage(Message<?> message) {
    try {
      Object sender = message.getHeaders().get(MailHeaders.FROM);
      Object subject = message.getHeaders().get(MailHeaders.SUBJECT);
      return "email from " + sender + " with subject " + subject;
    }
    catch (Exception e) {
      LOGGER.error("Exception in logStringMessage", e);
    }
    return "ERROR";
  }

  private String getLogMimeMultipartMessage(MimeMultipart payload) {
    try {
      LOGGER.debug("CCM-MessageID: payload = {}", payload);
      MimeMultipart multipart = payload;
      if (multipart.getParent() instanceof MimeMessage) {
        MimeMessageParser parser = new MimeMessageParser((MimeMessage) multipart.getParent()).parse();
        return "email from " + parser.getFrom() + " with subject " + parser.getSubject();
      }
    }
    catch (Exception e) {
      LOGGER.error("Exception in logMimeMultipartMessage", e);
    }
    return "ERROR";
  }

}

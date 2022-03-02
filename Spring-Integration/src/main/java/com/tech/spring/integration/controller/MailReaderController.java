package com.tech.spring.integration.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tech.spring.integration.service.MailReaderService;

@RestController
@RequestMapping("/mail/reader")
public class MailReaderController {

  private final MailReaderService mailReaderService;

  public MailReaderController(MailReaderService mailReaderService) {
    this.mailReaderService = mailReaderService;
  }

  @PostMapping("/start")
  public void startReadingEmails() {
    mailReaderService.startReadingEmails();
  }

  @PostMapping("/stop")
  public void stopReadingEmails() {
    mailReaderService.stopReadingEmails();
  }

}

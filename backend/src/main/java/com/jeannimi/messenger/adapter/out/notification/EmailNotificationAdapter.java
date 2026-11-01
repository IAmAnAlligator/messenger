package com.jeannimi.messenger.adapter.out.notification;

import com.jeannimi.messenger.application.notification.dto.MessageNotification;
import com.jeannimi.messenger.application.port.out.EmailNotificationPort;
import com.jeannimi.messenger.application.port.out.UserRepositoryPort;
import com.jeannimi.messenger.domain.user.User;
import com.jeannimi.messenger.domain.user.UserId;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationAdapter implements EmailNotificationPort {

  private final JavaMailSender mailSender;
  private final SpringTemplateEngine templateEngine;
  private final UserRepositoryPort userRepositoryPort;

  @Value("${app.mail.from}")
  private String from;

  @Override
  public void send(MessageNotification notification) {

    for (UserId recipientId : notification.recipientUserIds()) {

      if (notification.recipientUserIds().size() != 1) {
        throw new IllegalArgumentException(
            "Email notification must have exactly one recipient");
      }

      User recipient = userRepositoryPort.findById(recipientId)
          .orElseThrow(() -> new IllegalStateException(
              "Recipient not found: " + recipientId));

      sendEmail(
          recipient.getEmail().getValue(),
          notification);
    }
  }

  private void sendEmail(
      String recipientEmail,
      MessageNotification notification) {

    Context context = new Context();
    context.setVariable("senderUsername", notification.senderUsername());

    String html = templateEngine.process(
        "email/message-notification-private",
        context);

    log.debug("Generated email HTML: {}", html);

    MimeMessage message = mailSender.createMimeMessage();

    try {
      MimeMessageHelper helper = new MimeMessageHelper(
          message,
          true,
          StandardCharsets.UTF_8.name());

      helper.setFrom(from);
      helper.setTo(recipientEmail);
      helper.setSubject("You have a new message");
      helper.setText(html, true);

    } catch (MessagingException exception) {
      throw new MailPreparationException(
          "Failed to prepare email notification",
          exception);
    }

    mailSender.send(message);

    log.info(
        "Email notification sent: eventId={}, messageId={}",
        notification.eventId(),
        notification.messageId());
  }
}
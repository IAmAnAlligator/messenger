package com.jeannimi.messenger.adapter.out.notification;

import com.jeannimi.messenger.application.notification.dto.MessageNotification;
import com.jeannimi.messenger.application.port.out.EmailNotificationPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailNotificationAdapter implements EmailNotificationPort {

  @Override
  public void send(MessageNotification notification) {
    log.info(
        "Email notification requested: eventId={}, messageId={}, recipients={}",
        notification.eventId(),
        notification.messageId(),
        notification.recipientUserIds().size());
  }
}

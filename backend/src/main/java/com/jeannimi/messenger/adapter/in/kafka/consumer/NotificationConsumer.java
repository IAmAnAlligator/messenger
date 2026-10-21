package com.jeannimi.messenger.adapter.in.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeannimi.messenger.adapter.in.kafka.mapper.KafkaEventToApplicationEventMapper;
import com.jeannimi.messenger.adapter.kafka.envelope.KafkaEventEnvelope;
import com.jeannimi.messenger.adapter.kafka.event.MessageSentEvent;
import com.jeannimi.messenger.adapter.out.kafka.KafkaTopics;
import com.jeannimi.messenger.application.event.MessageCreatedEvent;
import com.jeannimi.messenger.application.notification.dto.MessageNotification;
import com.jeannimi.messenger.application.port.in.MessageNotificationUseCase;
import com.jeannimi.messenger.domain.event.EventId;
import com.jeannimi.messenger.domain.user.UserId;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationConsumer {

  private final ObjectMapper objectMapper;
  private final KafkaEventToApplicationEventMapper eventMapper;
  private final MessageNotificationUseCase messageNotificationUseCase;

  @KafkaListener(topics = KafkaTopics.CHAT_MESSAGES, groupId = "notification-group")
  public void consumeMessage(String payload, Acknowledgment ack) {
    try {
      KafkaEventEnvelope envelope = objectMapper.readValue(payload, KafkaEventEnvelope.class);

      Objects.requireNonNull(envelope.eventId(), "Kafka eventId must not be null");

      MessageSentEvent event = objectMapper.treeToValue(envelope.payload(), MessageSentEvent.class);

      MessageCreatedEvent applicationEvent = eventMapper.toApplicationEvent(event);

      UserId senderId = applicationEvent.message().sender().id();

      MessageNotification notification =
          new MessageNotification(
              new EventId(envelope.eventId()),
              applicationEvent.message().id(),
              applicationEvent.message().chatId(),
              senderId,
              applicationEvent.message().sender().username(),
              applicationEvent.recipientUserIds().stream()
                  .filter(userId -> !userId.equals(senderId))
                  .toList());

      messageNotificationUseCase.notifyMessageReceived(notification);

      ack.acknowledge();
    } catch (Exception e) {
      log.error("Notification event processing failed", e);
      throw new RuntimeException("Notification event processing failed", e);
    }
  }
}

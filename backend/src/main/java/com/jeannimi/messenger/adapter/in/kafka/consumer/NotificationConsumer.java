package com.jeannimi.messenger.adapter.in.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeannimi.messenger.adapter.in.kafka.exception.InvalidKafkaEventException;
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

  @KafkaListener(
      topics = KafkaTopics.CHAT_MESSAGES,
      groupId = "notification-group",
      containerFactory = "chatMessagesKafkaListenerContainerFactory")
  public void consumeMessage(String payload, Acknowledgment ack) {

    KafkaEventEnvelope envelope = deserializeEnvelope(payload);

    if (envelope.eventId() == null) {
      throw new InvalidKafkaEventException("Notification event must contain eventId");
    }

    MessageSentEvent event = deserializeEvent(envelope);

    MessageCreatedEvent applicationEvent = eventMapper.toApplicationEvent(event);

    if (applicationEvent == null
        || applicationEvent.message() == null
        || applicationEvent.message().sender() == null
        || applicationEvent.message().sender().id() == null) {
      throw new InvalidKafkaEventException(
          "Notification event must contain message and sender information");
    }

    UserId senderId = applicationEvent.message().sender().id();

    MessageNotification notification =
        new MessageNotification(
            new EventId(envelope.eventId()),
            applicationEvent.message().id(),
            applicationEvent.message().chatId(),
            senderId,
            applicationEvent.message().sender().username(),
            applicationEvent.recipientUserIds().stream()
                .filter(Objects::nonNull)
                .filter(userId -> !userId.equals(senderId))
                .toList());

    messageNotificationUseCase.notifyMessageReceived(notification);

    ack.acknowledge();

    log.debug("Notification event processed successfully: eventId={}", envelope.eventId());
  }

  private KafkaEventEnvelope deserializeEnvelope(String payload) {

    if (payload == null || payload.isBlank()) {
      throw new InvalidKafkaEventException("Notification event payload must not be empty");
    }

    try {
      KafkaEventEnvelope envelope = objectMapper.readValue(payload, KafkaEventEnvelope.class);

      if (envelope == null) {
        throw new InvalidKafkaEventException("Notification event envelope must not be null");
      }

      if (envelope.payload() == null || envelope.payload().isNull()) {
        throw new InvalidKafkaEventException("Notification event payload must not be null");
      }

      return envelope;

    } catch (JsonProcessingException e) {
      throw new InvalidKafkaEventException("Failed to deserialize notification event envelope", e);
    }
  }

  private MessageSentEvent deserializeEvent(KafkaEventEnvelope envelope) {

    try {
      MessageSentEvent event = objectMapper.treeToValue(envelope.payload(), MessageSentEvent.class);

      if (event == null) {
        throw new InvalidKafkaEventException("Notification event must not be null");
      }

      return event;

    } catch (JsonProcessingException e) {
      throw new InvalidKafkaEventException("Failed to deserialize notification event", e);
    }
  }
}

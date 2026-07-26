package com.jeannimi.messenger.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeannimi.messenger.message.dto.MessageDto;
import com.jeannimi.messenger.kafka.event.MessageDeletedEvent;
import com.jeannimi.messenger.kafka.event.MessageReadEvent;
import com.jeannimi.messenger.kafka.event.MessageSentEvent;
import com.jeannimi.messenger.kafka.event.EventType;
import com.jeannimi.messenger.message.entity.MessageStatus;
import com.jeannimi.messenger.message.entity.ProcessedMessage;
import com.jeannimi.messenger.kafka.handler.ChatEventHandler;
import com.jeannimi.messenger.kafka.envelope.KafkaEventEnvelope;
import com.jeannimi.messenger.kafka.KafkaTopics;
import com.jeannimi.messenger.message.repository.ProcessedMessageRepository;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventConsumer {

  private final ObjectMapper objectMapper;

  private final SimpMessagingTemplate messagingTemplate;

  private final ProcessedMessageRepository processedRepository;

  private final List<ChatEventHandler> handlers;

  private Map<EventType, ChatEventHandler> handlerMap;

  @PostConstruct
  private void init() {

    handlerMap =
        handlers.stream()
            .collect(Collectors.toMap(ChatEventHandler::supports, Function.identity()));
  }

  /*
   =========================
       CHAT MESSAGES
   =========================
  */

  @KafkaListener(topics = KafkaTopics.CHAT_MESSAGES, groupId = "chat-ws-group")
  public void consumeMessage(String payload, Acknowledgment ack) {

    process(
        payload,
        ack,
        MessageSentEvent.class,
        event -> {


          MessageDto dto =
              new MessageDto(
                  event.messageId(),
                  event.chatId(),
                  event.sender(),
                  event.content(),
                  event.createdAt(),
                  MessageStatus.SENT
              );


          messagingTemplate.convertAndSend(
              "/topic/chat/" + dto.chatId(),
              dto
          );
        });
  }

  /*
   =========================
       READ EVENTS
   =========================
  */

  @KafkaListener(topics = KafkaTopics.CHAT_READ, groupId = "chat-ws-group")
  public void consumeRead(String payload, Acknowledgment ack) {

    process(
        payload,
        ack,
        MessageReadEvent.class,
        event ->
            messagingTemplate.convertAndSend("/topic/chat/"
                + event.chatId(), event));
  }

  /*
   =========================
       MESSAGE DELETE
   =========================
  */

  @KafkaListener(topics = KafkaTopics.CHAT_MESSAGE_DELETED, groupId = "chat-ws-group")
  public void consumeDelete(String payload, Acknowledgment ack) {

    process(
        payload,
        ack,
        MessageDeletedEvent.class,
        event ->
            messagingTemplate.convertAndSend(
                "/topic/chat/" + event.chatId(), event
            )
    );
  }

  /*
   =========================
       CHAT EVENTS
   =========================
  */

  @KafkaListener(topics = KafkaTopics.CHAT_EVENTS, groupId = "chat-ws-group")
  public void consumeChatEvents(String payload, Acknowledgment ack) {

    processEnvelope(
        payload,
        ack,
        envelope -> {

          EventType eventType = EventType.valueOf(envelope.eventType());

          ChatEventHandler handler = handlerMap.get(eventType);

          if (handler == null) {

            log.warn("No handler found for event {}", eventType);

            return;
          }

          handler.handle(envelope.payload());
        });
  }

/*
 =========================
     COMMON PROCESSOR
 =========================
*/

  private void processEnvelope(
      String payload,
      Acknowledgment ack,
      Consumer<KafkaEventEnvelope> consumer) {

    try {

      KafkaEventEnvelope envelope =
          objectMapper.readValue(payload, KafkaEventEnvelope.class);

      UUID eventId = envelope.eventId();

      try {

        processedRepository.save(ProcessedMessage.of(eventId));

      } catch (DataIntegrityViolationException e) {

        log.info("Duplicate event skipped {}", eventId);

        ack.acknowledge();

        return;
      }

      consumer.accept(envelope);

      ack.acknowledge();

    } catch (Exception e) {

      log.error("Kafka processing failed", e);

      throw new RuntimeException(e);
    }
  }

  private <T> void process(
      String payload,
      Acknowledgment ack,
      Class<T> type,
      EventHandler<T> handler) {

    processEnvelope(
        payload,
        ack,
        envelope -> {

          T event = null;
          try {
            event = objectMapper.treeToValue(envelope.payload(), type);
          } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
          }

          handler.handle(event);
        });
  }

  @FunctionalInterface
  private interface EventHandler<T> {

    void handle(T event);
  }
}

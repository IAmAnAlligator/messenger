package com.jeannimi.messenger.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeannimi.messenger.dto.ChatCreatedEvent;
import com.jeannimi.messenger.dto.ChatDeletedEvent;
import com.jeannimi.messenger.dto.DeleteMessageDto;
import com.jeannimi.messenger.dto.MessageDeletedEvent;
import com.jeannimi.messenger.dto.MessageDto;
import com.jeannimi.messenger.entity.ProcessedMessage;
import com.jeannimi.messenger.repository.ProcessedMessageRepository;
import jakarta.transaction.Transactional;
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
public class ChatEventConsumer {

  private final ObjectMapper objectMapper;

  private final SimpMessagingTemplate messagingTemplate;

  private final ProcessedMessageRepository processedRepository;

  /*
  1. Получаем JSON из Kafka
  2. Парсим в DTO
  3. Проверяем идемпотентность
  4. Отправляем в WebSocket
  5. Фиксируем обработку
  6. Commit offset
  */

  @Transactional
  @KafkaListener(topics = "chat.messages", groupId = "chat-ws-group")
  public void consumeMessage(String payload, Acknowledgment ack) throws JsonProcessingException {

    long started = System.currentTimeMillis();

    log.info("[START] payload={}", payload);

    try {

      MessageDto dto = objectMapper.readValue(payload, MessageDto.class);

      if (dto.getId() == null) {
        throw new IllegalStateException("message id is null");
      }

      log.info("[PARSED] id={}, chat={}", dto.getId(), dto.getChatId());

      boolean duplicate = false;

      try {

        processedRepository.save(new ProcessedMessage(dto.getId()));

      } catch (DataIntegrityViolationException e) {

        duplicate = true;

        log.info("[SKIP DUPLICATE] message={}", dto.getId());
      }

      // Отправляем только новые сообщения
      if (!duplicate) {

        messagingTemplate.convertAndSend("/topic/chat/" + dto.getChatId(), dto);

        log.info("[WS SENT] id={}", dto.getId());
      }

      // Kafka подтверждаем всегда
      ack.acknowledge();

    } catch (Exception e) {

      log.error("[FAILED] payload={}", payload, e);

      throw e;

    } finally {

      log.info("[END] elapsed={}ms", System.currentTimeMillis() - started);
    }
  }

  @KafkaListener(topics = "chat.read", groupId = "chat-ws-group")
  public void consumeRead(String payload, Acknowledgment ack) {

    try {

      MessageDto dto = objectMapper.readValue(payload, MessageDto.class);

      messagingTemplate.convertAndSend("/topic/chat/" + dto.getChatId(), dto);

      ack.acknowledge();

    } catch (Exception e) {

      log.error("Read delivery failed", e);

      throw new RuntimeException(e);
    }
  }

  @KafkaListener(topics = "chat.delete", groupId = "chat-ws-group")
  public void consumeDelete(String payload, Acknowledgment ack) {

    try {

      DeleteMessageDto dto = objectMapper.readValue(payload, DeleteMessageDto.class);

      messagingTemplate.convertAndSend(
          "/topic/chat/" + dto.getChatId(),
          new MessageDeletedEvent("MESSAGE_DELETED", dto.getId()));

      ack.acknowledge();

    } catch (Exception e) {

      log.error("Delete delivery failed", e);

      throw new RuntimeException(e);
    }
  }

  @KafkaListener(topics = "chat.deleted", groupId = "chat-ws-group")
  public void consumeChatDeleted(String payload, Acknowledgment ack) {

    try {

      ChatDeletedEvent dto = objectMapper.readValue(payload, ChatDeletedEvent.class);

      // Для ChatPage
      messagingTemplate.convertAndSend("/topic/chat/" + dto.getChatId(), dto);

      // Для ChatsPage
      messagingTemplate.convertAndSend("/topic/chat.deleted", dto);

      ack.acknowledge();

    } catch (Exception e) {

      throw new RuntimeException(e);
    }
  }

  @KafkaListener(topics = "chat.created", groupId = "chat-ws-group")
  public void consumeChatCreated(String payload, Acknowledgment ack) {

    try {

      ChatCreatedEvent dto = objectMapper.readValue(payload, ChatCreatedEvent.class);

      messagingTemplate.convertAndSend("/topic/chat.created", dto);

      ack.acknowledge();

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}

package com.jeannimi.messenger.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeannimi.messenger.dto.MessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

  /*

  1. получает raw JSON
  2. парсит в DTO
  3. логика обработки
  4. отправка в WebSocket
  5. commit offset
  6. error handling

   */

  @KafkaListener(topics = "chat.messages", groupId = "chat-ws-group")
  public void consumeMessage(String payload, Acknowledgment ack) throws JsonProcessingException {

    long started = System.currentTimeMillis();

    log.info("[START] payload={}", payload);

    try {

      MessageDto dto = objectMapper.readValue(payload, MessageDto.class);

      log.info("[PARSED] id={}, chat={}", dto.getId(), dto.getChatId());

      messagingTemplate.convertAndSend("/topic/chat/" + dto.getChatId(), dto);

      log.info("[WS SENT] id={}", dto.getId());

      //      throw new RuntimeException(
      //          "simulate failure"
      //      );

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

      messagingTemplate.convertAndSend(
          "/topic/chat/" + dto.getChatId(),
          dto
      );

      ack.acknowledge();

    } catch (Exception e) {

      log.error("Read delivery failed", e);

      throw new RuntimeException(e);
    }
  }
}

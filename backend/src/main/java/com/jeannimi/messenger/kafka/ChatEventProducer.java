package com.jeannimi.messenger.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeannimi.messenger.dto.DeleteMessageDto;
import com.jeannimi.messenger.dto.MessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatEventProducer {

  // Отправка сообщений в Kafka
  private final KafkaTemplate<String, String> kafkaTemplate;

  private final ObjectMapper objectMapper;

  private static final String MESSAGE_TOPIC = "chat.messages";

  private static final String READ_TOPIC = "chat.read";

  private static final String DELETE_TOPIC = "chat.delete";

  /*

  Что происходит:
  DTO → JSON
  отправка в Kafka topic chat.messages
  key = chatId
  📌 Зачем key = chatId?

  👉 Kafka гарантирует:

  все сообщения одного chatId попадут в одну partition

  Это важно для:

  сохранения порядка сообщений в чате
  корректного realtime UI

   */
  public void sendMessage(MessageDto dto) {

    try {

      kafkaTemplate.send(
          MESSAGE_TOPIC, dto.getChatId().toString(), objectMapper.writeValueAsString(dto));

    } catch (Exception e) {

      throw new RuntimeException(e);
    }
  }

  public void readMessage(MessageDto dto) {

    log.info("SEND READ {}", dto.getId());

    try {

      kafkaTemplate.send(
          READ_TOPIC, dto.getChatId().toString(), objectMapper.writeValueAsString(dto));

    } catch (Exception e) {

      throw new RuntimeException(e);
    }
  }

  public void deleteMessage(DeleteMessageDto dto) {

    try {

      log.info("DELETE MESSAGE {}", dto.getId());

      kafkaTemplate
          .send(DELETE_TOPIC, dto.getChatId().toString(), objectMapper.writeValueAsString(dto))
          .get();

    } catch (Exception e) {

      throw new RuntimeException("Kafka delete publish failed", e);
    }
  }
}

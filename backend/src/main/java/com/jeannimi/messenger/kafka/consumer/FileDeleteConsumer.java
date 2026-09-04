package com.jeannimi.messenger.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeannimi.messenger.kafka.KafkaTopics;
import com.jeannimi.messenger.kafka.envelope.KafkaEventEnvelope;
import com.jeannimi.messenger.kafka.event.FileDeletionRequestedEvent;
import com.jeannimi.messenger.message.entity.ProcessedMessage;
import com.jeannimi.messenger.application.port.out.ProcessedMessageRepositoryPort;
import com.jeannimi.messenger.message.storage.FileStorageService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileDeleteConsumer {

  private final ObjectMapper objectMapper;
  private final FileStorageService fileStorageService;
  private final ProcessedMessageRepositoryPort processedRepository;

  @KafkaListener(topics = KafkaTopics.FILE_DELETE, groupId = "file-storage-group")
  public void consume(String payload, Acknowledgment ack) {

    try {

      KafkaEventEnvelope envelope = objectMapper.readValue(payload, KafkaEventEnvelope.class);

      UUID eventId = envelope.eventId();

      FileDeletionRequestedEvent event =
          objectMapper.treeToValue(envelope.payload(), FileDeletionRequestedEvent.class);

      /*
       * Проверяем duplicate ДО удаления.
       *
       * Но ProcessedMessage создаём ПОСЛЕ успешного удаления.
       */
      if (processedRepository.existsByEventId(eventId)) {
        log.info("Duplicate file deletion event skipped: {}", eventId);

        ack.acknowledge();
        return;
      }

      /*
       * delete должен быть idempotent.
       *
       * Если файла уже нет — это успешный результат.
       */
      fileStorageService.delete(event.storageFileName());

      log.info("[FILE DELETE] deleted file={}", event.storageFileName());

      /*
       * Только после успешного удаления
       * фиксируем event как обработанный.
       */
      try {

        processedRepository.save(ProcessedMessage.of(eventId));

      } catch (DataIntegrityViolationException e) {

        /*
         * Другой consumer/thread мог успеть
         * обработать тот же event.
         *
         * Само удаление уже выполнено,
         * поэтому это не ошибка.
         */
        log.info("File deletion event already processed: {}", eventId);
      }

      ack.acknowledge();

    } catch (JsonProcessingException e) {

      log.error("Failed to deserialize file deletion event", e);

      throw new RuntimeException(e);

    } catch (Exception e) {

      log.error("File deletion failed", e);

      /*
       * ACK не вызываем.
       * Kafka сможет повторить сообщение.
       */
      throw new RuntimeException(e);
    }
  }
}

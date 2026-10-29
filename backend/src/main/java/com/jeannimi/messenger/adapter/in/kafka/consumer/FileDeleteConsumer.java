package com.jeannimi.messenger.adapter.in.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeannimi.messenger.adapter.in.kafka.exception.InvalidKafkaEventException;
import com.jeannimi.messenger.adapter.kafka.envelope.KafkaEventEnvelope;
import com.jeannimi.messenger.adapter.out.kafka.KafkaTopics;
import com.jeannimi.messenger.application.event.FileDeletionRequestedEvent;
import com.jeannimi.messenger.application.event.service.ProcessedEventService;
import com.jeannimi.messenger.application.port.out.FileStoragePort;
import com.jeannimi.messenger.domain.event.EventId;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileDeleteConsumer {

  private final ObjectMapper objectMapper;
  private final FileStoragePort fileStoragePort;
  private final ProcessedEventService processedEventService;

  @KafkaListener(
      topics = KafkaTopics.FILE_DELETE,
      groupId = "file-storage-group",
      containerFactory = "fileDeleteKafkaListenerContainerFactory")
  public void consume(String payload, Acknowledgment ack) {

    KafkaEventEnvelope envelope = deserializeEnvelope(payload);

    UUID eventUuid = envelope.eventId();

    if (eventUuid == null) {
      throw new InvalidKafkaEventException("File deletion event must contain eventId");
    }

    EventId eventId = new EventId(eventUuid);

    FileDeletionRequestedEvent event = deserializeEvent(envelope);

    String storageFileName = event.storageFileName();

    if (storageFileName == null || storageFileName.isBlank()) {
      throw new InvalidKafkaEventException("File deletion event must contain storageFileName");
    }

    if (processedEventService.existsByEventId(eventId)) {
      log.info("Duplicate file deletion event skipped: {}", eventId);

      ack.acknowledge();
      return;
    }

    // Delete the file before registering the event.
    fileStoragePort.delete(storageFileName);

    processedEventService.register(eventId);

    log.info("[FILE DELETE] Successfully processed eventId={}, file={}", eventId, storageFileName);

    ack.acknowledge();
  }

  private KafkaEventEnvelope deserializeEnvelope(String rawMessage) {

    if (rawMessage == null || rawMessage.isBlank()) {
      throw new InvalidKafkaEventException("File deletion message must not be empty");
    }

    try {
      KafkaEventEnvelope envelope = objectMapper.readValue(rawMessage, KafkaEventEnvelope.class);

      if (envelope == null) {
        throw new InvalidKafkaEventException("File deletion envelope must not be null");
      }

      if (envelope.payload() == null || envelope.payload().isNull()) {
        throw new InvalidKafkaEventException("File deletion event payload must not be null");
      }

      return envelope;

    } catch (JsonProcessingException e) {
      throw new InvalidKafkaEventException("Failed to deserialize file deletion envelope", e);
    }
  }

  private FileDeletionRequestedEvent deserializeEvent(KafkaEventEnvelope envelope) {

    try {
      FileDeletionRequestedEvent event =
          objectMapper.treeToValue(envelope.payload(), FileDeletionRequestedEvent.class);

      if (event == null) {
        throw new InvalidKafkaEventException("File deletion event must not be null");
      }

      return event;

    } catch (JsonProcessingException e) {
      throw new InvalidKafkaEventException("Failed to deserialize file deletion event", e);
    }
  }
}

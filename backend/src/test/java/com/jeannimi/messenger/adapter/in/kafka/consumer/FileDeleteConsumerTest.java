package com.jeannimi.messenger.adapter.in.kafka.consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeannimi.messenger.adapter.in.kafka.exception.InvalidKafkaEventException;
import com.jeannimi.messenger.adapter.kafka.envelope.KafkaEventEnvelope;
import com.jeannimi.messenger.application.event.FileDeletionRequestedEvent;
import com.jeannimi.messenger.application.event.service.ProcessedEventService;
import com.jeannimi.messenger.application.port.out.FileStoragePort;
import com.jeannimi.messenger.domain.event.EventId;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.kafka.support.Acknowledgment;

class FileDeleteConsumerTest {

  private static final String RAW_MESSAGE = "{\"event\":\"file-deletion\"}";
  private static final String STORAGE_FILE_NAME = "file-123.enc";

  private static final UUID EVENT_UUID = UUID.fromString("018f1234-5678-7abc-8def-123456789001");

  private static final EventId EVENT_ID = new EventId(EVENT_UUID);

  private ObjectMapper objectMapper;
  private FileStoragePort fileStoragePort;
  private ProcessedEventService processedEventService;
  private Acknowledgment acknowledgment;

  private KafkaEventEnvelope envelope;
  private JsonNode payloadNode;
  private FileDeletionRequestedEvent event;

  private FileDeleteConsumer consumer;

  @BeforeEach
  void setUp() {
    objectMapper = mock(ObjectMapper.class);
    fileStoragePort = mock(FileStoragePort.class);
    processedEventService = mock(ProcessedEventService.class);
    acknowledgment = mock(Acknowledgment.class);

    envelope = mock(KafkaEventEnvelope.class);
    payloadNode = mock(JsonNode.class);
    event = mock(FileDeletionRequestedEvent.class);

    consumer = new FileDeleteConsumer(objectMapper, fileStoragePort, processedEventService);
  }

  @Test
  void consume_shouldDeleteFileRegisterEventAndAcknowledge() throws Exception {
    prepareEnvelope();
    prepareEvent();

    when(processedEventService.existsByEventId(EVENT_ID)).thenReturn(false);

    consumer.consume(RAW_MESSAGE, acknowledgment);

    InOrder inOrder = inOrder(fileStoragePort, processedEventService, acknowledgment);

    inOrder.verify(fileStoragePort).delete(STORAGE_FILE_NAME);
    inOrder.verify(processedEventService).register(EVENT_ID);
    inOrder.verify(acknowledgment).acknowledge();
  }

  @Test
  void consume_shouldSkipAlreadyProcessedEvent() throws Exception {
    prepareEnvelope();
    prepareEvent();

    when(processedEventService.existsByEventId(EVENT_ID)).thenReturn(true);

    consumer.consume(RAW_MESSAGE, acknowledgment);

    verify(processedEventService).existsByEventId(EVENT_ID);

    verify(fileStoragePort, never()).delete(STORAGE_FILE_NAME);
    verify(processedEventService, never()).register(EVENT_ID);

    verify(acknowledgment).acknowledge();
  }

  @Test
  void consume_shouldFailWhenMessageIsNull() {
    InvalidKafkaEventException exception =
        assertThrows(
            InvalidKafkaEventException.class, () -> consumer.consume(null, acknowledgment));

    assertEquals("File deletion message must not be empty", exception.getMessage());

    verifyNoInteractions(objectMapper);
    verifyNoInteractions(fileStoragePort);
    verifyNoInteractions(processedEventService);
    verifyNoInteractions(acknowledgment);
  }

  @Test
  void consume_shouldFailWhenMessageIsBlank() {
    InvalidKafkaEventException exception =
        assertThrows(
            InvalidKafkaEventException.class, () -> consumer.consume("   ", acknowledgment));

    assertEquals("File deletion message must not be empty", exception.getMessage());

    verifyNoInteractions(objectMapper);
    verifyNoInteractions(fileStoragePort);
    verifyNoInteractions(processedEventService);
    verifyNoInteractions(acknowledgment);
  }

  @Test
  void consume_shouldFailWhenEnvelopeIsNull() throws Exception {
    when(objectMapper.readValue(RAW_MESSAGE, KafkaEventEnvelope.class)).thenReturn(null);

    InvalidKafkaEventException exception =
        assertThrows(
            InvalidKafkaEventException.class, () -> consumer.consume(RAW_MESSAGE, acknowledgment));

    assertEquals("File deletion envelope must not be null", exception.getMessage());

    verifyNoInteractions(fileStoragePort);
    verifyNoInteractions(processedEventService);
    verifyNoInteractions(acknowledgment);
  }

  @Test
  void consume_shouldFailWhenEnvelopePayloadIsNull() throws Exception {
    when(objectMapper.readValue(RAW_MESSAGE, KafkaEventEnvelope.class)).thenReturn(envelope);

    when(envelope.payload()).thenReturn(null);

    InvalidKafkaEventException exception =
        assertThrows(
            InvalidKafkaEventException.class, () -> consumer.consume(RAW_MESSAGE, acknowledgment));

    assertEquals("File deletion event payload must not be null", exception.getMessage());

    verifyNoInteractions(fileStoragePort);
    verifyNoInteractions(processedEventService);
    verifyNoInteractions(acknowledgment);
  }

  @Test
  void consume_shouldFailWhenEnvelopePayloadIsJsonNull() throws Exception {
    when(objectMapper.readValue(RAW_MESSAGE, KafkaEventEnvelope.class)).thenReturn(envelope);

    when(envelope.payload()).thenReturn(payloadNode);
    when(payloadNode.isNull()).thenReturn(true);

    InvalidKafkaEventException exception =
        assertThrows(
            InvalidKafkaEventException.class, () -> consumer.consume(RAW_MESSAGE, acknowledgment));

    assertEquals("File deletion event payload must not be null", exception.getMessage());

    verifyNoInteractions(fileStoragePort);
    verifyNoInteractions(processedEventService);
    verifyNoInteractions(acknowledgment);
  }

  @Test
  void consume_shouldFailWhenEnvelopeJsonIsInvalid() throws Exception {
    JsonProcessingException cause = jsonProcessingException("Invalid envelope JSON");

    when(objectMapper.readValue(RAW_MESSAGE, KafkaEventEnvelope.class)).thenThrow(cause);

    InvalidKafkaEventException exception =
        assertThrows(
            InvalidKafkaEventException.class, () -> consumer.consume(RAW_MESSAGE, acknowledgment));

    assertEquals("Failed to deserialize file deletion envelope", exception.getMessage());
    assertEquals(cause, exception.getCause());

    verifyNoInteractions(fileStoragePort);
    verifyNoInteractions(processedEventService);
    verifyNoInteractions(acknowledgment);
  }

  @Test
  void consume_shouldFailWhenEventIdIsNull() throws Exception {
    when(objectMapper.readValue(RAW_MESSAGE, KafkaEventEnvelope.class)).thenReturn(envelope);

    when(envelope.eventId()).thenReturn(null);
    when(envelope.payload()).thenReturn(payloadNode);

    InvalidKafkaEventException exception =
        assertThrows(
            InvalidKafkaEventException.class, () -> consumer.consume(RAW_MESSAGE, acknowledgment));

    assertEquals("File deletion event must contain eventId", exception.getMessage());

    verify(objectMapper, never()).treeToValue(payloadNode, FileDeletionRequestedEvent.class);

    verifyNoInteractions(fileStoragePort);
    verifyNoInteractions(processedEventService);
    verifyNoInteractions(acknowledgment);
  }

  @Test
  void consume_shouldFailWhenEventCannotBeDeserialized() throws Exception {
    prepareEnvelope();

    JsonProcessingException cause = jsonProcessingException("Invalid event payload");

    when(objectMapper.treeToValue(payloadNode, FileDeletionRequestedEvent.class)).thenThrow(cause);

    InvalidKafkaEventException exception =
        assertThrows(
            InvalidKafkaEventException.class, () -> consumer.consume(RAW_MESSAGE, acknowledgment));

    assertEquals("Failed to deserialize file deletion event", exception.getMessage());
    assertEquals(cause, exception.getCause());

    verifyNoInteractions(fileStoragePort);
    verifyNoInteractions(processedEventService);
    verifyNoInteractions(acknowledgment);
  }

  @Test
  void consume_shouldFailWhenDeserializedEventIsNull() throws Exception {
    prepareEnvelope();

    when(objectMapper.treeToValue(payloadNode, FileDeletionRequestedEvent.class)).thenReturn(null);

    InvalidKafkaEventException exception =
        assertThrows(
            InvalidKafkaEventException.class, () -> consumer.consume(RAW_MESSAGE, acknowledgment));

    assertEquals("File deletion event must not be null", exception.getMessage());

    verifyNoInteractions(fileStoragePort);
    verifyNoInteractions(processedEventService);
    verifyNoInteractions(acknowledgment);
  }

  @Test
  void consume_shouldFailWhenStorageFileNameIsNull() throws Exception {
    prepareEnvelope();

    when(objectMapper.treeToValue(payloadNode, FileDeletionRequestedEvent.class)).thenReturn(event);

    when(event.storageFileName()).thenReturn(null);

    InvalidKafkaEventException exception =
        assertThrows(
            InvalidKafkaEventException.class, () -> consumer.consume(RAW_MESSAGE, acknowledgment));

    assertEquals("File deletion event must contain storageFileName", exception.getMessage());

    verifyNoInteractions(fileStoragePort);
    verifyNoInteractions(processedEventService);
    verifyNoInteractions(acknowledgment);
  }

  @Test
  void consume_shouldFailWhenStorageFileNameIsBlank() throws Exception {
    prepareEnvelope();

    when(objectMapper.treeToValue(payloadNode, FileDeletionRequestedEvent.class)).thenReturn(event);

    when(event.storageFileName()).thenReturn("   ");

    InvalidKafkaEventException exception =
        assertThrows(
            InvalidKafkaEventException.class, () -> consumer.consume(RAW_MESSAGE, acknowledgment));

    assertEquals("File deletion event must contain storageFileName", exception.getMessage());

    verifyNoInteractions(fileStoragePort);
    verifyNoInteractions(processedEventService);
    verifyNoInteractions(acknowledgment);
  }

  @Test
  void consume_shouldNotRegisterOrAcknowledgeWhenFileDeletionFails() throws Exception {

    prepareEnvelope();
    prepareEvent();

    when(processedEventService.existsByEventId(EVENT_ID)).thenReturn(false);

    RuntimeException cause = new RuntimeException("Storage failure");

    doThrow(cause).when(fileStoragePort).delete(STORAGE_FILE_NAME);

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> consumer.consume(RAW_MESSAGE, acknowledgment));

    assertEquals(cause, exception);

    verify(fileStoragePort).delete(STORAGE_FILE_NAME);

    verify(processedEventService, never()).register(EVENT_ID);
    verify(acknowledgment, never()).acknowledge();
  }

  @Test
  void consume_shouldNotAcknowledgeWhenEventRegistrationFails() throws Exception {

    prepareEnvelope();
    prepareEvent();

    when(processedEventService.existsByEventId(EVENT_ID)).thenReturn(false);

    RuntimeException cause = new RuntimeException("Event registration failure");

    doThrow(cause).when(processedEventService).register(EVENT_ID);

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> consumer.consume(RAW_MESSAGE, acknowledgment));

    assertEquals(cause, exception);

    InOrder inOrder = inOrder(fileStoragePort, processedEventService, acknowledgment);

    inOrder.verify(fileStoragePort).delete(STORAGE_FILE_NAME);
    inOrder.verify(processedEventService).register(EVENT_ID);

    verify(acknowledgment, never()).acknowledge();
  }

  private void prepareEnvelope() throws Exception {
    when(objectMapper.readValue(RAW_MESSAGE, KafkaEventEnvelope.class)).thenReturn(envelope);

    when(envelope.eventId()).thenReturn(EVENT_UUID);
    when(envelope.payload()).thenReturn(payloadNode);
  }

  private void prepareEvent() throws Exception {
    prepareEnvelope();

    when(objectMapper.treeToValue(payloadNode, FileDeletionRequestedEvent.class)).thenReturn(event);

    when(event.storageFileName()).thenReturn(STORAGE_FILE_NAME);
  }

  private JsonProcessingException jsonProcessingException(String message) {
    return new JsonProcessingException(message) {};
  }
}

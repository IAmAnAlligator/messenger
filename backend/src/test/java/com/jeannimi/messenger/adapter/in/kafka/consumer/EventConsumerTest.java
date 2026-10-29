package com.jeannimi.messenger.adapter.in.kafka.consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
import com.jeannimi.messenger.adapter.in.kafka.handler.ChatEventHandler;
import com.jeannimi.messenger.adapter.in.kafka.mapper.KafkaEventToApplicationEventMapper;
import com.jeannimi.messenger.adapter.kafka.envelope.KafkaEventEnvelope;
import com.jeannimi.messenger.adapter.kafka.event.MessageSentEvent;
import com.jeannimi.messenger.application.event.EventType;
import com.jeannimi.messenger.application.event.MessageCreatedEvent;
import com.jeannimi.messenger.application.event.MessageDeletedEvent;
import com.jeannimi.messenger.application.event.MessageReadEvent;
import com.jeannimi.messenger.application.event.service.ProcessedEventService;
import com.jeannimi.messenger.application.port.out.RealtimeEventPublisherPort;
import com.jeannimi.messenger.domain.event.EventId;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EventConsumerTest {

  private static final String PAYLOAD = "{\"event\":\"test\"}";

  private static final UUID EVENT_UUID = UUID.fromString("018f1234-5678-7abc-8def-123456789001");

  private static final EventId EVENT_ID = new EventId(EVENT_UUID);

  @Mock private ObjectMapper objectMapper;

  @Mock private KafkaEventToApplicationEventMapper eventMapper;

  @Mock private RealtimeEventPublisherPort realtimeEventPublisher;

  @Mock private ProcessedEventService processedEventService;

  @Mock private Acknowledgment acknowledgment;

  @Mock private KafkaEventEnvelope envelope;

  @Mock private JsonNode payloadNode;

  private EventConsumer consumer;

  @BeforeEach
  void setUp() {
    consumer =
        new EventConsumer(
            objectMapper, eventMapper, realtimeEventPublisher, processedEventService, List.of());

    // Имитируем вызов @PostConstruct, выполняемый Spring.
    ReflectionTestUtils.invokeMethod(consumer, "init");
  }

  // --------------------------------------------------
  // MESSAGE CREATED
  // --------------------------------------------------

  @Test
  void consumeMessage_shouldPublishCreatedEventAndAcknowledge() throws Exception {

    MessageSentEvent messageSentEvent = mock(MessageSentEvent.class);
    MessageCreatedEvent applicationEvent = mock(MessageCreatedEvent.class);

    prepareEnvelope();

    when(objectMapper.treeToValue(payloadNode, MessageSentEvent.class))
        .thenReturn(messageSentEvent);

    when(eventMapper.toApplicationEvent(messageSentEvent)).thenReturn(applicationEvent);

    when(processedEventService.existsByEventId(EVENT_ID)).thenReturn(false);

    consumer.consumeMessage(PAYLOAD, acknowledgment);

    InOrder inOrder =
        inOrder(eventMapper, realtimeEventPublisher, processedEventService, acknowledgment);

    inOrder.verify(eventMapper).toApplicationEvent(messageSentEvent);

    inOrder.verify(realtimeEventPublisher).publish(EventType.MESSAGE_CREATED, applicationEvent);

    inOrder.verify(processedEventService).register(EVENT_ID);

    inOrder.verify(acknowledgment).acknowledge();
  }

  // --------------------------------------------------
  // MESSAGE READ
  // --------------------------------------------------

  @Test
  void consumeRead_shouldPublishReadEventAndAcknowledge() throws Exception {

    MessageReadEvent readEvent = mock(MessageReadEvent.class);

    prepareEnvelope();

    when(objectMapper.treeToValue(payloadNode, MessageReadEvent.class)).thenReturn(readEvent);

    when(processedEventService.existsByEventId(EVENT_ID)).thenReturn(false);

    consumer.consumeRead(PAYLOAD, acknowledgment);

    InOrder inOrder = inOrder(realtimeEventPublisher, processedEventService, acknowledgment);

    inOrder.verify(realtimeEventPublisher).publish(EventType.MESSAGE_READ, readEvent);

    inOrder.verify(processedEventService).register(EVENT_ID);

    inOrder.verify(acknowledgment).acknowledge();
  }

  // --------------------------------------------------
  // MESSAGE DELETED
  // --------------------------------------------------

  @Test
  void consumeDelete_shouldPublishDeletedEventAndAcknowledge() throws Exception {

    MessageDeletedEvent deletedEvent = mock(MessageDeletedEvent.class);

    prepareEnvelope();

    when(objectMapper.treeToValue(payloadNode, MessageDeletedEvent.class)).thenReturn(deletedEvent);

    when(processedEventService.existsByEventId(EVENT_ID)).thenReturn(false);

    consumer.consumeDelete(PAYLOAD, acknowledgment);

    InOrder inOrder = inOrder(realtimeEventPublisher, processedEventService, acknowledgment);

    inOrder.verify(realtimeEventPublisher).publish(EventType.MESSAGE_DELETED, deletedEvent);

    inOrder.verify(processedEventService).register(EVENT_ID);

    inOrder.verify(acknowledgment).acknowledge();
  }

  // --------------------------------------------------
  // DUPLICATE EVENT
  // --------------------------------------------------

  @Test
  void consumeMessage_shouldSkipAlreadyProcessedEvent() throws Exception {

    when(objectMapper.readValue(PAYLOAD, KafkaEventEnvelope.class)).thenReturn(envelope);

    when(envelope.eventId()).thenReturn(EVENT_UUID);
    when(envelope.payload()).thenReturn(payloadNode); // Добавить

    when(processedEventService.existsByEventId(EVENT_ID)).thenReturn(true);

    consumer.consumeMessage(PAYLOAD, acknowledgment);

    verify(processedEventService).existsByEventId(EVENT_ID);

    verifyNoInteractions(eventMapper);
    verifyNoInteractions(realtimeEventPublisher);

    verify(processedEventService, never()).register(any(EventId.class));

    verify(acknowledgment).acknowledge();
  }

  // --------------------------------------------------
  // CHAT EVENT HANDLER
  // --------------------------------------------------

  @Test
  void consumeChatEvents_shouldCallMatchingHandler() throws Exception {

    ChatEventHandler handler = mock(ChatEventHandler.class);

    when(handler.supports()).thenReturn(EventType.MESSAGE_CREATED);

    EventConsumer consumerWithHandler =
        new EventConsumer(
            objectMapper,
            eventMapper,
            realtimeEventPublisher,
            processedEventService,
            List.of(handler));

    ReflectionTestUtils.invokeMethod(consumerWithHandler, "init");

    prepareEnvelope();

    when(envelope.eventType()).thenReturn(EventType.MESSAGE_CREATED.name());

    when(processedEventService.existsByEventId(EVENT_ID)).thenReturn(false);

    consumerWithHandler.consumeChatEvents(PAYLOAD, acknowledgment);

    InOrder inOrder = inOrder(handler, processedEventService, acknowledgment);

    inOrder.verify(handler).handle(payloadNode);
    inOrder.verify(processedEventService).register(EVENT_ID);
    inOrder.verify(acknowledgment).acknowledge();

    verifyNoInteractions(realtimeEventPublisher);
  }

  @Test
  void consumeChatEvents_shouldFailWhenNoHandlerExists() throws Exception {

    prepareEnvelope();

    when(envelope.eventType()).thenReturn(EventType.MESSAGE_CREATED.name());

    when(processedEventService.existsByEventId(EVENT_ID)).thenReturn(false);

    InvalidKafkaEventException exception =
        assertThrows(
            InvalidKafkaEventException.class,
            () -> consumer.consumeChatEvents(PAYLOAD, acknowledgment));

    assertEquals("No handler found for event type: MESSAGE_CREATED", exception.getMessage());

    verify(processedEventService).existsByEventId(EVENT_ID);

    verify(processedEventService, never()).register(any(EventId.class));

    verifyNoInteractions(realtimeEventPublisher);

    verify(acknowledgment, never()).acknowledge();
  }

  // --------------------------------------------------
  // PROCESSING ERRORS
  // --------------------------------------------------

  @Test
  void consumeMessage_shouldNotRegisterOrAcknowledgeWhenPublishingFails() throws Exception {

    MessageSentEvent messageSentEvent = mock(MessageSentEvent.class);
    MessageCreatedEvent applicationEvent = mock(MessageCreatedEvent.class);

    prepareEnvelope();

    when(objectMapper.treeToValue(payloadNode, MessageSentEvent.class))
        .thenReturn(messageSentEvent);

    when(eventMapper.toApplicationEvent(messageSentEvent)).thenReturn(applicationEvent);

    when(processedEventService.existsByEventId(EVENT_ID)).thenReturn(false);

    RuntimeException cause = new RuntimeException("Publishing failed");

    doThrow(cause)
        .when(realtimeEventPublisher)
        .publish(EventType.MESSAGE_CREATED, applicationEvent);

    RuntimeException exception =
        assertThrows(
            RuntimeException.class, () -> consumer.consumeMessage(PAYLOAD, acknowledgment));

    assertEquals(cause, exception);

    verify(processedEventService, never()).register(any(EventId.class));

    verify(acknowledgment, never()).acknowledge();
  }

  @Test
  void consumeMessage_shouldFailWhenEventIdIsNull() throws Exception {

    when(objectMapper.readValue(PAYLOAD, KafkaEventEnvelope.class)).thenReturn(envelope);

    when(envelope.eventId()).thenReturn(null);
    when(envelope.payload()).thenReturn(payloadNode);

    InvalidKafkaEventException exception =
        assertThrows(
            InvalidKafkaEventException.class,
            () -> consumer.consumeMessage(PAYLOAD, acknowledgment));

    assertEquals("Kafka eventId must not be null", exception.getMessage());

    verify(processedEventService, never()).existsByEventId(any(EventId.class));

    verify(processedEventService, never()).register(any(EventId.class));

    verifyNoInteractions(realtimeEventPublisher);

    verify(acknowledgment, never()).acknowledge();
  }

  @Test
  void consumeMessage_shouldFailWhenJsonIsInvalid() throws Exception {

    JsonProcessingException cause = new JsonProcessingException("Invalid JSON") {};

    when(objectMapper.readValue(PAYLOAD, KafkaEventEnvelope.class)).thenThrow(cause);

    InvalidKafkaEventException exception =
        assertThrows(
            InvalidKafkaEventException.class,
            () -> consumer.consumeMessage(PAYLOAD, acknowledgment));

    assertEquals("Failed to deserialize Kafka event envelope", exception.getMessage());

    assertEquals(cause, exception.getCause());

    verifyNoInteractions(processedEventService);
    verifyNoInteractions(realtimeEventPublisher);

    verify(acknowledgment, never()).acknowledge();
  }

  @Test
  void consumeMessage_shouldFailWhenEventPayloadCannotBeDeserialized() throws Exception {

    prepareEnvelope();

    when(processedEventService.existsByEventId(EVENT_ID)).thenReturn(false);

    JsonProcessingException cause = new JsonProcessingException("Invalid event payload") {};

    when(objectMapper.treeToValue(payloadNode, MessageSentEvent.class)).thenThrow(cause);

    InvalidKafkaEventException exception =
        assertThrows(
            InvalidKafkaEventException.class,
            () -> consumer.consumeMessage(PAYLOAD, acknowledgment));

    assertEquals("Failed to deserialize Kafka event", exception.getMessage());

    assertEquals(cause, exception.getCause());

    verify(processedEventService).existsByEventId(EVENT_ID);

    verify(processedEventService, never()).register(any(EventId.class));

    verifyNoInteractions(realtimeEventPublisher);

    verify(acknowledgment, never()).acknowledge();
  }

  @Test
  void consumeChatEvents_shouldFailWhenEventTypeIsUnknown() throws Exception {

    prepareEnvelope();

    when(envelope.eventType()).thenReturn("UNKNOWN_EVENT");

    when(processedEventService.existsByEventId(EVENT_ID)).thenReturn(false);

    InvalidKafkaEventException exception =
        assertThrows(
            InvalidKafkaEventException.class,
            () -> consumer.consumeChatEvents(PAYLOAD, acknowledgment));

    assertEquals("Unknown Kafka event type: UNKNOWN_EVENT", exception.getMessage());

    verify(processedEventService).existsByEventId(EVENT_ID);

    verify(processedEventService, never()).register(any(EventId.class));

    verifyNoInteractions(realtimeEventPublisher);

    verify(acknowledgment, never()).acknowledge();
  }

  @Test
  void consumeMessage_shouldFailWhenEnvelopeIsNull() throws Exception {
    when(objectMapper.readValue(PAYLOAD, KafkaEventEnvelope.class)).thenReturn(null);

    InvalidKafkaEventException exception =
        assertThrows(
            InvalidKafkaEventException.class,
            () -> consumer.consumeMessage(PAYLOAD, acknowledgment));

    assertEquals("Kafka event envelope must not be null", exception.getMessage());

    verify(objectMapper).readValue(PAYLOAD, KafkaEventEnvelope.class);

    verifyNoInteractions(processedEventService);
    verifyNoInteractions(eventMapper);
    verifyNoInteractions(realtimeEventPublisher);
    verifyNoInteractions(acknowledgment);
  }

  // --------------------------------------------------
  // TEST HELPERS
  // --------------------------------------------------

  private void prepareEnvelope() throws Exception {
    when(objectMapper.readValue(PAYLOAD, KafkaEventEnvelope.class)).thenReturn(envelope);

    when(envelope.eventId()).thenReturn(EVENT_UUID);
    when(envelope.payload()).thenReturn(payloadNode);
  }
}

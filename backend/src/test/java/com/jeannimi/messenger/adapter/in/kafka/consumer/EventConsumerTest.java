package com.jeannimi.messenger.adapter.in.kafka.consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    // Имитируем вызов @PostConstruct, который Spring выполняет
    // автоматически при создании bean.
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

    verify(realtimeEventPublisher).publish(EventType.MESSAGE_READ, readEvent);

    verify(processedEventService).register(EVENT_ID);

    verify(acknowledgment).acknowledge();
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

    verify(realtimeEventPublisher).publish(EventType.MESSAGE_DELETED, deletedEvent);

    verify(processedEventService).register(EVENT_ID);

    verify(acknowledgment).acknowledge();
  }

  // --------------------------------------------------
  // DUPLICATE EVENT
  // --------------------------------------------------

  @Test
  void consumeMessage_shouldSkipAlreadyProcessedEvent() throws Exception {
    when(objectMapper.readValue(PAYLOAD, KafkaEventEnvelope.class)).thenReturn(envelope);

    when(envelope.eventId()).thenReturn(EVENT_UUID);

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

    when(objectMapper.readValue(PAYLOAD, KafkaEventEnvelope.class)).thenReturn(envelope);

    when(envelope.eventId()).thenReturn(EVENT_UUID);
    when(envelope.eventType()).thenReturn(EventType.MESSAGE_CREATED.name());
    when(envelope.payload()).thenReturn(payloadNode);

    when(processedEventService.existsByEventId(EVENT_ID)).thenReturn(false);

    consumerWithHandler.consumeChatEvents(PAYLOAD, acknowledgment);

    verify(handler).handle(payloadNode);
    verify(processedEventService).register(EVENT_ID);
    verify(acknowledgment).acknowledge();
  }

  @Test
  void consumeChatEvents_shouldSkipEventWithoutHandler() throws Exception {

    when(objectMapper.readValue(PAYLOAD, KafkaEventEnvelope.class)).thenReturn(envelope);

    when(envelope.eventId()).thenReturn(EVENT_UUID);

    when(envelope.eventType()).thenReturn(EventType.MESSAGE_CREATED.name());

    when(processedEventService.existsByEventId(EVENT_ID)).thenReturn(false);

    consumer.consumeChatEvents(PAYLOAD, acknowledgment);

    verify(processedEventService).register(EVENT_ID);

    verify(acknowledgment).acknowledge();

    verifyNoInteractions(realtimeEventPublisher);
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

    doThrow(new RuntimeException("Publishing failed"))
        .when(realtimeEventPublisher)
        .publish(EventType.MESSAGE_CREATED, applicationEvent);

    assertThrows(RuntimeException.class, () -> consumer.consumeMessage(PAYLOAD, acknowledgment));

    verify(processedEventService, never()).register(any(EventId.class));

    verify(acknowledgment, never()).acknowledge();
  }

  @Test
  void consumeMessage_shouldFailWhenEventIdIsNull() throws Exception {
    when(objectMapper.readValue(PAYLOAD, KafkaEventEnvelope.class)).thenReturn(envelope);

    when(envelope.eventId()).thenReturn(null);

    assertThrows(RuntimeException.class, () -> consumer.consumeMessage(PAYLOAD, acknowledgment));

    verify(processedEventService, never()).existsByEventId(any(EventId.class));

    verify(processedEventService, never()).register(any(EventId.class));

    verify(acknowledgment, never()).acknowledge();
  }

  @Test
  void consumeMessage_shouldFailWhenJsonIsInvalid() throws Exception {
    when(objectMapper.readValue(PAYLOAD, KafkaEventEnvelope.class))
        .thenThrow(new JsonProcessingException("Invalid JSON") {});

    assertThrows(RuntimeException.class, () -> consumer.consumeMessage(PAYLOAD, acknowledgment));

    verifyNoInteractions(processedEventService);
    verifyNoInteractions(realtimeEventPublisher);

    verify(acknowledgment, never()).acknowledge();
  }

  @Test
  void consumeMessage_shouldFailWhenEventPayloadCannotBeDeserialized() throws Exception {

    prepareEnvelope();

    when(processedEventService.existsByEventId(EVENT_ID)).thenReturn(false);

    when(objectMapper.treeToValue(payloadNode, MessageSentEvent.class))
        .thenThrow(new JsonProcessingException("Invalid event payload") {});

    RuntimeException exception =
        assertThrows(
            RuntimeException.class, () -> consumer.consumeMessage(PAYLOAD, acknowledgment));

    assertEquals("Failed to process Kafka event", exception.getMessage());

    assertInstanceOf(RuntimeException.class, exception.getCause());

    assertEquals("Failed to deserialize Kafka event", exception.getCause().getMessage());

    verify(processedEventService).existsByEventId(EVENT_ID);

    verify(processedEventService, never()).register(any(EventId.class));

    verifyNoInteractions(realtimeEventPublisher);

    verify(acknowledgment, never()).acknowledge();
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

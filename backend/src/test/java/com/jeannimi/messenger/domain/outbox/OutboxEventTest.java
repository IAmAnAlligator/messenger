package com.jeannimi.messenger.domain.outbox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jeannimi.messenger.domain.event.EventId;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OutboxEventTest {

  private static final OutboxEventId OUTBOX_EVENT_ID =
      new OutboxEventId(UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789012"));

  private static final OutboxEventId ANOTHER_OUTBOX_EVENT_ID =
      new OutboxEventId(UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789013"));

  private static final EventId EVENT_ID =
      new EventId(UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789014"));

  private static final EventId ANOTHER_EVENT_ID =
      new EventId(UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789015"));

  private static final AggregateId AGGREGATE_ID =
      new AggregateId(UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789016"));

  private static final Instant CREATED_AT = Instant.parse("2026-10-17T12:00:00Z");

  private static final Instant NEXT_ATTEMPT_AT = Instant.parse("2026-10-17T12:01:00Z");

  private static final String TOPIC = "chat-events";
  private static final String EVENT_TYPE = "MESSAGE_CREATED";
  private static final String PAYLOAD = "{\"messageId\":\"123\"}";

  @Test
  void create_shouldCreateOutboxEventWithExpectedValues() {
    Instant before = Instant.now();

    OutboxEvent event =
        OutboxEvent.create(OUTBOX_EVENT_ID, EVENT_ID, TOPIC, EVENT_TYPE, AGGREGATE_ID, PAYLOAD);

    Instant after = Instant.now();

    assertEquals(OUTBOX_EVENT_ID, event.getId());
    assertEquals(EVENT_ID, event.getEventId());
    assertEquals(TOPIC, event.getTopic());
    assertEquals(EVENT_TYPE, event.getEventType());
    assertEquals(AGGREGATE_ID, event.getAggregateId());
    assertEquals(OutboxStatus.NEW, event.getStatus());
    assertEquals(PAYLOAD, event.getPayload());
    assertTrue(!event.getCreatedAt().isBefore(before) && !event.getCreatedAt().isAfter(after));
    assertEquals(0, event.getAttemptCount());
    assertEquals(event.getCreatedAt(), event.getNextAttemptAt());
  }

  @Test
  void create_shouldThrowNullPointerException_whenOutboxEventIdIsNull() {
    assertThrows(
        NullPointerException.class,
        () -> OutboxEvent.create(null, EVENT_ID, TOPIC, EVENT_TYPE, AGGREGATE_ID, PAYLOAD));
  }

  @Test
  void create_shouldThrowNullPointerException_whenEventIdIsNull() {
    assertThrows(
        NullPointerException.class,
        () -> OutboxEvent.create(OUTBOX_EVENT_ID, null, TOPIC, EVENT_TYPE, AGGREGATE_ID, PAYLOAD));
  }

  @Test
  void create_shouldThrowNullPointerException_whenTopicIsNull() {
    assertThrows(
        NullPointerException.class,
        () ->
            OutboxEvent.create(OUTBOX_EVENT_ID, EVENT_ID, null, EVENT_TYPE, AGGREGATE_ID, PAYLOAD));
  }

  @Test
  void create_shouldThrowNullPointerException_whenEventTypeIsNull() {
    assertThrows(
        NullPointerException.class,
        () -> OutboxEvent.create(OUTBOX_EVENT_ID, EVENT_ID, TOPIC, null, AGGREGATE_ID, PAYLOAD));
  }

  @Test
  void create_shouldThrowNullPointerException_whenAggregateIdIsNull() {
    assertThrows(
        NullPointerException.class,
        () -> OutboxEvent.create(OUTBOX_EVENT_ID, EVENT_ID, TOPIC, EVENT_TYPE, null, PAYLOAD));
  }

  @Test
  void create_shouldThrowNullPointerException_whenPayloadIsNull() {
    assertThrows(
        NullPointerException.class,
        () -> OutboxEvent.create(OUTBOX_EVENT_ID, EVENT_ID, TOPIC, EVENT_TYPE, AGGREGATE_ID, null));
  }

  @Test
  void reconstitute_shouldCreateOutboxEventWithExpectedValues() {
    OutboxEvent event =
        OutboxEvent.reconstitute(
            OUTBOX_EVENT_ID,
            EVENT_ID,
            TOPIC,
            EVENT_TYPE,
            AGGREGATE_ID,
            OutboxStatus.SENT,
            PAYLOAD,
            CREATED_AT,
            3,
            NEXT_ATTEMPT_AT);

    assertEquals(OUTBOX_EVENT_ID, event.getId());
    assertEquals(EVENT_ID, event.getEventId());
    assertEquals(TOPIC, event.getTopic());
    assertEquals(EVENT_TYPE, event.getEventType());
    assertEquals(AGGREGATE_ID, event.getAggregateId());
    assertEquals(OutboxStatus.SENT, event.getStatus());
    assertEquals(PAYLOAD, event.getPayload());
    assertEquals(CREATED_AT, event.getCreatedAt());
    assertEquals(3, event.getAttemptCount());
    assertEquals(NEXT_ATTEMPT_AT, event.getNextAttemptAt());
  }

  @Test
  void reconstitute_shouldThrowNullPointerException_whenOutboxEventIdIsNull() {
    assertThrows(
        NullPointerException.class,
        () ->
            OutboxEvent.reconstitute(
                null,
                EVENT_ID,
                TOPIC,
                EVENT_TYPE,
                AGGREGATE_ID,
                OutboxStatus.NEW,
                PAYLOAD,
                CREATED_AT,
                0,
                NEXT_ATTEMPT_AT));
  }

  @Test
  void reconstitute_shouldThrowNullPointerException_whenEventIdIsNull() {
    assertThrows(
        NullPointerException.class,
        () ->
            OutboxEvent.reconstitute(
                OUTBOX_EVENT_ID,
                null,
                TOPIC,
                EVENT_TYPE,
                AGGREGATE_ID,
                OutboxStatus.NEW,
                PAYLOAD,
                CREATED_AT,
                0,
                NEXT_ATTEMPT_AT));
  }

  @Test
  void reconstitute_shouldThrowNullPointerException_whenTopicIsNull() {
    assertThrows(
        NullPointerException.class,
        () ->
            OutboxEvent.reconstitute(
                OUTBOX_EVENT_ID,
                EVENT_ID,
                null,
                EVENT_TYPE,
                AGGREGATE_ID,
                OutboxStatus.NEW,
                PAYLOAD,
                CREATED_AT,
                0,
                NEXT_ATTEMPT_AT));
  }

  @Test
  void reconstitute_shouldThrowNullPointerException_whenEventTypeIsNull() {
    assertThrows(
        NullPointerException.class,
        () ->
            OutboxEvent.reconstitute(
                OUTBOX_EVENT_ID,
                EVENT_ID,
                TOPIC,
                null,
                AGGREGATE_ID,
                OutboxStatus.NEW,
                PAYLOAD,
                CREATED_AT,
                0,
                NEXT_ATTEMPT_AT));
  }

  @Test
  void reconstitute_shouldThrowNullPointerException_whenAggregateIdIsNull() {
    assertThrows(
        NullPointerException.class,
        () ->
            OutboxEvent.reconstitute(
                OUTBOX_EVENT_ID,
                EVENT_ID,
                TOPIC,
                EVENT_TYPE,
                null,
                OutboxStatus.NEW,
                PAYLOAD,
                CREATED_AT,
                0,
                NEXT_ATTEMPT_AT));
  }

  @Test
  void reconstitute_shouldThrowNullPointerException_whenStatusIsNull() {
    assertThrows(
        NullPointerException.class,
        () ->
            OutboxEvent.reconstitute(
                OUTBOX_EVENT_ID,
                EVENT_ID,
                TOPIC,
                EVENT_TYPE,
                AGGREGATE_ID,
                null,
                PAYLOAD,
                CREATED_AT,
                0,
                NEXT_ATTEMPT_AT));
  }

  @Test
  void reconstitute_shouldThrowNullPointerException_whenPayloadIsNull() {
    assertThrows(
        NullPointerException.class,
        () ->
            OutboxEvent.reconstitute(
                OUTBOX_EVENT_ID,
                EVENT_ID,
                TOPIC,
                EVENT_TYPE,
                AGGREGATE_ID,
                OutboxStatus.NEW,
                null,
                CREATED_AT,
                0,
                NEXT_ATTEMPT_AT));
  }

  @Test
  void reconstitute_shouldThrowNullPointerException_whenCreatedAtIsNull() {
    assertThrows(
        NullPointerException.class,
        () ->
            OutboxEvent.reconstitute(
                OUTBOX_EVENT_ID,
                EVENT_ID,
                TOPIC,
                EVENT_TYPE,
                AGGREGATE_ID,
                OutboxStatus.NEW,
                PAYLOAD,
                null,
                0,
                NEXT_ATTEMPT_AT));
  }

  @Test
  void reconstitute_shouldThrowIllegalArgumentException_whenAttemptCountIsNegative() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                OutboxEvent.reconstitute(
                    OUTBOX_EVENT_ID,
                    EVENT_ID,
                    TOPIC,
                    EVENT_TYPE,
                    AGGREGATE_ID,
                    OutboxStatus.NEW,
                    PAYLOAD,
                    CREATED_AT,
                    -1,
                    NEXT_ATTEMPT_AT));

    assertEquals("Attempt count cannot be negative", exception.getMessage());
  }

  @Test
  void reconstitute_shouldAcceptZeroAttemptCount() {
    OutboxEvent event =
        OutboxEvent.reconstitute(
            OUTBOX_EVENT_ID,
            EVENT_ID,
            TOPIC,
            EVENT_TYPE,
            AGGREGATE_ID,
            OutboxStatus.NEW,
            PAYLOAD,
            CREATED_AT,
            0,
            NEXT_ATTEMPT_AT);

    assertEquals(0, event.getAttemptCount());
  }

  @Test
  void reconstitute_shouldThrowNullPointerException_whenNextAttemptAtIsNull() {
    assertThrows(
        NullPointerException.class,
        () ->
            OutboxEvent.reconstitute(
                OUTBOX_EVENT_ID,
                EVENT_ID,
                TOPIC,
                EVENT_TYPE,
                AGGREGATE_ID,
                OutboxStatus.NEW,
                PAYLOAD,
                CREATED_AT,
                0,
                null));
  }

  @Test
  void equals_shouldReturnTrue_whenComparingSameInstance() {
    OutboxEvent event = createEvent();

    assertTrue(event.equals(event));
  }

  @Test
  void equals_shouldReturnTrue_whenIdsAreEqual() {
    OutboxEvent first = createEvent();

    OutboxEvent second =
        OutboxEvent.reconstitute(
            OUTBOX_EVENT_ID,
            ANOTHER_EVENT_ID,
            "another-topic",
            "ANOTHER_EVENT",
            AGGREGATE_ID,
            OutboxStatus.SENT,
            "another-payload",
            CREATED_AT,
            5,
            NEXT_ATTEMPT_AT);

    assertEquals(first, second);
  }

  @Test
  void equals_shouldReturnFalse_whenIdsAreDifferent() {
    OutboxEvent first = createEvent();

    OutboxEvent second =
        OutboxEvent.reconstitute(
            ANOTHER_OUTBOX_EVENT_ID,
            EVENT_ID,
            TOPIC,
            EVENT_TYPE,
            AGGREGATE_ID,
            OutboxStatus.NEW,
            PAYLOAD,
            CREATED_AT,
            0,
            NEXT_ATTEMPT_AT);

    assertNotEquals(first, second);
  }

  @Test
  void equals_shouldReturnFalse_whenComparedWithNull() {
    OutboxEvent event = createEvent();

    assertFalse(event.equals(null));
  }

  @Test
  void equals_shouldReturnFalse_whenComparedWithDifferentType() {
    OutboxEvent event = createEvent();

    assertFalse(event.equals("event"));
  }

  @Test
  void hashCode_shouldBeEqual_whenIdsAreEqual() {
    OutboxEvent first = createEvent();

    OutboxEvent second =
        OutboxEvent.reconstitute(
            OUTBOX_EVENT_ID,
            ANOTHER_EVENT_ID,
            TOPIC,
            EVENT_TYPE,
            AGGREGATE_ID,
            OutboxStatus.NEW,
            PAYLOAD,
            CREATED_AT,
            0,
            NEXT_ATTEMPT_AT);

    assertEquals(first.hashCode(), second.hashCode());
  }

  @Test
  void hashCode_shouldBeDifferent_whenIdsAreDifferent() {
    OutboxEvent first = createEvent();

    OutboxEvent second =
        OutboxEvent.reconstitute(
            ANOTHER_OUTBOX_EVENT_ID,
            EVENT_ID,
            TOPIC,
            EVENT_TYPE,
            AGGREGATE_ID,
            OutboxStatus.NEW,
            PAYLOAD,
            CREATED_AT,
            0,
            NEXT_ATTEMPT_AT);

    assertNotEquals(first.hashCode(), second.hashCode());
  }

  private OutboxEvent createEvent() {
    return OutboxEvent.reconstitute(
        OUTBOX_EVENT_ID,
        EVENT_ID,
        TOPIC,
        EVENT_TYPE,
        AGGREGATE_ID,
        OutboxStatus.NEW,
        PAYLOAD,
        CREATED_AT,
        0,
        NEXT_ATTEMPT_AT);
  }
}

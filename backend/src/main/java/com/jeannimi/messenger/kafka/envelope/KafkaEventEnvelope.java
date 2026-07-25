package com.jeannimi.messenger.kafka.envelope;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KafkaEventEnvelope(
    UUID eventId, String eventType, String aggregateId, JsonNode payload) {}

package com.jeannimi.messenger.application.outbox;

public record OutboxBatchRequest(
    OutboxStatus status,
    int limit) {}
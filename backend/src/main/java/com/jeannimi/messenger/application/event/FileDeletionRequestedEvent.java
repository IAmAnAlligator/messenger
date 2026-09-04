package com.jeannimi.messenger.application.event;

public record FileDeletionRequestedEvent(
    String storageFileName) implements ApplicationEvent {
}
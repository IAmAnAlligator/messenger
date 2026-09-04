package com.jeannimi.messenger.application.event;

import com.jeannimi.messenger.application.message.dto.MessageResult;

public record MessageCreatedEvent(
    MessageResult message) implements ApplicationEvent {
}
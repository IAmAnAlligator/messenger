package com.jeannimi.messenger.kafka.mapper;

import com.jeannimi.messenger.application.event.EventType;
import org.springframework.stereotype.Component;

@Component
public class KafkaEventTypeMapper {

  public com.jeannimi.messenger.kafka.event.EventType toKafkaEventType(
      EventType type) {

    return switch (type) {
      case CHAT_CREATED -> com.jeannimi.messenger.kafka.event.EventType.CHAT_CREATED;
      case CHAT_DELETED -> com.jeannimi.messenger.kafka.event.EventType.CHAT_DELETED;
      case CHAT_RENAMED -> com.jeannimi.messenger.kafka.event.EventType.CHAT_RENAMED;
      case CHAT_MEMBER_ADDED -> com.jeannimi.messenger.kafka.event.EventType.CHAT_MEMBER_ADDED;
      case CHAT_MEMBER_REMOVED -> com.jeannimi.messenger.kafka.event.EventType.CHAT_MEMBER_REMOVED;
      case CHAT_MEMBER_LEFT -> com.jeannimi.messenger.kafka.event.EventType.CHAT_MEMBER_LEFT;
      case MESSAGE_CREATED -> com.jeannimi.messenger.kafka.event.EventType.MESSAGE_CREATED;
      case MESSAGE_READ -> com.jeannimi.messenger.kafka.event.EventType.MESSAGE_READ;
      case MESSAGE_DELETED -> com.jeannimi.messenger.kafka.event.EventType.MESSAGE_DELETED;
      case FILE_DELETION_REQUESTED ->
          com.jeannimi.messenger.kafka.event.EventType.FILE_DELETION_REQUESTED;
    };
  }
}
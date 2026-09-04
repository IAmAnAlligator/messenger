package com.jeannimi.messenger.kafka.mapper;

import com.jeannimi.messenger.application.event.EventType;
import com.jeannimi.messenger.kafka.KafkaTopics;
import org.springframework.stereotype.Component;

@Component
public class KafkaTopicMapper {

  public String toTopic(EventType type) {

    return switch (type) {
      case CHAT_CREATED, CHAT_DELETED, CHAT_RENAMED, CHAT_MEMBER_ADDED, CHAT_MEMBER_REMOVED,
           CHAT_MEMBER_LEFT -> KafkaTopics.CHAT_EVENTS;
      case MESSAGE_CREATED -> KafkaTopics.CHAT_MESSAGES;
      case MESSAGE_READ -> KafkaTopics.CHAT_READ;
      case MESSAGE_DELETED -> KafkaTopics.CHAT_MESSAGE_DELETED;
      case FILE_DELETION_REQUESTED -> KafkaTopics.FILE_DELETE;
    };
  }
}
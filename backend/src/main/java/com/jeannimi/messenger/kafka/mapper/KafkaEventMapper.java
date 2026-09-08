package com.jeannimi.messenger.kafka.mapper;

import com.jeannimi.messenger.application.event.ApplicationEvent;
import com.jeannimi.messenger.application.event.ChatCreatedEvent;
import com.jeannimi.messenger.application.event.ChatDeletedEvent;
import com.jeannimi.messenger.application.event.ChatMemberAddedEvent;
import com.jeannimi.messenger.application.event.ChatMemberLeftEvent;
import com.jeannimi.messenger.application.event.ChatMemberRemovedEvent;
import com.jeannimi.messenger.application.event.ChatRenamedEvent;
import com.jeannimi.messenger.application.event.EventType;
import com.jeannimi.messenger.application.event.FileDeletionRequestedEvent;
import com.jeannimi.messenger.application.event.MessageCreatedEvent;
import com.jeannimi.messenger.application.event.MessageDeletedEvent;
import com.jeannimi.messenger.application.event.MessageReadEvent;
import org.springframework.stereotype.Component;

@Component
public class KafkaEventMapper {

  public Object toKafkaEvent(EventType type, ApplicationEvent event) {

    return switch (type) {
      case CHAT_CREATED -> toKafkaEvent((ChatCreatedEvent) event);
      case CHAT_DELETED -> toKafkaEvent((ChatDeletedEvent) event);
      case CHAT_RENAMED -> toKafkaEvent((ChatRenamedEvent) event);
      case CHAT_MEMBER_ADDED -> toKafkaEvent((ChatMemberAddedEvent) event);
      case CHAT_MEMBER_REMOVED -> toKafkaEvent((ChatMemberRemovedEvent) event);
      case CHAT_MEMBER_LEFT -> toKafkaEvent((ChatMemberLeftEvent) event);
      case MESSAGE_CREATED -> toKafkaEvent((MessageCreatedEvent) event);
      case MESSAGE_READ -> toKafkaEvent((MessageReadEvent) event);
      case MESSAGE_DELETED -> toKafkaEvent((MessageDeletedEvent) event);
      case FILE_DELETION_REQUESTED -> toKafkaEvent((FileDeletionRequestedEvent) event);
    };
  }

  private com.jeannimi.messenger.kafka.event.ChatCreatedEvent toKafkaEvent(ChatCreatedEvent event) {

    return new com.jeannimi.messenger.kafka.event.ChatCreatedEvent(
        event.chatId(), event.name(), event.type(), event.memberIds());
  }

  private com.jeannimi.messenger.kafka.event.ChatDeletedEvent toKafkaEvent(ChatDeletedEvent event) {

    return new com.jeannimi.messenger.kafka.event.ChatDeletedEvent(event.chatId());
  }

  private com.jeannimi.messenger.kafka.event.ChatRenamedEvent toKafkaEvent(ChatRenamedEvent event) {

    return new com.jeannimi.messenger.kafka.event.ChatRenamedEvent(
        event.chatId(), event.oldName(), event.newName());
  }

  private com.jeannimi.messenger.kafka.event.ChatMemberAddedEvent toKafkaEvent(
      ChatMemberAddedEvent event) {

    return new com.jeannimi.messenger.kafka.event.ChatMemberAddedEvent(
        event.chatId(), event.userId(), event.username());
  }

  private com.jeannimi.messenger.kafka.event.ChatMemberRemovedEvent toKafkaEvent(
      ChatMemberRemovedEvent event) {

    return new com.jeannimi.messenger.kafka.event.ChatMemberRemovedEvent(
        event.chatId(), event.userId());
  }

  private com.jeannimi.messenger.kafka.event.ChatMemberLeftEvent toKafkaEvent(
      ChatMemberLeftEvent event) {

    return new com.jeannimi.messenger.kafka.event.ChatMemberLeftEvent(
        event.chatId(), event.userId());
  }

  private com.jeannimi.messenger.kafka.event.MessageSentEvent toKafkaEvent(
      MessageCreatedEvent event) {

    var message = event.message();

    return new com.jeannimi.messenger.kafka.event.MessageSentEvent(
        message.id(),
        message.chatId(),
        message.sender(),
        message.content(),
        message.createdAt(),
        message.attachment());
  }

  private com.jeannimi.messenger.kafka.event.MessageReadEvent toKafkaEvent(MessageReadEvent event) {

    return new com.jeannimi.messenger.kafka.event.MessageReadEvent(
        event.messageId(),
        event.chatId(),
        event.readerId(),
        event.readAt(),
        event.lastReadMessageId());
  }

  private com.jeannimi.messenger.kafka.event.MessageDeletedEvent toKafkaEvent(
      MessageDeletedEvent event) {

    return new com.jeannimi.messenger.kafka.event.MessageDeletedEvent(
        event.messageId(), event.chatId(), event.deletedBy(), event.deletedAt());
  }

  private com.jeannimi.messenger.kafka.event.FileDeletionRequestedEvent toKafkaEvent(
      FileDeletionRequestedEvent event) {

    return new com.jeannimi.messenger.kafka.event.FileDeletionRequestedEvent(
        event.storageFileName());
  }
}

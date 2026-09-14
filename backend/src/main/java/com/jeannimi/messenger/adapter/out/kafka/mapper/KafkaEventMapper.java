package com.jeannimi.messenger.adapter.out.kafka.mapper;

import com.jeannimi.messenger.adapter.kafka.event.MessageSentEvent;
import com.jeannimi.messenger.application.event.MessageCreatedEvent;
import org.springframework.stereotype.Component;

@Component
public class KafkaEventMapper {

  public MessageSentEvent toKafkaEvent(MessageCreatedEvent event) {

    var message = event.message();

    return new MessageSentEvent(
        message.id(),
        message.chatId(),
        message.sender(),
        message.content(),
        message.createdAt(),
        message.attachment());
  }
}
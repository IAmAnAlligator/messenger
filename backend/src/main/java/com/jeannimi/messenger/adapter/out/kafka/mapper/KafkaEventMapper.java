package com.jeannimi.messenger.adapter.out.kafka.mapper;

import com.jeannimi.messenger.adapter.kafka.event.MessageSentEvent;
import com.jeannimi.messenger.application.event.MessageCreatedEvent;
import org.springframework.stereotype.Component;

@Component
public class KafkaEventMapper {

  public MessageSentEvent toKafkaEvent(MessageCreatedEvent event) {

    return MessageSentEvent.from(event.message(), event.recipientUserIds());
  }
}

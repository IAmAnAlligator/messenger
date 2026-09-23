package com.jeannimi.messenger.adapter.in.kafka.mapper;

import com.jeannimi.messenger.adapter.kafka.event.MessageSentEvent;
import com.jeannimi.messenger.application.event.MessageCreatedEvent;
import org.springframework.stereotype.Component;

@Component
public class KafkaEventToApplicationEventMapper {

  public MessageCreatedEvent toApplicationEvent(MessageSentEvent event) {

    return new MessageCreatedEvent(event.toResult(), event.recipientUserIds());
  }
}

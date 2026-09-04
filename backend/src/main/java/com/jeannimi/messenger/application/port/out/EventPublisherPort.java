package com.jeannimi.messenger.application.port.out;

import com.jeannimi.messenger.application.event.ApplicationEvent;
import com.jeannimi.messenger.application.event.EventType;

public interface EventPublisherPort {

  void publish(
      EventType type,
      String aggregateId,
      ApplicationEvent event);
}
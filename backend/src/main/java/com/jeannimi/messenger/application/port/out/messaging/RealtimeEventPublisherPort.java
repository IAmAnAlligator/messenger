package com.jeannimi.messenger.application.port.out.messaging;

import com.jeannimi.messenger.application.event.ApplicationEvent;
import com.jeannimi.messenger.application.event.EventType;

public interface RealtimeEventPublisherPort {

  void publish(EventType eventType, ApplicationEvent event);
}

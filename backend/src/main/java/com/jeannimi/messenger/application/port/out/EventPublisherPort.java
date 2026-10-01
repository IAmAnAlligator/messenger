package com.jeannimi.messenger.application.port.out;

import com.jeannimi.messenger.application.event.ApplicationEvent;
import com.jeannimi.messenger.application.event.EventType;
import com.jeannimi.messenger.domain.common.DomainId;
import com.jeannimi.messenger.domain.outbox.AggregateId;

public interface EventPublisherPort {

  void publish(EventType type, AggregateId aggregateId, ApplicationEvent event);
}

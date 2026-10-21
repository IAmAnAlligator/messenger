package com.jeannimi.messenger.application.port.out;

import com.jeannimi.messenger.domain.event.EventId;

public interface NotificationEventRepositoryPort {

  boolean exists(EventId eventId);

  void save(EventId eventId);
}

package com.jeannimi.messenger.application.port.out;

import com.jeannimi.messenger.domain.common.EventId;

public interface NotificationEventRepositoryPort {

  boolean tryRegister(EventId eventId);
}

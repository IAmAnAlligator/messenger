package com.jeannimi.messenger.adapter.out.persistence.adapter;

import com.jeannimi.messenger.adapter.out.persistence.repository.NotificationEventJpaRepository;
import com.jeannimi.messenger.application.port.out.NotificationEventRepositoryPort;
import com.jeannimi.messenger.domain.common.EventId;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NotificationEventPersistenceAdapter implements NotificationEventRepositoryPort {

  private final NotificationEventJpaRepository repository;

  @Override
  public boolean tryRegister(EventId eventId) {
    Objects.requireNonNull(eventId, "eventId");

    return repository.insertIfAbsent(eventId.value()) == 1;
  }
}

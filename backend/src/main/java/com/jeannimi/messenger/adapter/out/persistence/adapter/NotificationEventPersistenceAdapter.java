package com.jeannimi.messenger.adapter.out.persistence.adapter;

import com.jeannimi.messenger.adapter.out.persistence.entity.NotificationEventJpaEntity;
import com.jeannimi.messenger.adapter.out.persistence.repository.NotificationEventJpaRepository;
import com.jeannimi.messenger.application.port.out.NotificationEventRepositoryPort;
import com.jeannimi.messenger.domain.event.EventId;
import java.time.Instant;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class NotificationEventPersistenceAdapter implements NotificationEventRepositoryPort {

  private final NotificationEventJpaRepository repository;

  @Override
  @Transactional(readOnly = true)
  public boolean exists(EventId eventId) {
    Objects.requireNonNull(eventId, "eventId");

    return repository.existsById(eventId.value());
  }

  @Override
  @Transactional
  public void save(EventId eventId) {
    Objects.requireNonNull(eventId, "eventId");

    if (repository.existsById(eventId.value())) {
      return;
    }

    repository.save(new NotificationEventJpaEntity(eventId.value(), Instant.now()));
  }
}

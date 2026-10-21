package com.jeannimi.messenger.adapter.out.persistence.mapper;

import com.jeannimi.messenger.adapter.out.persistence.entity.NotificationEventJpaEntity;
import com.jeannimi.messenger.domain.event.EventId;
import java.time.Instant;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventPersistenceMapper {

  public EventId toDomain(NotificationEventJpaEntity entity) {
    if (entity == null) {
      return null;
    }

    return new EventId(entity.getEventId());
  }

  public NotificationEventJpaEntity toEntity(EventId eventId) {
    Objects.requireNonNull(eventId, "eventId");

    return new NotificationEventJpaEntity(eventId.value(), Instant.now());
  }
}

package com.jeannimi.messenger.adapter.out.persistence.repository;

import com.jeannimi.messenger.adapter.out.persistence.entity.NotificationEventJpaEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationEventJpaRepository
    extends JpaRepository<NotificationEventJpaEntity, UUID> {

  @Modifying
  @Query(
      value =
          """
            INSERT INTO processed_notification_events (event_id, processed_at)
            VALUES (:eventId, CURRENT_TIMESTAMP)
            ON CONFLICT (event_id) DO NOTHING
            """,
      nativeQuery = true)
  int insertIfAbsent(@Param("eventId") UUID eventId);
}

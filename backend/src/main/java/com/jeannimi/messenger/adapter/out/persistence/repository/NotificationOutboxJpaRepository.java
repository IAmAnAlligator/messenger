package com.jeannimi.messenger.adapter.out.persistence.repository;

import com.jeannimi.messenger.adapter.out.persistence.entity.NotificationOutboxJpaEntity;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationOutboxJpaRepository
    extends JpaRepository<NotificationOutboxJpaEntity, UUID> {

  @Query(
      value =
          """
          SELECT *
          FROM notification_outbox
          WHERE
              (
                  status = 'NEW'
                  OR (
                      status = 'RETRY'
                      AND next_attempt_at <= CURRENT_TIMESTAMP
                  )
                  OR (
                      status = 'PROCESSING'
                      AND (
                          locked_until IS NULL
                          OR locked_until <= CURRENT_TIMESTAMP
                      )
                  )
              )
          ORDER BY created_at, id
          LIMIT :batchSize
          FOR UPDATE SKIP LOCKED
          """,
      nativeQuery = true)
  List<NotificationOutboxJpaEntity> findAvailableBatch(@Param("batchSize") int batchSize);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(
      value =
          """
          UPDATE notification_outbox
          SET
              status = 'PROCESSING',
              attempts = attempts + 1,
              locked_until = CURRENT_TIMESTAMP + INTERVAL '10 minutes'
          WHERE id IN (:ids)
          """,
      nativeQuery = true)
  int markBatchAsProcessing(@Param("ids") List<UUID> ids);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(
      value =
          """
    UPDATE notification_outbox
    SET status = 'SENT',
        sent_at = CURRENT_TIMESTAMP,
        locked_until = NULL,
        last_error = NULL
    WHERE id = :id
      AND status = 'PROCESSING'
    """,
      nativeQuery = true)
  int markAsSent(@Param("id") UUID id);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(
      value =
          """
    UPDATE notification_outbox
    SET status = 'RETRY',
        next_attempt_at = :nextAttemptAt,
        locked_until = NULL,
        last_error = :error
    WHERE id = :id
      AND status = 'PROCESSING'
    """,
      nativeQuery = true)
  int scheduleRetry(
      @Param("id") UUID id,
      @Param("nextAttemptAt") Instant nextAttemptAt,
      @Param("error") String error);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(
      value =
          """
    UPDATE notification_outbox
    SET status = 'DEAD',
        locked_until = NULL,
        last_error = :error
    WHERE id = :id
      AND status = 'PROCESSING'
    """,
      nativeQuery = true)
  int markAsDead(@Param("id") UUID id, @Param("error") String error);
}

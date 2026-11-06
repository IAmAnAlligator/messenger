package com.jeannimi.messenger.adapter.out.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.jeannimi.messenger.adapter.out.persistence.entity.NotificationOutboxJpaEntity;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest(properties = {"spring.jpa.show-sql=false"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class NotificationOutboxJpaRepositoryIT {

  @Autowired private PlatformTransactionManager transactionManager;

  @Container
  static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("messenger_test")
          .withUsername("test")
          .withPassword("test");

  @DynamicPropertySource
  static void configureDataSource(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
  }

  @Autowired private NotificationOutboxJpaRepository repository;

  @Autowired private JdbcTemplate jdbcTemplate;

  // ---------------------------------------------------------
  // findAvailableBatch
  // ---------------------------------------------------------

  @Test
  void shouldReturnNewNotifications() {

    UUID firstId = insertNotification("NEW");
    UUID secondId = insertNotification("NEW");

    List<NotificationOutboxJpaEntity> result = repository.findAvailableBatch(10);

    assertThat(result).extracting(NotificationOutboxJpaEntity::getId).contains(firstId, secondId);
  }

  @Test
  void shouldReturnRetryNotificationsWhenNextAttemptIsDue() {

    UUID id = insertNotification("RETRY", Instant.now().minusSeconds(60), null);

    List<NotificationOutboxJpaEntity> result = repository.findAvailableBatch(10);

    assertThat(result).extracting(NotificationOutboxJpaEntity::getId).contains(id);
  }

  @Test
  void shouldNotReturnRetryNotificationsWhenNextAttemptIsInFuture() {

    UUID id = insertNotification("RETRY", Instant.now().plusSeconds(3600), null);

    List<NotificationOutboxJpaEntity> result = repository.findAvailableBatch(10);

    assertThat(result).extracting(NotificationOutboxJpaEntity::getId).doesNotContain(id);
  }

  @Test
  void shouldReturnProcessingNotificationsWhenLockExpired() {

    UUID id = insertNotification("PROCESSING", Instant.now(), Instant.now().minusSeconds(60));

    List<NotificationOutboxJpaEntity> result = repository.findAvailableBatch(10);

    assertThat(result).extracting(NotificationOutboxJpaEntity::getId).contains(id);
  }

  @Test
  void shouldReturnProcessingNotificationsWhenLockIsNull() {

    UUID id = insertNotification("PROCESSING", Instant.now(), null);

    List<NotificationOutboxJpaEntity> result = repository.findAvailableBatch(10);

    assertThat(result).extracting(NotificationOutboxJpaEntity::getId).contains(id);
  }

  @Test
  void shouldNotReturnProcessingNotificationsWhenLockIsActive() {

    UUID id = insertNotification("PROCESSING", Instant.now(), Instant.now().plusSeconds(3600));

    List<NotificationOutboxJpaEntity> result = repository.findAvailableBatch(10);

    assertThat(result).extracting(NotificationOutboxJpaEntity::getId).doesNotContain(id);
  }

  @Test
  void shouldNotReturnSentOrDeadNotifications() {

    UUID sentId = insertNotification("SENT");
    UUID deadId = insertNotification("DEAD");
    UUID skippedId = insertNotification("SKIPPED");

    List<NotificationOutboxJpaEntity> result = repository.findAvailableBatch(10);

    assertThat(result)
        .extracting(NotificationOutboxJpaEntity::getId)
        .doesNotContain(sentId, deadId, skippedId);
  }

  @Test
  void shouldRespectBatchSize() {

    insertNotification("NEW");
    insertNotification("NEW");
    insertNotification("NEW");

    List<NotificationOutboxJpaEntity> result = repository.findAvailableBatch(2);

    assertThat(result).hasSize(2);
  }

  @Test
  void shouldReturnNotificationsOrderedByCreatedAt() {

    Instant now = Instant.now();

    UUID firstId = insertNotification("NEW", now.minusSeconds(120), null);

    UUID secondId = insertNotification("NEW", now.minusSeconds(60), null);

    UUID thirdId = insertNotification("NEW", now, null);

    List<NotificationOutboxJpaEntity> result = repository.findAvailableBatch(10);

    assertThat(result)
        .extracting(NotificationOutboxJpaEntity::getId)
        .containsSubsequence(firstId, secondId, thirdId);
  }

  // ---------------------------------------------------------
  // markBatchAsProcessing
  // ---------------------------------------------------------

  @Test
  void shouldMarkBatchAsProcessing() {

    UUID firstId = insertNotification("NEW");
    UUID secondId = insertNotification("NEW");

    int updated = repository.markBatchAsProcessing(List.of(firstId, secondId));

    assertThat(updated).isEqualTo(2);

    assertStatus(firstId, "PROCESSING");
    assertStatus(secondId, "PROCESSING");
  }

  @Test
  void shouldIncrementAttemptsWhenMarkingAsProcessing() {

    UUID id = insertNotification("NEW");

    repository.markBatchAsProcessing(List.of(id));

    assertThat(getAttempts(id)).isEqualTo(1);

    // Повторный вызов увеличит attempts ещё раз.
    repository.markBatchAsProcessing(List.of(id));

    assertThat(getAttempts(id)).isEqualTo(2);
  }

  @Test
  void shouldSetLockedUntilWhenMarkingAsProcessing() {

    UUID id = insertNotification("NEW");

    repository.markBatchAsProcessing(List.of(id));

    Instant lockedUntil = getInstant(id, "locked_until");

    assertThat(lockedUntil).isNotNull();
    assertThat(lockedUntil).isAfter(Instant.now());
  }

  @Test
  void shouldReturnZeroWhenBatchIsEmpty() {

    int updated = repository.markBatchAsProcessing(List.of());

    assertThat(updated).isZero();
  }

  // ---------------------------------------------------------
  // markAsSent
  // ---------------------------------------------------------

  @Test
  void shouldMarkNotificationAsSent() {

    UUID id = insertNotification("PROCESSING");

    int updated = repository.markAsSent(id);

    assertThat(updated).isEqualTo(1);

    assertStatus(id, "SENT");

    assertThat(getInstant(id, "sent_at")).isNotNull();
  }

  @Test
  void shouldClearLockAndErrorWhenMarkedAsSent() {

    UUID id = insertNotification("PROCESSING");

    updateLockAndError(id);

    repository.markAsSent(id);

    assertThat(getInstant(id, "locked_until")).isNull();
    assertThat(getString(id, "last_error")).isNull();
  }

  @Test
  void shouldNotMarkNotificationAsSentWhenNotProcessing() {

    UUID id = insertNotification("NEW");

    int updated = repository.markAsSent(id);

    assertThat(updated).isZero();

    assertStatus(id, "NEW");
  }

  // ---------------------------------------------------------
  // scheduleRetry
  // ---------------------------------------------------------

  @Test
  void shouldScheduleRetry() {

    UUID id = insertNotification("PROCESSING");

    Instant nextAttemptAt = Instant.now().plusSeconds(300).truncatedTo(ChronoUnit.MICROS);
    String error = "SMTP connection failed";

    int updated = repository.scheduleRetry(id, nextAttemptAt, error);

    assertThat(updated).isEqualTo(1);

    assertStatus(id, "RETRY");

    assertThat(getInstant(id, "next_attempt_at")).isEqualTo(nextAttemptAt);

    assertThat(getString(id, "last_error")).isEqualTo(error);

    assertThat(getInstant(id, "locked_until")).isNull();
  }

  @Test
  void shouldNotScheduleRetryWhenNotProcessing() {

    UUID id = insertNotification("NEW");

    int updated = repository.scheduleRetry(id, Instant.now().plusSeconds(300), "Some error");

    assertThat(updated).isZero();

    assertStatus(id, "NEW");
  }

  // ---------------------------------------------------------
  // markAsDead
  // ---------------------------------------------------------

  @Test
  void shouldMarkNotificationAsDead() {

    UUID id = insertNotification("PROCESSING");

    String error = "Maximum retry attempts exceeded";

    int updated = repository.markAsDead(id, error);

    assertThat(updated).isEqualTo(1);

    assertStatus(id, "DEAD");

    assertThat(getString(id, "last_error")).isEqualTo(error);

    assertThat(getInstant(id, "locked_until")).isNull();
  }

  @Test
  void shouldNotMarkNotificationAsDeadWhenNotProcessing() {

    UUID id = insertNotification("NEW");

    int updated = repository.markAsDead(id, "Some error");

    assertThat(updated).isZero();

    assertStatus(id, "NEW");
  }

  @Test
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  void shouldSkipRowLockedByAnotherWorker() throws Exception {
    // Arrange
    UUID notificationId = insertNotification("NEW");

    TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

    CountDownLatch firstWorkerLockedRow = new CountDownLatch(1);
    CountDownLatch releaseFirstWorker = new CountDownLatch(1);

    ExecutorService executor = Executors.newFixedThreadPool(2);

    try {
      // Act: Worker 1 selects the row and keeps the transaction open.
      Future<List<?>> firstWorker =
          executor.submit(
              () ->
                  transactionTemplate.execute(
                      status -> {
                        List<?> batch = repository.findAvailableBatch(1);

                        firstWorkerLockedRow.countDown();

                        try {
                          if (!releaseFirstWorker.await(5, TimeUnit.SECONDS)) {
                            throw new IllegalStateException(
                                "Timed out waiting for the second worker");
                          }
                        } catch (InterruptedException exception) {
                          Thread.currentThread().interrupt();
                          throw new IllegalStateException(exception);
                        }

                        return batch;
                      }));

      // Wait until Worker 1 has acquired the row lock.
      assertThat(firstWorkerLockedRow.await(5, TimeUnit.SECONDS)).isTrue();

      // Worker 2 tries to select the same row.
      Future<List<?>> secondWorker =
          executor.submit(
              () -> transactionTemplate.execute(status -> repository.findAvailableBatch(1)));

      // Assert: Worker 2 skips the locked row.
      List<?> secondBatch = secondWorker.get(5, TimeUnit.SECONDS);

      assertThat(secondBatch).isEmpty();

      // Release Worker 1 and wait for its transaction to complete.
      releaseFirstWorker.countDown();

      List<?> firstBatch = firstWorker.get(5, TimeUnit.SECONDS);

      assertThat(firstBatch).hasSize(1);
    } finally {
      // Prevent Worker 1 from remaining blocked if an assertion fails.
      releaseFirstWorker.countDown();

      executor.shutdownNow();
      assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
    }

    // The record remains available after the first transaction commits,
    // because this test only selects records and does not change their status.
    assertThat(repository.findAvailableBatch(1)).hasSize(1);
  }

  // ---------------------------------------------------------
  // Test helpers
  // ---------------------------------------------------------

  private UUID insertNotification(String status) {
    return insertNotification(status, Instant.now().minusSeconds(60), null);
  }

  private UUID insertNotification(String status, Instant nextAttemptAt, Instant lockedUntil) {

    UUID id = UUID.randomUUID();

    Instant createdAt = Instant.now();

    jdbcTemplate.update(
        """
        INSERT INTO notification_outbox (
            id,
            event_id,
            message_id,
            chat_id,
            chat_type,
            chat_name,
            recipient_user_id,
            sender_user_id,
            sender_username,
            channel,
            status,
            attempts,
            next_attempt_at,
            locked_until,
            created_at
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """,
        id,
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        "PRIVATE",
        null,
        UUID.randomUUID(),
        UUID.randomUUID(),
        "test_sender",
        "EMAIL",
        status,
        0,
        Timestamp.from(nextAttemptAt),
        lockedUntil == null ? null : Timestamp.from(lockedUntil),
        Timestamp.from(createdAt));

    return id;
  }

  private void assertStatus(UUID id, String expectedStatus) {

    assertThat(getString(id, "status")).isEqualTo(expectedStatus);
  }

  private int getAttempts(UUID id) {

    return jdbcTemplate.queryForObject(
        """
        SELECT attempts
        FROM notification_outbox
        WHERE id = ?
        """,
        Integer.class,
        id);
  }

  private String getString(UUID id, String column) {

    return jdbcTemplate.queryForObject(
        "SELECT " + column + " FROM notification_outbox WHERE id = ?", String.class, id);
  }

  private Instant getInstant(UUID id, String column) {

    Timestamp timestamp =
        jdbcTemplate.queryForObject(
            "SELECT " + column + " FROM notification_outbox WHERE id = ?", Timestamp.class, id);

    return timestamp == null ? null : timestamp.toInstant();
  }

  private void updateLockAndError(UUID id) {

    jdbcTemplate.update(
        """
        UPDATE notification_outbox
        SET locked_until = CURRENT_TIMESTAMP + INTERVAL '5 minutes',
            last_error = 'Previous error'
        WHERE id = ?
        """,
        id);
  }
}

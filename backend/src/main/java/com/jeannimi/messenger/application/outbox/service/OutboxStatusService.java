package com.jeannimi.messenger.application.outbox.service;

import com.jeannimi.messenger.application.port.out.OutboxRepositoryPort;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OutboxStatusService {

  private static final int MAX_ATTEMPTS = 10;

  private final OutboxRepositoryPort outboxRepository;

  @Transactional
  public void markSent(Long id) {
    outboxRepository.markSent(id);
  }

  @Transactional
  public void handleFailure(
      Long id,
      int currentAttemptCount) {

    int nextAttemptCount = currentAttemptCount + 1;

    if (nextAttemptCount >= MAX_ATTEMPTS) {
      outboxRepository.registerFinalFailure(id);
      return;
    }

    Instant nextAttemptAt =
        Instant.now().plus(retryDelay(nextAttemptCount));

    outboxRepository.scheduleRetry(
        id,
        nextAttemptAt);
  }

  private Duration retryDelay(int attempt) {
    return switch (attempt) {
      case 1 -> Duration.ofSeconds(5);
      case 2 -> Duration.ofSeconds(10);
      case 3 -> Duration.ofSeconds(20);
      case 4 -> Duration.ofSeconds(40);
      default -> Duration.ofMinutes(1);
    };
  }
}
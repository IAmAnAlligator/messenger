package com.jeannimi.messenger.outbox.service;

import com.jeannimi.messenger.application.port.out.OutboxRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OutboxStatusService {

  private final OutboxRepositoryPort outboxRepository;

  @Transactional
  public void markSent(Long id) {
    outboxRepository.markSent(id);
  }

  @Transactional
  public void markFailed(Long id) {
    outboxRepository.markFailed(id);
  }
}
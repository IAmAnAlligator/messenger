package com.jeannimi.messenger.application.event.service;

import com.jeannimi.messenger.application.port.out.ProcessedEventRepositoryPort;
import com.jeannimi.messenger.domain.event.EventId;
import com.jeannimi.messenger.domain.event.ProcessedEvent;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProcessedEventService {

  private final ProcessedEventRepositoryPort processedEventRepository;

  @Transactional(readOnly = true)
  public boolean existsByEventId(EventId eventId) {
    Objects.requireNonNull(eventId, "eventId");

    return processedEventRepository.existsByEventId(eventId);
  }

  @Transactional
  public void register(EventId eventId) {
    Objects.requireNonNull(eventId, "eventId");

    processedEventRepository.save(ProcessedEvent.create(eventId));
  }

}
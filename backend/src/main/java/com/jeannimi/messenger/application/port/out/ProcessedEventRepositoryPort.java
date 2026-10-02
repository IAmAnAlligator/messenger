package com.jeannimi.messenger.application.port.out;

import com.jeannimi.messenger.domain.event.EventId;
import com.jeannimi.messenger.domain.event.ProcessedEvent;

public interface ProcessedEventRepositoryPort {

  boolean existsByEventId(EventId eventId);

  ProcessedEvent save(ProcessedEvent processedEvent);
}

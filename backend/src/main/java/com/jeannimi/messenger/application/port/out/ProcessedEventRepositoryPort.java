package com.jeannimi.messenger.application.port.out;

import com.jeannimi.messenger.domain.event.ProcessedEvent;
import java.util.UUID;

public interface ProcessedEventRepositoryPort {

  boolean existsByEventId(UUID eventId);

  ProcessedEvent save(ProcessedEvent processedEvent);
}

package com.jeannimi.messenger.application.port.out;

import com.jeannimi.messenger.domain.message.ProcessedMessage;
import java.util.UUID;

public interface ProcessedMessageRepositoryPort {

  boolean existsByEventId(UUID eventId);

  ProcessedMessage save(ProcessedMessage processedMessage);
}

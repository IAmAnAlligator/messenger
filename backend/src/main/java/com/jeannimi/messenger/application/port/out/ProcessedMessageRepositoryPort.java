package com.jeannimi.messenger.application.port.out;

import com.jeannimi.messenger.message.entity.ProcessedMessage;
import java.util.UUID;

public interface ProcessedMessageRepositoryPort {

  boolean existsByEventId(UUID eventId);

  ProcessedMessage save(ProcessedMessage processedMessage);

}

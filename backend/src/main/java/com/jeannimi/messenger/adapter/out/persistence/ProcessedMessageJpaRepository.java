package com.jeannimi.messenger.adapter.out.persistence;

import com.jeannimi.messenger.message.entity.ProcessedMessage;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedMessageJpaRepository extends JpaRepository<ProcessedMessage, Long> {

  boolean existsByEventId(UUID eventId);

}

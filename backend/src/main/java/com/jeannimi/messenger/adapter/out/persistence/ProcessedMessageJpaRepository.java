package com.jeannimi.messenger.adapter.out.persistence;

import com.jeannimi.messenger.adapter.out.persistence.entity.ProcessedMessageJpaEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedMessageJpaRepository
    extends JpaRepository<ProcessedMessageJpaEntity, UUID> {

  boolean existsByEventId(UUID eventId);
}

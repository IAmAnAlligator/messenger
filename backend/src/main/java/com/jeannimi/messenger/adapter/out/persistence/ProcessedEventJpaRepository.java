package com.jeannimi.messenger.adapter.out.persistence;

import com.jeannimi.messenger.adapter.out.persistence.entity.ProcessedEventJpaEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventJpaRepository
    extends JpaRepository<ProcessedEventJpaEntity, UUID> {

  boolean existsByEventId(UUID eventId);
}

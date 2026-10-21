package com.jeannimi.messenger.adapter.out.persistence.repository;

import com.jeannimi.messenger.adapter.out.persistence.entity.NotificationEventJpaEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationEventJpaRepository
    extends JpaRepository<NotificationEventJpaEntity, UUID> {}

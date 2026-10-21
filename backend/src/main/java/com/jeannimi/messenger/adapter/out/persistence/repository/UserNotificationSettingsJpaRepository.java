package com.jeannimi.messenger.adapter.out.persistence.repository;

import com.jeannimi.messenger.adapter.out.persistence.entity.UserNotificationSettingsJpaEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserNotificationSettingsJpaRepository
    extends JpaRepository<UserNotificationSettingsJpaEntity, UUID> {

  Optional<UserNotificationSettingsJpaEntity> findByUserId(UUID userId);
}

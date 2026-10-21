package com.jeannimi.messenger.adapter.out.persistence.repository;

import com.jeannimi.messenger.adapter.out.persistence.entity.WebPushSubscriptionJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebPushSubscriptionJpaRepository
    extends JpaRepository<WebPushSubscriptionJpaEntity, UUID> {

  List<WebPushSubscriptionJpaEntity> findAllByUserId(UUID userId);

  void deleteByUserIdAndEndpoint(UUID userId, String endpoint);

  Optional<WebPushSubscriptionJpaEntity> findByEndpoint(String endpoint);
}

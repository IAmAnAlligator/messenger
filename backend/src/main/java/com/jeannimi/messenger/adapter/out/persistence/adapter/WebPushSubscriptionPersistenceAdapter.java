package com.jeannimi.messenger.adapter.out.persistence.adapter;

import com.jeannimi.messenger.adapter.out.persistence.entity.WebPushSubscriptionJpaEntity;
import com.jeannimi.messenger.adapter.out.persistence.mapper.WebPushSubscriptionPersistenceMapper;
import com.jeannimi.messenger.adapter.out.persistence.repository.WebPushSubscriptionJpaRepository;
import com.jeannimi.messenger.application.port.out.WebPushSubscriptionRepositoryPort;
import com.jeannimi.messenger.domain.notification.WebPushSubscription;
import com.jeannimi.messenger.domain.user.UserId;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class WebPushSubscriptionPersistenceAdapter implements WebPushSubscriptionRepositoryPort {

  private final WebPushSubscriptionJpaRepository repository;
  private final WebPushSubscriptionPersistenceMapper mapper;

  @Override
  public Optional<WebPushSubscription> findByEndpoint(String endpoint) {
    Objects.requireNonNull(endpoint, "endpoint");

    return repository.findByEndpoint(endpoint).map(mapper::toDomain);
  }

  @Override
  public WebPushSubscription save(WebPushSubscription subscription) {
    Objects.requireNonNull(subscription, "subscription");

    WebPushSubscriptionJpaEntity entity = mapper.toEntity(subscription);
    WebPushSubscriptionJpaEntity saved = repository.save(entity);

    return mapper.toDomain(saved);
  }

  @Override
  public List<WebPushSubscription> findAllByUserId(UserId userId) {
    Objects.requireNonNull(userId, "userId");

    return repository.findAllByUserId(userId.value()).stream().map(mapper::toDomain).toList();
  }

  @Override
  public void deleteByUserIdAndEndpoint(UserId userId, String endpoint) {
    Objects.requireNonNull(userId, "userId");
    Objects.requireNonNull(endpoint, "endpoint");

    repository.deleteByUserIdAndEndpoint(userId.value(), endpoint);
  }
}

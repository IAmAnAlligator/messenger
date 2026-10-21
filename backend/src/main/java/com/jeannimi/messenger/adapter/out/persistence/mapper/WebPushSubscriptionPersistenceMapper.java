package com.jeannimi.messenger.adapter.out.persistence.mapper;

import com.jeannimi.messenger.adapter.out.persistence.entity.WebPushSubscriptionJpaEntity;
import com.jeannimi.messenger.domain.notification.WebPushSubscription;
import com.jeannimi.messenger.domain.notification.WebPushSubscriptionId;
import com.jeannimi.messenger.domain.user.UserId;
import org.springframework.stereotype.Component;

@Component
public class WebPushSubscriptionPersistenceMapper {

  public WebPushSubscription toDomain(WebPushSubscriptionJpaEntity entity) {
    if (entity == null) {
      return null;
    }

    return WebPushSubscription.reconstitute(
        new WebPushSubscriptionId(entity.getId()),
        new UserId(entity.getUserId()),
        entity.getEndpoint(),
        entity.getP256dh(),
        entity.getAuth(),
        entity.getCreatedAt(),
        entity.getLastUsedAt());
  }

  public WebPushSubscriptionJpaEntity toEntity(WebPushSubscription subscription) {
    if (subscription == null) {
      return null;
    }

    return new WebPushSubscriptionJpaEntity(
        subscription.getId().value(),
        subscription.getUserId().value(),
        subscription.getEndpoint(),
        subscription.getP256dh(),
        subscription.getAuth(),
        subscription.getCreatedAt(),
        subscription.getLastUsedAt());
  }
}

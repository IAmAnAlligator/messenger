package com.jeannimi.messenger.application.notification.service;

import com.jeannimi.messenger.application.notification.dto.WebPushSubscriptionResult;
import com.jeannimi.messenger.application.port.in.WebPushSubscriptionUseCase;
import com.jeannimi.messenger.application.port.out.IdGenerator;
import com.jeannimi.messenger.application.port.out.WebPushSubscriptionRepositoryPort;
import com.jeannimi.messenger.domain.notification.WebPushSubscription;
import com.jeannimi.messenger.domain.notification.WebPushSubscriptionId;
import com.jeannimi.messenger.domain.user.UserId;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WebPushSubscriptionService implements WebPushSubscriptionUseCase {

  private final WebPushSubscriptionRepositoryPort subscriptionRepository;
  private final IdGenerator idGenerator;

  @Override
  @Transactional
  public WebPushSubscriptionResult register(
      UserId userId, String endpoint, String p256dh, String auth) {

    Objects.requireNonNull(userId, "userId");

    String validatedEndpoint = requireNotBlank(endpoint, "endpoint");
    String validatedP256dh = requireNotBlank(p256dh, "p256dh");
    String validatedAuth = requireNotBlank(auth, "auth");

    WebPushSubscription subscription =
        subscriptionRepository
            .findByEndpoint(validatedEndpoint)
            .map(
                existing ->
                    WebPushSubscription.reconstitute(
                        existing.getId(),
                        userId,
                        validatedEndpoint,
                        validatedP256dh,
                        validatedAuth,
                        existing.getCreatedAt(),
                        existing.getLastUsedAt()))
            .orElseGet(
                () ->
                    WebPushSubscription.create(
                        new WebPushSubscriptionId(idGenerator.generate()),
                        userId,
                        validatedEndpoint,
                        validatedP256dh,
                        validatedAuth));

    return toResult(subscriptionRepository.save(subscription));
  }

  @Override
  @Transactional(readOnly = true)
  public List<WebPushSubscriptionResult> getSubscriptions(UserId userId) {
    Objects.requireNonNull(userId, "userId");

    return subscriptionRepository.findAllByUserId(userId).stream().map(this::toResult).toList();
  }

  @Override
  @Transactional
  public void delete(UserId userId, String endpoint) {
    Objects.requireNonNull(userId, "userId");

    subscriptionRepository.deleteByUserIdAndEndpoint(userId, requireNotBlank(endpoint, "endpoint"));
  }

  private String requireNotBlank(String value, String field) {
    Objects.requireNonNull(value, field);

    if (value.isBlank()) {
      throw new IllegalArgumentException(field + " must not be blank");
    }

    return value;
  }

  private WebPushSubscriptionResult toResult(WebPushSubscription subscription) {
    return new WebPushSubscriptionResult(
        subscription.getId().value(),
        subscription.getEndpoint(),
        subscription.getCreatedAt(),
        subscription.getLastUsedAt());
  }
}

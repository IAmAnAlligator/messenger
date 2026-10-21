package com.jeannimi.messenger.domain.notification;

import com.jeannimi.messenger.domain.user.UserId;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;

@Getter
public final class WebPushSubscription {

  private final WebPushSubscriptionId id;
  private final UserId userId;
  private final String endpoint;
  private final String p256dh;
  private final String auth;
  private final Instant createdAt;
  private final Instant lastUsedAt;

  private WebPushSubscription(
      WebPushSubscriptionId id,
      UserId userId,
      String endpoint,
      String p256dh,
      String auth,
      Instant createdAt,
      Instant lastUsedAt) {

    this.id = Objects.requireNonNull(id, "id");
    this.userId = Objects.requireNonNull(userId, "userId");
    this.endpoint = requireNotBlank(endpoint, "endpoint");
    this.p256dh = requireNotBlank(p256dh, "p256dh");
    this.auth = requireNotBlank(auth, "auth");
    this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    this.lastUsedAt = lastUsedAt;
  }

  public static WebPushSubscription create(
      WebPushSubscriptionId id, UserId userId, String endpoint, String p256dh, String auth) {

    return new WebPushSubscription(id, userId, endpoint, p256dh, auth, Instant.now(), null);
  }

  public static WebPushSubscription reconstitute(
      WebPushSubscriptionId id,
      UserId userId,
      String endpoint,
      String p256dh,
      String auth,
      Instant createdAt,
      Instant lastUsedAt) {

    return new WebPushSubscription(id, userId, endpoint, p256dh, auth, createdAt, lastUsedAt);
  }

  private static String requireNotBlank(String value, String field) {
    Objects.requireNonNull(value, field);

    if (value.isBlank()) {
      throw new IllegalArgumentException(field + " must not be blank");
    }

    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof WebPushSubscription that)) {
      return false;
    }

    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}

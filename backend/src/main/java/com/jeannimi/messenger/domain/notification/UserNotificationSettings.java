package com.jeannimi.messenger.domain.notification;

import com.jeannimi.messenger.domain.user.UserId;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;

@Getter
public final class UserNotificationSettings {

  private final UserId userId;
  private final boolean webPushEnabled;
  private final Instant createdAt;
  private final Instant updatedAt;

  private UserNotificationSettings(
      UserId userId, boolean webPushEnabled, Instant createdAt, Instant updatedAt) {

    this.userId = Objects.requireNonNull(userId, "userId");
    this.webPushEnabled = webPushEnabled;
    this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
  }

  public static UserNotificationSettings create(UserId userId) {
    Instant now = Instant.now();

    return new UserNotificationSettings(userId, false, now, now);
  }

  public static UserNotificationSettings reconstitute(
      UserId userId, boolean webPushEnabled, Instant createdAt, Instant updatedAt) {

    return new UserNotificationSettings(userId, webPushEnabled, createdAt, updatedAt);
  }

  public UserNotificationSettings withWebPushEnabled(boolean enabled) {
    return new UserNotificationSettings(userId, enabled, createdAt, Instant.now());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof UserNotificationSettings that)) {
      return false;
    }

    return userId.equals(that.userId);
  }

  @Override
  public int hashCode() {
    return userId.hashCode();
  }
}

package com.jeannimi.messenger.domain.notification;

import com.jeannimi.messenger.domain.user.UserId;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;

@Getter
public final class UserNotificationSettings {

  private final UserId userId;

  private boolean webPushPrivateChatsEnabled;
  private boolean webPushGroupChatsEnabled;

  private boolean emailPrivateChatsEnabled;
  private boolean emailGroupChatsEnabled;

  private final Instant createdAt;
  private Instant updatedAt;

  private UserNotificationSettings(
      UserId userId,
      boolean webPushPrivateChatsEnabled,
      boolean webPushGroupChatsEnabled,
      boolean emailPrivateChatsEnabled,
      boolean emailGroupChatsEnabled,
      Instant createdAt,
      Instant updatedAt) {

    this.userId = Objects.requireNonNull(userId, "userId");

    this.webPushPrivateChatsEnabled = webPushPrivateChatsEnabled;
    this.webPushGroupChatsEnabled = webPushGroupChatsEnabled;

    this.emailPrivateChatsEnabled = emailPrivateChatsEnabled;
    this.emailGroupChatsEnabled = emailGroupChatsEnabled;

    this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
  }

  public static UserNotificationSettings create(UserId userId) {
    Instant now = Instant.now();

    return new UserNotificationSettings(userId, false, false, false, false, now, now);
  }

  public static UserNotificationSettings reconstitute(
      UserId userId,
      boolean webPushPrivateChatsEnabled,
      boolean webPushGroupChatsEnabled,
      boolean emailPrivateChatsEnabled,
      boolean emailGroupChatsEnabled,
      Instant createdAt,
      Instant updatedAt) {

    return new UserNotificationSettings(
        userId,
        webPushPrivateChatsEnabled,
        webPushGroupChatsEnabled,
        emailPrivateChatsEnabled,
        emailGroupChatsEnabled,
        createdAt,
        updatedAt);
  }

  public void enableWebPushPrivateChats(boolean enabled) {
    if (webPushPrivateChatsEnabled == enabled) {
      return;
    }

    this.webPushPrivateChatsEnabled = enabled;
    updateTimestamp();
  }

  public void enableWebPushGroupChats(boolean enabled) {
    if (webPushGroupChatsEnabled == enabled) {
      return;
    }

    this.webPushGroupChatsEnabled = enabled;
    updateTimestamp();
  }

  public void enableEmailPrivateChats(boolean enabled) {
    if (emailPrivateChatsEnabled == enabled) {
      return;
    }

    this.emailPrivateChatsEnabled = enabled;
    updateTimestamp();
  }

  public void enableEmailGroupChats(boolean enabled) {
    if (emailGroupChatsEnabled == enabled) {
      return;
    }

    this.emailGroupChatsEnabled = enabled;
    updateTimestamp();
  }

  private void updateTimestamp() {
    this.updatedAt = Instant.now();
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

package com.jeannimi.messenger.adapter.in.web.notification.dto;

public record UpdateNotificationSettingsRequest(
    Boolean webPushPrivateChatsEnabled,
    Boolean webPushGroupChatsEnabled,
    Boolean emailPrivateChatsEnabled,
    Boolean emailGroupChatsEnabled) {

  public boolean isEmpty() {
    return webPushPrivateChatsEnabled == null
        && webPushGroupChatsEnabled == null
        && emailPrivateChatsEnabled == null
        && emailGroupChatsEnabled == null;
  }
}

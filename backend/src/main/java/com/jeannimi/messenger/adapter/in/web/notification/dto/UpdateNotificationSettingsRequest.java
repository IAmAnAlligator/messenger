package com.jeannimi.messenger.adapter.in.web.notification.dto;

public record UpdateNotificationSettingsRequest(
    Boolean webPushEnabled,
    Boolean emailEnabled) {

  public boolean isEmpty() {
    return webPushEnabled == null && emailEnabled == null;
  }
}
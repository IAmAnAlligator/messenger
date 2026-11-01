package com.jeannimi.messenger.domain.notification;

public enum NotificationOutboxStatus {
  NEW,
  PROCESSING,
  SENT,
  SKIPPED,
  RETRY,
  DEAD
}
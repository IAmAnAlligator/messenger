package com.jeannimi.messenger.adapter.out.persistence;

import com.jeannimi.messenger.application.outbox.OutboxStatus;

public final class OutboxStatusMapper {

  private OutboxStatusMapper() {}

  public static com.jeannimi.messenger.outbox.entity.OutboxStatus toEntity(
      OutboxStatus status) {

    return switch (status) {
      case NEW -> com.jeannimi.messenger.outbox.entity.OutboxStatus.NEW;
      case SENT -> com.jeannimi.messenger.outbox.entity.OutboxStatus.SENT;
      case FAILED -> com.jeannimi.messenger.outbox.entity.OutboxStatus.FAILED;
    };
  }

  public static OutboxStatus toData(
      com.jeannimi.messenger.outbox.entity.OutboxStatus status) {

    return switch (status) {
      case NEW -> OutboxStatus.NEW;
      case SENT -> OutboxStatus.SENT;
      case FAILED -> OutboxStatus.FAILED;
    };
  }
}
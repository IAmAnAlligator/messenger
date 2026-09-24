package com.jeannimi.messenger.adapter.in.scheduler;

import com.jeannimi.messenger.application.outbox.service.OutboxPublishService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxPublisher {

  private final OutboxPublishService outboxPublishService;

  @Scheduled(fixedDelay = 1000)
  public void publishOutboxEvents() {
    outboxPublishService.publishBatch();
  }

}
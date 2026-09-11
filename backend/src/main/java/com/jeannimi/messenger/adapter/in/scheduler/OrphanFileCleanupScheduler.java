package com.jeannimi.messenger.adapter.in.scheduler;

import com.jeannimi.messenger.application.file.service.OrphanFileCleanupService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrphanFileCleanupScheduler {

  private final OrphanFileCleanupService cleanupService;
  private final FileCleanupProperties properties;

  @Scheduled(fixedDelayString = "${file-storage.cleanup.interval}")
  public void cleanup() {

    if (properties.enabled()) {
      cleanupService.cleanupOrphanFiles(properties.orphanAge());
    }

  }
}
package com.jeannimi.messenger.adapter.in.scheduler;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "file-storage.cleanup")
public record FileCleanupProperties(
    boolean enabled,
    Duration interval,
    Duration orphanAge) {}
package com.jeannimi.messenger.application.port.out;

import java.time.Instant;

public record StoredFileInfo(
    String storageFileName,
    Instant lastModified) {}
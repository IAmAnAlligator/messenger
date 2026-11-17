package com.jeannimi.messenger.application.port.out.file;

import java.time.Instant;

public record StoredFileInfo(String storageFileName, Instant lastModified) {}

package com.jeannimi.messenger.application.message.dto;

import java.io.InputStream;

public record FileDownloadResult(
    InputStream inputStream,
    String originalFileName,
    String contentType,
    long size) {
}

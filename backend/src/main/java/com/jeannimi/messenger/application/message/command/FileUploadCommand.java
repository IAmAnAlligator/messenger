package com.jeannimi.messenger.application.message.command;

import java.io.InputStream;

public record FileUploadCommand(
    InputStream inputStream,
    String originalFileName,
    String contentType,
    long size) {
}

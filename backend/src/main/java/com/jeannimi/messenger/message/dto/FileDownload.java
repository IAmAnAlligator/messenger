package com.jeannimi.messenger.message.dto;

import java.io.InputStream;

public record FileDownload(
    InputStream inputStream,
    String fileName,
    String contentType,
    long size
) {}
package com.jeannimi.messenger.message.dto;

import java.io.InputStream;

public record FileUpload(
    InputStream inputStream, String originalFileName, String contentType, long size) {}

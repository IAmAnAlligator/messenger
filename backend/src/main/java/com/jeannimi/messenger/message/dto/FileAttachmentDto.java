package com.jeannimi.messenger.message.dto;

import java.util.UUID;

public record FileAttachmentDto(
    UUID id,
    String originalFileName,
    String contentType,
    Long size,
    String url) {}
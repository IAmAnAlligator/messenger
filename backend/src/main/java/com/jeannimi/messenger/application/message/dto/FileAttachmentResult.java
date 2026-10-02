package com.jeannimi.messenger.application.message.dto;

import com.jeannimi.messenger.domain.message.FileAttachmentId;

public record FileAttachmentResult(
    FileAttachmentId id, String originalFileName, String contentType, long size) {}

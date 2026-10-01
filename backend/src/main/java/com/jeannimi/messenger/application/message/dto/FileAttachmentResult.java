package com.jeannimi.messenger.application.message.dto;

import com.jeannimi.messenger.domain.message.FileAttachmentId;
import java.util.UUID;

public record FileAttachmentResult(
    FileAttachmentId id, String originalFileName, String contentType, long size) {}

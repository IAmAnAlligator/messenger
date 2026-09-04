package com.jeannimi.messenger.application.message.dto;

import java.util.UUID;

public record FileAttachmentResult(
    UUID id,
    String originalFileName,
    String contentType,
    long size) {

}

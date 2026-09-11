package com.jeannimi.messenger.domain.message;

import com.jeannimi.messenger.common.exception_handling.MessageError;
import com.jeannimi.messenger.common.exception_handling.MessageException;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public final class FileAttachment {

  private final UUID id;
  private final String originalFileName;
  private final String storageFileName;
  private final String contentType;
  private final Long size;
  private final Instant createdAt;

  private FileAttachment(
      UUID id,
      String originalFileName,
      String storageFileName,
      String contentType,
      Long size,
      Instant createdAt) {

    this.id = id;
    this.originalFileName = originalFileName;
    this.storageFileName = storageFileName;
    this.contentType = contentType;
    this.size = size;
    this.createdAt = createdAt;
  }

  public static FileAttachment create(
      String originalFileName,
      String storageFileName,
      String contentType,
      Long size) {

    validateOriginalFileName(originalFileName);
    validateStorageFileName(storageFileName);
    validateContentType(contentType);
    validateSize(size);

    return new FileAttachment(
        UUID.randomUUID(),
        originalFileName,
        storageFileName,
        contentType,
        size,
        Instant.now());
  }

  public static FileAttachment reconstitute(
      UUID id,
      String originalFileName,
      String storageFileName,
      String contentType,
      Long size,
      Instant createdAt) {

    return new FileAttachment(
        Objects.requireNonNull(id, "id"),
        originalFileName,
        storageFileName,
        contentType,
        size,
        Objects.requireNonNull(createdAt, "createdAt"));
  }

  private static void validateOriginalFileName(String fileName) {

    if (fileName == null || fileName.isBlank()) {
      throw new MessageException(
          MessageError.FILE_NAME_INVALID,
          "File name must not be empty");
    }

    if (fileName.length() > 255) {
      throw new MessageException(
          MessageError.FILE_NAME_TOO_LONG,
          "File name is too long");
    }
  }

  private static void validateStorageFileName(String fileName) {

    if (fileName == null || fileName.isBlank()) {
      throw new MessageException(
          MessageError.FILE_STORAGE_NAME_INVALID,
          "Storage file name must not be empty");
    }

    if (fileName.length() > 255) {
      throw new MessageException(
          MessageError.FILE_STORAGE_NAME_TOO_LONG,
          "Storage file name is too long");
    }
  }

  private static void validateContentType(String contentType) {

    if (contentType == null || contentType.isBlank()) {
      throw new MessageException(
          MessageError.FILE_CONTENT_TYPE_INVALID,
          "Content type must not be empty");
    }

    if (contentType.length() > 255) {
      throw new MessageException(
          MessageError.FILE_CONTENT_TYPE_TOO_LONG,
          "Content type is too long");
    }
  }

  private static void validateSize(Long size) {

    if (size == null || size <= 0) {
      throw new MessageException(
          MessageError.FILE_EMPTY,
          "File must not be empty");
    }

    if (size > FileAttachmentConstants.MAX_FILE_SIZE_BYTES) {
      throw new MessageException(
          MessageError.FILE_TOO_LARGE,
          "File size exceeds 10 MB");
    }
  }
}
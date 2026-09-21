package com.jeannimi.messenger.domain.message;

import com.jeannimi.messenger.domain.exception.MessageError;
import com.jeannimi.messenger.domain.exception.MessageException;
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

    this.id = Objects.requireNonNull(id, "id");
    this.originalFileName = Objects.requireNonNull(originalFileName, "originalFileName");
    this.storageFileName = Objects.requireNonNull(storageFileName, "storageFileName");
    this.contentType = Objects.requireNonNull(contentType, "contentType");
    this.size = Objects.requireNonNull(size, "size");
    this.createdAt = Objects.requireNonNull(createdAt, "createdAt");

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

    validateOriginalFileName(originalFileName);
    validateStorageFileName(storageFileName);
    validateContentType(contentType);
    validateSize(size);

    return new FileAttachment(
        id,
        originalFileName,
        storageFileName,
        contentType,
        size,
        createdAt);
  }

  private static void validateOriginalFileName(String fileName) {

    if (fileName == null || fileName.isBlank()) {
      throw new MessageException(
          MessageError.FILE_NAME_INVALID,
          "File name must not be empty");
    }

    if (fileName.length() > FileAttachmentConstants.MAX_FILE_NAME_LENGTH) {
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

    if (fileName.length() > FileAttachmentConstants.MAX_STORAGE_FILE_NAME_LENGTH) {
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

    if (contentType.length() > FileAttachmentConstants.MAX_CONTENT_TYPE_LENGTH) {
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
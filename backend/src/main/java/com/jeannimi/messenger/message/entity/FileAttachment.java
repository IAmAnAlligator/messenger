package com.jeannimi.messenger.message.entity;

import com.jeannimi.messenger.common.exception_handling.MessageError;
import com.jeannimi.messenger.common.exception_handling.MessageException;
import com.jeannimi.messenger.message.MessageConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "file_attachments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileAttachment {

  @Id
  @Column(name = "id")
  @UuidGenerator
  private UUID id;

  @Column(name = "original_file_name", nullable = false)
  private String originalFileName;

  @Column(name = "storage_file_name", nullable = false, unique = true)
  private String storageFileName;

  @Column(name = "content_type", nullable = false)
  private String contentType;

  @Column(name = "size", nullable = false)
  private Long size;

  @Column(name = "storage_path", nullable = false, length = 500)
  private String storagePath;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  public static FileAttachment create(
      String originalFileName,
      String storageFileName,
      String contentType,
      Long size,
      String storagePath) {

    validateOriginalFileName(originalFileName);
    validateStorageFileName(storageFileName);
    validateContentType(contentType);
    validateSize(size);
    validateStoragePath(storagePath);

    FileAttachment fileAttachment = new FileAttachment();

    fileAttachment.originalFileName = originalFileName;
    fileAttachment.storageFileName = storageFileName;
    fileAttachment.contentType = contentType;
    fileAttachment.size = size;
    fileAttachment.storagePath = storagePath;
    fileAttachment.createdAt = Instant.now();

    return fileAttachment;
  }

  private static void validateOriginalFileName(String fileName) {

    if (fileName == null || fileName.isBlank()) {
      throw new MessageException(MessageError.FILE_NAME_INVALID, "File name must not be empty");
    }

    if (fileName.length() > 255) {
      throw new MessageException(MessageError.FILE_NAME_TOO_LONG, "File name is too long");
    }
  }

  private static void validateStorageFileName(String fileName) {

    if (fileName == null || fileName.isBlank()) {
      throw new MessageException(
          MessageError.FILE_STORAGE_NAME_INVALID, "Storage file name must not be empty");
    }

    if (fileName.length() > 255) {
      throw new MessageException(
          MessageError.FILE_STORAGE_NAME_TOO_LONG, "Storage file name is too long");
    }
  }

  private static void validateContentType(String contentType) {

    if (contentType == null || contentType.isBlank()) {
      throw new MessageException(
          MessageError.FILE_CONTENT_TYPE_INVALID, "Content type must not be empty");
    }

    if (contentType.length() > 255) {
      throw new MessageException(
          MessageError.FILE_CONTENT_TYPE_TOO_LONG, "Content type is too long");
    }
  }

  private static void validateSize(Long size) {

    if (size == null || size <= 0) {
      throw new MessageException(MessageError.FILE_EMPTY, "File must not be empty");
    }

    if (size > MessageConstants.MAX_FILE_SIZE_BYTES) {
      throw new MessageException(MessageError.FILE_TOO_LARGE, "File size exceeds 10 MB");
    }
  }

  private static void validateStoragePath(String storagePath) {

    if (storagePath == null || storagePath.isBlank()) {
      throw new MessageException(
          MessageError.FILE_STORAGE_PATH_INVALID, "Storage path must not be empty");
    }

    if (storagePath.length() > 500) {
      throw new MessageException(
          MessageError.FILE_STORAGE_PATH_TOO_LONG, "Storage path is too long");
    }
  }
}

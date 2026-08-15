package com.jeannimi.messenger.message.entity;

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
      String storagePath
  ) {

    FileAttachment fileAttachment = new FileAttachment();

    fileAttachment.originalFileName = originalFileName;
    fileAttachment.storageFileName = storageFileName;
    fileAttachment.contentType = contentType;
    fileAttachment.size = size;
    fileAttachment.storagePath = storagePath;
    fileAttachment.createdAt = Instant.now();

    return fileAttachment;
  }
}

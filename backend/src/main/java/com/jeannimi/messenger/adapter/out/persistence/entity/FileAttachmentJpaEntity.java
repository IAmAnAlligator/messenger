package com.jeannimi.messenger.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "file_attachments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileAttachmentJpaEntity {

  @Id
  @Column(name = "id")
  private UUID id;

  @Column(name = "original_file_name", nullable = false, length = 255)
  private String originalFileName;

  @Column(name = "storage_file_name", nullable = false, unique = true, length = 255)
  private String storageFileName;

  @Column(name = "content_type", nullable = false, length = 255)
  private String contentType;

  @Column(name = "size", nullable = false)
  private Long size;

  @Column(name = "storage_path", nullable = false, length = 500)
  private String storagePath;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  public FileAttachmentJpaEntity(
      UUID id,
      String originalFileName,
      String storageFileName,
      String contentType,
      Long size,
      String storagePath,
      Instant createdAt) {

    this.id = id;
    this.originalFileName = originalFileName;
    this.storageFileName = storageFileName;
    this.contentType = contentType;
    this.size = size;
    this.storagePath = storagePath;
    this.createdAt = createdAt;
  }
}

package com.jeannimi.messenger.adapter.out.persistence.mapper;

import com.jeannimi.messenger.adapter.out.persistence.entity.FileAttachmentJpaEntity;
import com.jeannimi.messenger.domain.message.FileAttachment;
import org.springframework.stereotype.Component;

@Component
public class FileAttachmentPersistenceMapper {

  public FileAttachment toDomain(FileAttachmentJpaEntity entity) {

    if (entity == null) {
      return null;
    }

    return FileAttachment.reconstitute(
        entity.getId(),
        entity.getOriginalFileName(),
        entity.getStorageFileName(),
        entity.getContentType(),
        entity.getSize(),
        entity.getCreatedAt());
  }

  public FileAttachmentJpaEntity toEntity(FileAttachment attachment) {

    if (attachment == null) {
      return null;
    }

    return new FileAttachmentJpaEntity(
        attachment.getId(),
        attachment.getOriginalFileName(),
        attachment.getStorageFileName(),
        attachment.getContentType(),
        attachment.getSize(),
        attachment.getCreatedAt());
  }
}

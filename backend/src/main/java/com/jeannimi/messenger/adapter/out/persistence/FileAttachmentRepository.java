package com.jeannimi.messenger.adapter.out.persistence;

import com.jeannimi.messenger.adapter.out.persistence.mapper.FileAttachmentPersistenceMapper;
import com.jeannimi.messenger.application.port.out.FileAttachmentRepositoryPort;
import com.jeannimi.messenger.domain.message.FileAttachment;
import com.jeannimi.messenger.domain.message.FileAttachmentId;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FileAttachmentRepository implements FileAttachmentRepositoryPort {

  private final FileAttachmentJpaRepository fileAttachmentJpaRepository;
  private final FileAttachmentPersistenceMapper fileAttachmentPersistenceMapper;

  @Override
  public List<String> findAllStorageFileNames() {

    return fileAttachmentJpaRepository.findAllStorageFileNames();
  }

  @Override
  public List<FileAttachment> findOrphanAttachments() {

    return fileAttachmentJpaRepository.findOrphanAttachments().stream()
        .map(fileAttachmentPersistenceMapper::toDomain)
        .toList();
  }

  @Override
  public int deleteAllByIds(List<FileAttachmentId> ids) {
    if (ids == null || ids.isEmpty()) {
      return 0;
    }

    List<UUID> uuidIds = ids.stream().map(FileAttachmentId::value).toList();

    return fileAttachmentJpaRepository.deleteAllByIds(uuidIds);
  }

  @Override
  public void delete(FileAttachment fileAttachment) {

    if (fileAttachment == null) {
      return;
    }

    fileAttachmentJpaRepository.deleteById(fileAttachment.getId().value());

  }
}

package com.jeannimi.messenger.adapter.out.persistence;

import com.jeannimi.messenger.adapter.out.persistence.mapper.FileAttachmentPersistenceMapper;
import com.jeannimi.messenger.application.port.out.FileAttachmentRepositoryPort;
import com.jeannimi.messenger.domain.message.FileAttachment;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class FileAttachmentRepository implements FileAttachmentRepositoryPort {

  private final FileAttachmentJpaRepository fileAttachmentJpaRepository;
  private final FileAttachmentPersistenceMapper fileAttachmentPersistenceMapper;

  @Override
  @Transactional(readOnly = true)
  public List<String> findAllStorageFileNames() {

    return fileAttachmentJpaRepository.findAllStorageFileNames();
  }

  @Override
  @Transactional(readOnly = true)
  public List<FileAttachment> findOrphanAttachments() {

    return fileAttachmentJpaRepository.findOrphanAttachments().stream()
        .map(fileAttachmentPersistenceMapper::toDomain)
        .toList();
  }

  @Override
  @Transactional
  public void deleteAllByIds(List<UUID> ids) {

    if (ids == null || ids.isEmpty()) {
      return;
    }

    fileAttachmentJpaRepository.deleteAllByIds(ids);
  }

  @Override
  @Transactional
  public void delete(FileAttachment fileAttachment) {

    if (fileAttachment == null || fileAttachment.getId() == null) {
      return;
    }

    fileAttachmentJpaRepository.deleteById(fileAttachment.getId());
  }
}

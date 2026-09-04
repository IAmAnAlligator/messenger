package com.jeannimi.messenger.adapter.out.persistence;

import com.jeannimi.messenger.application.port.out.FileAttachmentRepositoryPort;
import com.jeannimi.messenger.message.entity.FileAttachment;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FileAttachmentRepository implements FileAttachmentRepositoryPort {

  private final FileAttachmentJpaRepository fileAttachmentJpaRepository;


  @Override
  public List<String> findAllStorageFileNames() {
    return fileAttachmentJpaRepository.findAllStorageFileNames();
  }

  @Override
  public List<FileAttachment> findOrphanAttachments() {
    return fileAttachmentJpaRepository.findOrphanAttachments();
  }

  @Override
  public void deleteAllByIds(List<UUID> ids) {
    fileAttachmentJpaRepository.deleteAllByIds(ids);
  }

  @Override
  public void delete(FileAttachment fileAttachment) {
    fileAttachmentJpaRepository.delete(fileAttachment);
  }
}

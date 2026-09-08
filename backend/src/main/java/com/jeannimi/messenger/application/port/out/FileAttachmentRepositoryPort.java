package com.jeannimi.messenger.application.port.out;

import com.jeannimi.messenger.domain.message.FileAttachment;
import java.util.List;
import java.util.UUID;

public interface FileAttachmentRepositoryPort {

  List<String> findAllStorageFileNames();

  List<FileAttachment> findOrphanAttachments();

  void deleteAllByIds(List<UUID> ids);

  void delete(FileAttachment fileAttachment);
}

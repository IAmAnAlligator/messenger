package com.jeannimi.messenger.application.port.out;

import com.jeannimi.messenger.domain.message.FileAttachment;
import com.jeannimi.messenger.domain.message.FileAttachmentId;
import java.util.List;

public interface FileAttachmentRepositoryPort {

  List<String> findAllStorageFileNames();

  List<FileAttachment> findOrphanAttachments();

  int deleteAllByIds(List<FileAttachmentId> ids);

  void delete(FileAttachment fileAttachment);
}

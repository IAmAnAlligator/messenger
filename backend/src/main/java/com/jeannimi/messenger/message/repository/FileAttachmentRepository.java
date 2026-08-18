package com.jeannimi.messenger.message.repository;

import com.jeannimi.messenger.message.entity.FileAttachment;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileAttachmentRepository extends JpaRepository<FileAttachment, UUID> {}

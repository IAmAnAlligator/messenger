package com.jeannimi.messenger.application.file.service;

import com.jeannimi.messenger.application.port.out.FileAttachmentRepositoryPort;
import com.jeannimi.messenger.application.port.out.FileStorageMaintenancePort;
import com.jeannimi.messenger.application.port.out.FileStoragePort;
import com.jeannimi.messenger.application.port.out.StoredFileInfo;
import com.jeannimi.messenger.domain.message.FileAttachment;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class OrphanFileCleanupService {

  private final FileStoragePort fileStoragePort;
  private final FileStorageMaintenancePort fileStorageMaintenancePort;
  private final FileAttachmentRepositoryPort fileAttachmentRepositoryPort;

  public OrphanFileCleanupService(
      FileStoragePort fileStoragePort,
      FileStorageMaintenancePort fileStorageMaintenancePort,
      FileAttachmentRepositoryPort fileAttachmentRepositoryPort) {

    this.fileStoragePort = fileStoragePort;
    this.fileStorageMaintenancePort = fileStorageMaintenancePort;
    this.fileAttachmentRepositoryPort = fileAttachmentRepositoryPort;
  }

  @Transactional
  public void cleanupOrphanFiles(Duration orphanAge) {

    log.info("Starting orphan file cleanup");

    Instant threshold = Instant.now().minus(orphanAge);

    List<StoredFileInfo> filesInStorage =
        fileStorageMaintenancePort.listFiles();

    log.info("Storage contains {} files", filesInStorage.size());

    int deletedFiles =
        deleteOrphanFiles(filesInStorage, threshold);

    List<StoredFileInfo> currentFilesInStorage =
        fileStorageMaintenancePort.listFiles();

    int deletedAttachments =
        deleteOrphanAttachments(currentFilesInStorage);

    log.info(
        "Orphan file cleanup finished. "
            + "Deleted files: {}, deleted DB attachments: {}",
        deletedFiles,
        deletedAttachments);
  }

  private int deleteOrphanFiles(
      List<StoredFileInfo> filesInStorage,
      Instant threshold) {

    Set<String> filesInDatabase =
        new HashSet<>(
            fileAttachmentRepositoryPort.findAllStorageFileNames());

    int deletedFiles = 0;

    for (StoredFileInfo file : filesInStorage) {

      String storageFileName = file.storageFileName();

      if (filesInDatabase.contains(storageFileName)) {

        log.debug(
            "File is registered in DB: {}",
            storageFileName);

      } else if (file.lastModified().isBefore(threshold)) {

        if (deletePhysicalFile(storageFileName)) {
          deletedFiles++;
        }

      } else {

        log.debug(
            "Orphan file is too young, keeping it: {}",
            storageFileName);
      }
    }

    return deletedFiles;
  }

  private boolean deletePhysicalFile(String storageFileName) {

    try {

      fileStoragePort.delete(storageFileName);

      log.info(
          "Deleted orphan file: {}",
          storageFileName);

      return true;

    } catch (Exception e) {

      log.error(
          "Failed to delete orphan file: {}",
          storageFileName,
          e);

      return false;
    }
  }

  private int deleteOrphanAttachments(
      List<StoredFileInfo> filesInStorage) {

    Set<String> filesOnDisk =
        filesInStorage.stream()
            .map(StoredFileInfo::storageFileName)
            .collect(Collectors.toSet());

    List<FileAttachment> orphanAttachments =
        fileAttachmentRepositoryPort.findOrphanAttachments();

    int deletedAttachments = 0;

    for (FileAttachment attachment : orphanAttachments) {

      String storageFileName =
          attachment.getStorageFileName();

      if (!filesOnDisk.contains(storageFileName)) {

        try {

          fileAttachmentRepositoryPort.delete(attachment);
          deletedAttachments++;

          log.info(
              "Deleted orphan DB attachment: {}",
              storageFileName);

        } catch (Exception e) {

          log.error(
              "Failed to delete orphan DB attachment: {}",
              storageFileName,
              e);
        }

      } else {

        log.debug(
            "Orphan DB attachment still has physical file: {}",
            storageFileName);
      }
    }

    return deletedAttachments;
  }
}
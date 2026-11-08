package com.jeannimi.messenger.application.file.service;

import com.jeannimi.messenger.application.port.out.FileAttachmentRepositoryPort;
import com.jeannimi.messenger.application.port.out.FileStorageMaintenancePort;
import com.jeannimi.messenger.application.port.out.FileStoragePort;
import com.jeannimi.messenger.application.port.out.StoredFileInfo;
import com.jeannimi.messenger.domain.message.FileAttachment;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

  private static final String THUMBNAIL_SUFFIX = ".thumbnail.jpg";

  private String getOriginalFileName(String fileName) {

    if (fileName.endsWith(THUMBNAIL_SUFFIX)) {
      return fileName.substring(0, fileName.length() - THUMBNAIL_SUFFIX.length());
    }

    return fileName;
  }

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

    List<StoredFileInfo> filesInStorage = fileStorageMaintenancePort.listFiles();

    log.info("Storage contains {} files", filesInStorage.size());

    int deletedFiles = deleteOrphanFiles(filesInStorage, threshold);

    List<StoredFileInfo> currentFilesInStorage = fileStorageMaintenancePort.listFiles();

    int deletedAttachments = deleteOrphanAttachments(currentFilesInStorage);

    log.info(
        "Orphan file cleanup finished. " + "Deleted files: {}, deleted DB attachments: {}",
        deletedFiles,
        deletedAttachments);
  }

  private int deleteOrphanFiles(List<StoredFileInfo> filesInStorage, Instant threshold) {

    Set<String> filesInDatabase =
        new HashSet<>(fileAttachmentRepositoryPort.findAllStorageFileNames());

    Map<String, List<StoredFileInfo>> filesByOriginalName = new HashMap<>();

    for (StoredFileInfo file : filesInStorage) {

      String originalFileName = getOriginalFileName(file.storageFileName());

      filesByOriginalName
          .computeIfAbsent(originalFileName, key -> new java.util.ArrayList<>())
          .add(file);
    }

    int deletedFiles = 0;

    for (Map.Entry<String, List<StoredFileInfo>> entry : filesByOriginalName.entrySet()) {

      String originalFileName = entry.getKey();
      List<StoredFileInfo> relatedFiles = entry.getValue();

      boolean originalExists =
          relatedFiles.stream().anyMatch(file -> file.storageFileName().equals(originalFileName));

      boolean registeredInDatabase = filesInDatabase.contains(originalFileName);

      // DB attachment exists, but its original file is missing.
      // Remove the remaining thumbnail as well.
      if (registeredInDatabase && !originalExists) {

        Instant latestModified =
            relatedFiles.stream()
                .map(StoredFileInfo::lastModified)
                .max(Instant::compareTo)
                .orElse(Instant.EPOCH);

        if (latestModified.isBefore(threshold)) {

          if (deletePhysicalFile(originalFileName)) {
            deletedFiles++;
          }

        } else {
          log.debug("Thumbnail without original is too young, keeping it: {}", originalFileName);
        }

        continue;
      }

      if (registeredInDatabase) {
        continue;
      }

      // Use the newest modification time of the original and thumbnail.
      Instant latestModified =
          relatedFiles.stream()
              .map(StoredFileInfo::lastModified)
              .max(Instant::compareTo)
              .orElse(Instant.EPOCH);

      if (latestModified.isBefore(threshold)) {

        if (deletePhysicalFile(originalFileName)) {
          deletedFiles++;
        }

      } else {
        log.debug("Orphan file is too young, keeping it: {}", originalFileName);
      }
    }

    return deletedFiles;
  }

  private boolean deletePhysicalFile(String storageFileName) {

    try {

      fileStoragePort.delete(storageFileName);

      log.info("Deleted orphan file: {}", storageFileName);

      return true;

    } catch (Exception e) {

      log.error("Failed to delete orphan file: {}", storageFileName, e);

      return false;
    }
  }

  private int deleteOrphanAttachments(List<StoredFileInfo> filesInStorage) {

    Set<String> filesOnDisk =
        filesInStorage.stream().map(StoredFileInfo::storageFileName).collect(Collectors.toSet());

    List<FileAttachment> orphanAttachments = fileAttachmentRepositoryPort.findOrphanAttachments();

    int deletedAttachments = 0;

    for (FileAttachment attachment : orphanAttachments) {

      String storageFileName = attachment.getStorageFileName();

      if (!filesOnDisk.contains(storageFileName)) {

        try {

          fileAttachmentRepositoryPort.delete(attachment);
          deletedAttachments++;

          log.info("Deleted orphan DB attachment: {}", storageFileName);

        } catch (Exception e) {

          log.error("Failed to delete orphan DB attachment: {}", storageFileName, e);
        }

      } else {

        log.debug("Orphan DB attachment still has physical file: {}", storageFileName);
      }
    }

    return deletedAttachments;
  }
}

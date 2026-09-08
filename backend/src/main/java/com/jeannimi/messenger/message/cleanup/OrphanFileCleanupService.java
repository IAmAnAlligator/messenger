package com.jeannimi.messenger.message.cleanup;

import com.jeannimi.messenger.application.port.out.FileAttachmentRepositoryPort;
import com.jeannimi.messenger.domain.message.FileAttachment;
import com.jeannimi.messenger.message.storage.FileStorageService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class OrphanFileCleanupService {

  private final FileStorageService fileStorageService;

  private final FileAttachmentRepositoryPort fileAttachmentRepository;

  private final boolean enabled;

  private final Duration orphanAge;

  public OrphanFileCleanupService(
      FileStorageService fileStorageService,
      FileAttachmentRepositoryPort fileAttachmentRepository,
      @Value("${file-storage.cleanup.enabled}") boolean enabled,
      @Value("${file-storage.cleanup.orphan-age}") Duration orphanAge) {

    this.fileStorageService = fileStorageService;
    this.fileAttachmentRepository = fileAttachmentRepository;
    this.enabled = enabled;
    this.orphanAge = orphanAge;
  }

  @Scheduled(fixedDelayString = "${file-storage.cleanup.interval}")
  @Transactional
  public void cleanupOrphanFiles() {

    if (enabled) {

      log.info("Starting orphan file cleanup");

      Instant threshold = Instant.now().minus(orphanAge);

      /*
       * Получаем физические файлы из storage.
       */
      List<Path> filesInStorage = fileStorageService.listFiles();

      log.info("Storage contains {} files", filesInStorage.size());

      /*
       * Удаляем физические файлы, которых нет в БД
       * и которые старше заданного orphan-age.
       */
      int deletedFiles = deleteOrphanFiles(filesInStorage, threshold);

      /*
       * После удаления физических файлов получаем
       * актуальное состояние storage.
       */
      List<Path> currentFilesInStorage = fileStorageService.listFiles();
      /*
       * Удаляем DB-записи FileAttachment,
       * которые больше не используются Message
       * и для которых физического файла уже нет.
       */
      int deletedAttachments = deleteOrphanAttachments(currentFilesInStorage);

      log.info(
          "Orphan file cleanup finished. " + "Deleted files: {}, " + "deleted DB attachments: {}",
          deletedFiles,
          deletedAttachments);

    } else {

      log.debug("Orphan file cleanup is disabled");
    }
  }

  private int deleteOrphanFiles(List<Path> filesInStorage, Instant threshold) {

    /*
     * Все storageFileName, зарегистрированные
     * в таблице file_attachments.
     */
    Set<String> filesInDatabase = new HashSet<>(fileAttachmentRepository.findAllStorageFileNames());

    int deletedFiles = 0;

    for (Path file : filesInStorage) {

      String storageFileName = file.getFileName().toString();

      /*
       * Зарегистрированный в БД файл удалять нельзя.
       */
      if (filesInDatabase.contains(storageFileName)) {

        log.debug("File is registered in DB: {}", storageFileName);

      } else {

        /*
         * Файл отсутствует в БД.
         *
         * Удаляем его только после того,
         * как он достаточно долго находится
         * в storage.
         */
        if (isOlderThan(file, threshold)) {

          if (deletePhysicalFile(storageFileName)) {
            deletedFiles++;
          }

        } else {

          log.debug("Orphan file is too young, keeping it: {}", storageFileName);
        }
      }
    }

    return deletedFiles;
  }

  private boolean isOlderThan(Path file, Instant threshold) {

    try {

      Instant lastModified = Files.getLastModifiedTime(file).toInstant();

      return lastModified.isBefore(threshold);

    } catch (IOException e) {

      log.error("Failed to read last modified time: {}", file, e);

      return false;
    }
  }

  private boolean deletePhysicalFile(String storageFileName) {

    try {

      fileStorageService.delete(storageFileName);

      log.info("Deleted orphan file: {}", storageFileName);

      return true;

    } catch (Exception e) {

      log.error("Failed to delete orphan file: {}", storageFileName, e);

      return false;
    }
  }

  private int deleteOrphanAttachments(List<Path> filesInStorage) {

    /*
     * Создаём множество физических файлов,
     * существующих в storage.
     */
    Set<String> filesOnDisk = new HashSet<>();

    for (Path file : filesInStorage) {

      filesOnDisk.add(file.getFileName().toString());
    }

    /*
     * Получаем FileAttachment, которые больше
     * не используются ни одним Message.
     */
    List<FileAttachment> orphanAttachments = fileAttachmentRepository.findOrphanAttachments();

    int deletedAttachments = 0;

    for (FileAttachment attachment : orphanAttachments) {

      String storageFileName = attachment.getStorageFileName();

      /*
       * Если физического файла нет,
       * удаляем бесполезную запись из БД.
       */
      if (!filesOnDisk.contains(storageFileName)) {

        try {

          fileAttachmentRepository.delete(attachment);

          deletedAttachments++;

          log.info("Deleted orphan DB attachment: {}", storageFileName);

        } catch (Exception e) {

          log.error("Failed to delete orphan DB attachment: {}", storageFileName, e);
        }

      } else {

        /*
         * Attachment не используется Message,
         * но физический файл ещё существует.
         */
        log.debug("Orphan DB attachment still has physical file: {}", storageFileName);
      }
    }

    return deletedAttachments;
  }
}

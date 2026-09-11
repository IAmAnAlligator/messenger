package com.jeannimi.messenger.adapter.out.filestorage;

import com.jeannimi.messenger.application.port.out.FileStorageMaintenancePort;
import com.jeannimi.messenger.application.port.out.FileStoragePort;
import com.jeannimi.messenger.application.port.out.StoredFile;
import com.jeannimi.messenger.application.port.out.StoredFileInfo;
import com.jeannimi.messenger.common.exception_handling.FileStorageException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LocalFileStorageAdapter implements FileStoragePort, FileStorageMaintenancePort {

  private final Path rootLocation;

  public LocalFileStorageAdapter(
      @Value("${file.storage.location}") String storageLocation) {

    this.rootLocation = Path.of(storageLocation)
        .toAbsolutePath()
        .normalize();
  }

  @Override
  public StoredFile store(
      InputStream inputStream,
      String originalFileName) {

    try {
      Files.createDirectories(rootLocation);

      String extension = extractExtension(originalFileName);
      String storageFileName = UUID.randomUUID() + extension;

      Path target = rootLocation
          .resolve(storageFileName)
          .normalize();

      if (!target.startsWith(rootLocation)) {
        throw new FileStorageException("Invalid storage path");
      }

      Files.copy(
          inputStream,
          target,
          StandardCopyOption.REPLACE_EXISTING);

      return new StoredFile(
          storageFileName);

    } catch (IOException e) {
      throw new FileStorageException(
          "Failed to store file",
          e);
    }
  }

  @Override
  public InputStream load(String storageFileName) {

    try {
      Path file = rootLocation
          .resolve(storageFileName)
          .normalize();

      if (!file.startsWith(rootLocation)) {
        throw new FileStorageException("Invalid storage path");
      }

      return Files.newInputStream(file);

    } catch (IOException e) {
      throw new FileStorageException(
          "Failed to load file",
          e);
    }
  }

  @Override
  public void delete(String storageFileName) {

    Path path = rootLocation
        .resolve(storageFileName)
        .normalize();

    try {
      Files.deleteIfExists(path);

    } catch (IOException e) {
      throw new FileStorageException(
          "Failed to delete file: " + storageFileName,
          e);
    }
  }

  @Override
  public String detectContentType(String storageFileName) {

    try {

      Path file = rootLocation
          .resolve(storageFileName)
          .normalize();

      if (!file.startsWith(rootLocation)) {
        throw new FileStorageException("Invalid storage path");
      }

      String contentType =
          Files.probeContentType(file);

      return contentType == null || contentType.isBlank()
          ? "application/octet-stream"
          : contentType;

    } catch (IOException e) {

      throw new FileStorageException(
          "Failed to detect content type: " + storageFileName,
          e);
    }
  }

  private String extractExtension(String fileName) {

    if (fileName == null || fileName.isBlank()) {
      return "";
    }

    int index = fileName.lastIndexOf('.');

    if (index <= 0 || index == fileName.length() - 1) {
      return "";
    }

    return fileName.substring(index);
  }

  @Override
  public List<StoredFileInfo> listFiles() {

    try (Stream<Path> stream = Files.list(rootLocation)) {

      return stream
          .filter(Files::isRegularFile)
          .map(this::toStoredFileInfo)
          .toList();

    } catch (IOException e) {

      throw new FileStorageException(
          "Failed to list stored files", e);
    }
  }

  private StoredFileInfo toStoredFileInfo(Path file) {

    try {

      return new StoredFileInfo(
          file.getFileName().toString(),
          Files.getLastModifiedTime(file).toInstant());

    } catch (IOException e) {

      throw new FileStorageException(
          "Failed to read file metadata: " + file, e);
    }
  }
}
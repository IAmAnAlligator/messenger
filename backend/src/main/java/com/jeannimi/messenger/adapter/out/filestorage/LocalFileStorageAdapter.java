package com.jeannimi.messenger.adapter.out.filestorage;

import com.jeannimi.messenger.application.port.out.FileStorageMaintenancePort;
import com.jeannimi.messenger.application.port.out.FileStoragePort;
import com.jeannimi.messenger.application.port.out.StoredFile;
import com.jeannimi.messenger.application.port.out.StoredFileInfo;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LocalFileStorageAdapter implements FileStoragePort, FileStorageMaintenancePort {

  private final Path rootLocation;

  private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024;
  private static final int BUFFER_SIZE = 8192;

  public LocalFileStorageAdapter(@Value("${file.storage.location}") String storageLocation) {

    this.rootLocation = Path.of(storageLocation).toAbsolutePath().normalize();
  }

  private String getThumbnailFileName(
      String originalFileName,
      String extension
  ) {
    if (!".jpg".equals(extension) && !".png".equals(extension)) {
      throw new FileStorageException("Unsupported thumbnail extension");
    }

    return originalFileName + ".thumbnail" + extension;
  }

  @Override
  public void storeThumbnail(
      String originalStorageFileName,
      byte[] content,
      String extension
  ) {
    if (content == null || content.length == 0) {
      throw new FileStorageException("Thumbnail content is empty");
    }

    try {
      Files.createDirectories(rootLocation);

      Path thumbnailPath = resolveSafePath(
          getThumbnailFileName(originalStorageFileName, extension));

      Files.write(
          thumbnailPath,
          content,
          StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING);

    } catch (IOException e) {
      throw new FileStorageException("Failed to store thumbnail", e);
    }
  }

  @Override
  public byte[] loadThumbnail(
      String originalStorageFileName,
      String extension
  ) {
    Path thumbnailPath = resolveSafePath(
        getThumbnailFileName(originalStorageFileName, extension));

    try {
      return Files.readAllBytes(thumbnailPath);
    } catch (IOException e) {
      throw new FileStorageException("Failed to load thumbnail", e);
    }
  }

  @Override
  public StoredFile store(InputStream inputStream, String originalFileName) {
    try {
      Files.createDirectories(rootLocation);

      String extension = extractExtension(originalFileName);
      String storageFileName = UUID.randomUUID() + extension;

      Path target = resolveSafePath(storageFileName);

      try (OutputStream outputStream = Files.newOutputStream(target)) {
        byte[] buffer = new byte[BUFFER_SIZE];
        long totalBytes = 0;
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
          totalBytes += bytesRead;

          if (totalBytes > MAX_FILE_SIZE_BYTES) {
            throw new FileStorageException(
                "File size exceeds 10 MB");
          }

          outputStream.write(buffer, 0, bytesRead);
        }
      } catch (RuntimeException e) {
        Files.deleteIfExists(target);
        throw e;
      }

      return new StoredFile(storageFileName);

    } catch (IOException e) {
      throw new FileStorageException("Failed to store file", e);
    }
  }

  @Override
  public InputStream load(String storageFileName) {

    Path file = resolveSafePath(storageFileName);

    try {
      return Files.newInputStream(file);
    } catch (IOException e) {
      throw new FileStorageException("Failed to load file", e);
    }
  }

  @Override
  public void delete(String storageFileName) {

    deleteFile(getThumbnailFileName(storageFileName, ".jpg"));
    deleteFile(getThumbnailFileName(storageFileName, ".png"));

    deleteFile(storageFileName);
  }

  private void deleteFile(String fileName) {

    Path path = resolveSafePath(fileName);

    try {
      Files.deleteIfExists(path);
    } catch (IOException e) {
      throw new FileStorageException("Failed to delete file: " + fileName, e);
    }
  }

  @Override
  public String detectContentType(String storageFileName) {

    Path file = resolveSafePath(storageFileName);

    try {
      String contentType = Files.probeContentType(file);

      return contentType == null || contentType.isBlank()
          ? "application/octet-stream"
          : contentType;

    } catch (IOException e) {
      throw new FileStorageException("Failed to detect content type: " + storageFileName, e);
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

      return stream.filter(Files::isRegularFile).map(this::toStoredFileInfo).toList();

    } catch (IOException e) {

      throw new FileStorageException("Failed to list stored files", e);
    }
  }

  private StoredFileInfo toStoredFileInfo(Path file) {

    try {

      return new StoredFileInfo(
          file.getFileName().toString(), Files.getLastModifiedTime(file).toInstant());

    } catch (IOException e) {

      throw new FileStorageException("Failed to read file metadata: " + file, e);
    }
  }

  private Path resolveSafePath(String fileName) {

    Path path = rootLocation.resolve(fileName).normalize();

    if (!path.startsWith(rootLocation)) {
      throw new FileStorageException("Invalid storage path");
    }

    return path;
  }
}

package com.jeannimi.messenger.adapter.out.filestorage;

import com.jeannimi.messenger.application.port.out.FileEncryptionPort;
import com.jeannimi.messenger.application.port.out.FileStorageMaintenancePort;
import com.jeannimi.messenger.application.port.out.FileStoragePort;
import com.jeannimi.messenger.application.port.out.StoredFile;
import com.jeannimi.messenger.application.port.out.StoredFileInfo;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LocalFileStorageAdapter implements FileStoragePort, FileStorageMaintenancePort {

  private final Path rootLocation;
  private final FileEncryptionPort fileEncryptionPort;

  private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024;
  private static final int ENCRYPTION_OVERHEAD_BYTES = 30;
  private static final int BUFFER_SIZE = 8192;

  public LocalFileStorageAdapter(
      @Value("${file.storage.location}") String storageLocation,
      FileEncryptionPort fileEncryptionPort) {

    this.rootLocation = Path.of(storageLocation).toAbsolutePath().normalize();
    this.fileEncryptionPort = fileEncryptionPort;
  }

  private String getThumbnailFileName(String originalFileName, String extension) {
    if (!".jpg".equals(extension) && !".png".equals(extension)) {
      throw new FileStorageException("Unsupported thumbnail extension");
    }

    return originalFileName + ".thumbnail" + extension;
  }

  @Override
  public void storeThumbnail(String originalStorageFileName, byte[] content, String extension) {
    if (content == null || content.length == 0) {
      throw new FileStorageException("Thumbnail content is empty");
    }

    Path thumbnailPath = resolveSafePath(getThumbnailFileName(originalStorageFileName, extension));

    Path tempFile = null;

    try {
      Files.createDirectories(rootLocation);

      // 1. Encrypt thumbnail.
      byte[] encrypted = fileEncryptionPort.encrypt(content);

      // 2. Create a temporary file in the same directory.
      tempFile = Files.createTempFile(rootLocation, ".thumbnail-", ".tmp");

      // 3. Write the complete encrypted content.
      Files.write(
          tempFile, encrypted, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);

      // 4. Atomically publish the thumbnail.
      // Fails if the target already exists.
      Files.createLink(thumbnailPath, tempFile);

    } catch (java.nio.file.FileAlreadyExistsException e) {
      throw new FileStorageException("Thumbnail already exists", e);

    } catch (IOException | UnsupportedOperationException e) {
      throw new FileStorageException("Failed to store thumbnail", e);

    } finally {
      // 5. Remove the temporary file.
      if (tempFile != null) {
        try {
          Files.deleteIfExists(tempFile);
        } catch (IOException e) {
          log.warn("Failed to delete temporary thumbnail file: {}", tempFile, e);
        }
      }
    }
  }

  @Override
  public byte[] loadThumbnail(String originalStorageFileName, String extension) {
    Path thumbnailPath = resolveSafePath(getThumbnailFileName(originalStorageFileName, extension));

    try {
      // Читаем зашифрованные данные.
      byte[] encrypted = Files.readAllBytes(thumbnailPath);

      // Дешифруем изображение.
      return fileEncryptionPort.decrypt(encrypted);

    } catch (IOException e) {
      throw new FileStorageException("Failed to load thumbnail", e);
    }
  }

  @Override
  public StoredFile store(InputStream inputStream, String originalFileName) {

    String extension = extractExtension(originalFileName);
    String storageFileName = UUID.randomUUID() + extension;

    Path target = resolveSafePath(storageFileName);

    boolean fileCreated = false;

    try {

      Files.createDirectories(rootLocation);

      // 1. Читаем исходный файл.
      byte[] plaintext = readWithSizeLimit(inputStream);

      // 2. Шифруем содержимое.
      byte[] encrypted = fileEncryptionPort.encrypt(plaintext);

      // 3. Создаём файл только после шифрования.
      try (OutputStream outputStream =
          Files.newOutputStream(target, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {

        fileCreated = true;

        outputStream.write(encrypted);
      }

      return new StoredFile(storageFileName);

    } catch (IOException | RuntimeException e) {

      // Удаляем только файл, созданный этим вызовом.
      if (fileCreated) {
        try {
          Files.deleteIfExists(target);
        } catch (IOException cleanupException) {
          e.addSuppressed(cleanupException);
        }
      }

      if (e instanceof FileStorageException fileStorageException) {
        throw fileStorageException;
      }

      throw new FileStorageException("Failed to store file", e);
    }
  }

  private byte[] readWithSizeLimit(InputStream inputStream) throws IOException {

    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

      byte[] buffer = new byte[BUFFER_SIZE];
      long totalBytes = 0;
      int bytesRead;

      while ((bytesRead = inputStream.read(buffer)) != -1) {

        totalBytes += bytesRead;

        if (totalBytes > MAX_FILE_SIZE_BYTES) {
          throw new FileStorageException("File size exceeds 10 MB");
        }

        outputStream.write(buffer, 0, bytesRead);
      }

      return outputStream.toByteArray();
    }
  }

  @Override
  public InputStream load(String storageFileName) {

    Path file = resolveSafePath(storageFileName);

    try {

      long fileSize = Files.size(file);

      long maxEncryptedSize = MAX_FILE_SIZE_BYTES + ENCRYPTION_OVERHEAD_BYTES;

      if (fileSize > maxEncryptedSize) {
        throw new FileStorageException("Stored file exceeds maximum size");
      }

      byte[] encrypted = Files.readAllBytes(file);

      byte[] decrypted = fileEncryptionPort.decrypt(encrypted);

      return new ByteArrayInputStream(decrypted);

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

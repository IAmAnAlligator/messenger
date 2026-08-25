package com.jeannimi.messenger.message.storage;

import com.jeannimi.messenger.common.exception_handling.FileStorageException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LocalFileStorageService implements FileStorageService {

  private final Path rootLocation;

  public LocalFileStorageService(@Value("${file.storage.location}") String storageLocation) {

    this.rootLocation = Path.of(storageLocation).toAbsolutePath().normalize();
  }

  @Override
  public List<Path> listFiles() {

    try (Stream<Path> stream = Files.list(rootLocation)) {

      return stream.filter(Files::isRegularFile).toList();

    } catch (IOException e) {

      throw new FileStorageException("Failed to list stored files", e);
    }
  }

  @Override
  public StoredFile store(InputStream inputStream, String originalFileName) {

    try {

      Files.createDirectories(rootLocation);

      String extension = extractExtension(originalFileName);

      String storageFileName = UUID.randomUUID() + extension;

      Path target = rootLocation.resolve(storageFileName).normalize();

      if (!target.startsWith(rootLocation)) {
        throw new FileStorageException("Invalid storage path");
      }

      Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);

      return new StoredFile(storageFileName, target.toString());

    } catch (IOException e) {

      throw new FileStorageException("Failed to store file", e);
    }
  }

  @Override
  public InputStream load(String storageFileName) {

    try {

      Path file = rootLocation.resolve(storageFileName).normalize();

      if (!file.startsWith(rootLocation)) {
        throw new FileStorageException("Invalid storage path");
      }

      return Files.newInputStream(file);

    } catch (IOException e) {

      throw new FileStorageException("Failed to load file", e);
    }
  }

  @Override
  public void delete(String storageFileName) throws FileStorageException {

    Path path = rootLocation.resolve(storageFileName).normalize();

    try {

      Files.deleteIfExists(path);

    } catch (IOException e) {

      throw new FileStorageException("Failed to delete file: " + storageFileName, e);
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
}

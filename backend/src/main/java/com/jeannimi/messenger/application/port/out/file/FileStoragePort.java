package com.jeannimi.messenger.application.port.out.file;

import java.io.InputStream;

public interface FileStoragePort {

  StoredFile store(InputStream inputStream, String originalFileName);

  InputStream load(String storageFileName);

  void delete(String storageFileName);

  String detectContentType(String storageFileName);

  void storeThumbnail(String originalStorageFileName, byte[] content, String extension);

  byte[] loadThumbnail(String originalStorageFileName, String extension);
}

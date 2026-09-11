package com.jeannimi.messenger.application.port.out;

import java.io.InputStream;

public interface FileStoragePort {

  StoredFile store(InputStream inputStream, String originalFileName);

  InputStream load(String storageFileName);

  void delete(String storageFileName);

  String detectContentType(String storageFileName);

}
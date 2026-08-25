package com.jeannimi.messenger.message.storage;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

public interface FileStorageService {

  StoredFile store(InputStream inputStream, String originalFileName);

  InputStream load(String storageFileName);

  void delete(String storageFileName);

  List<Path> listFiles();
}

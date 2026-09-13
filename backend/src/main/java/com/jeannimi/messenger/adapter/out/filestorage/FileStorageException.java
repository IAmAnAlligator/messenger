package com.jeannimi.messenger.adapter.out.filestorage;

public class FileStorageException extends RuntimeException {

  public FileStorageException(String message) {
    super(message);
  }

  public FileStorageException(String message, Throwable cause) {
    super(message, cause);
  }
}

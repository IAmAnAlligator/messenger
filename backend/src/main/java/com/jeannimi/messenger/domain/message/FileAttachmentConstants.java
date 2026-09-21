package com.jeannimi.messenger.domain.message;

public class FileAttachmentConstants {

  private FileAttachmentConstants() {}

  public static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024;

  public static final int MAX_FILE_NAME_LENGTH = 255;
  public static final int MAX_STORAGE_FILE_NAME_LENGTH = 255;
  public static final int MAX_CONTENT_TYPE_LENGTH = 100;

}

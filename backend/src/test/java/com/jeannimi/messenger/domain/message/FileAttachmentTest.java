package com.jeannimi.messenger.domain.message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jeannimi.messenger.domain.exception.MessageError;
import com.jeannimi.messenger.domain.exception.MessageException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class FileAttachmentTest {

  private static final UUID FILE_ATTACHMENT_UUID =
      UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789012");

  private static final FileAttachmentId FILE_ATTACHMENT_ID =
      new FileAttachmentId(FILE_ATTACHMENT_UUID);

  private static final String ORIGINAL_FILE_NAME = "document.pdf";
  private static final String STORAGE_FILE_NAME = "generated-file-123";
  private static final String CONTENT_TYPE = "application/pdf";

  private static final Long FILE_SIZE = 1024L;

  private static final Instant CREATED_AT = Instant.parse("2026-10-15T10:00:00Z");

  @Test
  void create_shouldCreateFileAttachment_whenArgumentsAreValid() {
    FileAttachment fileAttachment =
        FileAttachment.create(
            FILE_ATTACHMENT_ID, ORIGINAL_FILE_NAME, STORAGE_FILE_NAME, CONTENT_TYPE, FILE_SIZE);

    assertNotNull(fileAttachment);
    assertEquals(FILE_ATTACHMENT_ID, fileAttachment.getId());
    assertEquals(ORIGINAL_FILE_NAME, fileAttachment.getOriginalFileName());
    assertEquals(STORAGE_FILE_NAME, fileAttachment.getStorageFileName());
    assertEquals(CONTENT_TYPE, fileAttachment.getContentType());
    assertEquals(FILE_SIZE, fileAttachment.getSize());
    assertNotNull(fileAttachment.getCreatedAt());
  }

  @Test
  void create_shouldSetCreatedAtNearCurrentTime() {
    Instant beforeCreation = Instant.now();

    FileAttachment fileAttachment =
        FileAttachment.create(
            FILE_ATTACHMENT_ID, ORIGINAL_FILE_NAME, STORAGE_FILE_NAME, CONTENT_TYPE, FILE_SIZE);

    Instant afterCreation = Instant.now();

    assertTrue(
        !fileAttachment.getCreatedAt().isBefore(beforeCreation)
            && !fileAttachment.getCreatedAt().isAfter(afterCreation));
  }

  @Test
  void create_shouldThrowMessageException_whenOriginalFileNameIsNull() {
    MessageException exception =
        assertThrows(
            MessageException.class,
            () ->
                FileAttachment.create(
                    FILE_ATTACHMENT_ID, null, STORAGE_FILE_NAME, CONTENT_TYPE, FILE_SIZE));

    assertEquals(MessageError.FILE_NAME_INVALID, exception.getError());
  }

  @Test
  void create_shouldThrowMessageException_whenOriginalFileNameIsBlank() {
    MessageException exception =
        assertThrows(
            MessageException.class,
            () ->
                FileAttachment.create(
                    FILE_ATTACHMENT_ID, "   ", STORAGE_FILE_NAME, CONTENT_TYPE, FILE_SIZE));

    assertEquals(MessageError.FILE_NAME_INVALID, exception.getError());
  }

  @Test
  void create_shouldThrowMessageException_whenOriginalFileNameIsTooLong() {
    String fileName = "a".repeat(FileAttachmentConstants.MAX_FILE_NAME_LENGTH + 1);

    MessageException exception =
        assertThrows(
            MessageException.class,
            () ->
                FileAttachment.create(
                    FILE_ATTACHMENT_ID, fileName, STORAGE_FILE_NAME, CONTENT_TYPE, FILE_SIZE));

    assertEquals(MessageError.FILE_NAME_TOO_LONG, exception.getError());
  }

  @Test
  void create_shouldAcceptOriginalFileNameAtMaximumLength() {
    String fileName = "a".repeat(FileAttachmentConstants.MAX_FILE_NAME_LENGTH);

    FileAttachment fileAttachment =
        FileAttachment.create(
            FILE_ATTACHMENT_ID, fileName, STORAGE_FILE_NAME, CONTENT_TYPE, FILE_SIZE);

    assertEquals(fileName, fileAttachment.getOriginalFileName());
  }

  @Test
  void create_shouldThrowMessageException_whenStorageFileNameIsNull() {
    MessageException exception =
        assertThrows(
            MessageException.class,
            () ->
                FileAttachment.create(
                    FILE_ATTACHMENT_ID, ORIGINAL_FILE_NAME, null, CONTENT_TYPE, FILE_SIZE));

    assertEquals(MessageError.FILE_STORAGE_NAME_INVALID, exception.getError());
  }

  @Test
  void create_shouldThrowMessageException_whenStorageFileNameIsBlank() {
    MessageException exception =
        assertThrows(
            MessageException.class,
            () ->
                FileAttachment.create(
                    FILE_ATTACHMENT_ID, ORIGINAL_FILE_NAME, " ", CONTENT_TYPE, FILE_SIZE));

    assertEquals(MessageError.FILE_STORAGE_NAME_INVALID, exception.getError());
  }

  @Test
  void create_shouldThrowMessageException_whenStorageFileNameIsTooLong() {
    String storageFileName = "a".repeat(FileAttachmentConstants.MAX_STORAGE_FILE_NAME_LENGTH + 1);

    MessageException exception =
        assertThrows(
            MessageException.class,
            () ->
                FileAttachment.create(
                    FILE_ATTACHMENT_ID,
                    ORIGINAL_FILE_NAME,
                    storageFileName,
                    CONTENT_TYPE,
                    FILE_SIZE));

    assertEquals(MessageError.FILE_STORAGE_NAME_TOO_LONG, exception.getError());
  }

  @Test
  void create_shouldAcceptStorageFileNameAtMaximumLength() {
    String storageFileName = "a".repeat(FileAttachmentConstants.MAX_STORAGE_FILE_NAME_LENGTH);

    FileAttachment fileAttachment =
        FileAttachment.create(
            FILE_ATTACHMENT_ID, ORIGINAL_FILE_NAME, storageFileName, CONTENT_TYPE, FILE_SIZE);

    assertEquals(storageFileName, fileAttachment.getStorageFileName());
  }

  @Test
  void create_shouldThrowMessageException_whenContentTypeIsNull() {
    MessageException exception =
        assertThrows(
            MessageException.class,
            () ->
                FileAttachment.create(
                    FILE_ATTACHMENT_ID, ORIGINAL_FILE_NAME, STORAGE_FILE_NAME, null, FILE_SIZE));

    assertEquals(MessageError.FILE_CONTENT_TYPE_INVALID, exception.getError());
  }

  @Test
  void create_shouldThrowMessageException_whenContentTypeIsBlank() {
    MessageException exception =
        assertThrows(
            MessageException.class,
            () ->
                FileAttachment.create(
                    FILE_ATTACHMENT_ID, ORIGINAL_FILE_NAME, STORAGE_FILE_NAME, " ", FILE_SIZE));

    assertEquals(MessageError.FILE_CONTENT_TYPE_INVALID, exception.getError());
  }

  @Test
  void create_shouldThrowMessageException_whenContentTypeIsTooLong() {
    String contentType = "a".repeat(FileAttachmentConstants.MAX_CONTENT_TYPE_LENGTH + 1);

    MessageException exception =
        assertThrows(
            MessageException.class,
            () ->
                FileAttachment.create(
                    FILE_ATTACHMENT_ID,
                    ORIGINAL_FILE_NAME,
                    STORAGE_FILE_NAME,
                    contentType,
                    FILE_SIZE));

    assertEquals(MessageError.FILE_CONTENT_TYPE_TOO_LONG, exception.getError());
  }

  @Test
  void create_shouldAcceptContentTypeAtMaximumLength() {
    String contentType = "a".repeat(FileAttachmentConstants.MAX_CONTENT_TYPE_LENGTH);

    FileAttachment fileAttachment =
        FileAttachment.create(
            FILE_ATTACHMENT_ID, ORIGINAL_FILE_NAME, STORAGE_FILE_NAME, contentType, FILE_SIZE);

    assertEquals(contentType, fileAttachment.getContentType());
  }

  @Test
  void create_shouldThrowMessageException_whenSizeIsNull() {
    MessageException exception =
        assertThrows(
            MessageException.class,
            () ->
                FileAttachment.create(
                    FILE_ATTACHMENT_ID, ORIGINAL_FILE_NAME, STORAGE_FILE_NAME, CONTENT_TYPE, null));

    assertEquals(MessageError.FILE_EMPTY, exception.getError());
  }

  @Test
  void create_shouldThrowMessageException_whenSizeIsZero() {
    MessageException exception =
        assertThrows(
            MessageException.class,
            () ->
                FileAttachment.create(
                    FILE_ATTACHMENT_ID, ORIGINAL_FILE_NAME, STORAGE_FILE_NAME, CONTENT_TYPE, 0L));

    assertEquals(MessageError.FILE_EMPTY, exception.getError());
  }

  @Test
  void create_shouldThrowMessageException_whenSizeIsNegative() {
    MessageException exception =
        assertThrows(
            MessageException.class,
            () ->
                FileAttachment.create(
                    FILE_ATTACHMENT_ID, ORIGINAL_FILE_NAME, STORAGE_FILE_NAME, CONTENT_TYPE, -1L));

    assertEquals(MessageError.FILE_EMPTY, exception.getError());
  }

  @Test
  void create_shouldThrowMessageException_whenSizeExceedsMaximum() {
    MessageException exception =
        assertThrows(
            MessageException.class,
            () ->
                FileAttachment.create(
                    FILE_ATTACHMENT_ID,
                    ORIGINAL_FILE_NAME,
                    STORAGE_FILE_NAME,
                    CONTENT_TYPE,
                    FileAttachmentConstants.MAX_FILE_SIZE_BYTES + 1));

    assertEquals(MessageError.FILE_TOO_LARGE, exception.getError());
  }

  @Test
  void create_shouldAcceptFileAtMaximumSize() {
    Long maxSize = FileAttachmentConstants.MAX_FILE_SIZE_BYTES;

    FileAttachment fileAttachment =
        FileAttachment.create(
            FILE_ATTACHMENT_ID, ORIGINAL_FILE_NAME, STORAGE_FILE_NAME, CONTENT_TYPE, maxSize);

    assertEquals(maxSize, fileAttachment.getSize());
  }

  @Test
  void reconstitute_shouldRestoreFileAttachment_whenArgumentsAreValid() {
    FileAttachment fileAttachment =
        FileAttachment.reconstitute(
            FILE_ATTACHMENT_ID,
            ORIGINAL_FILE_NAME,
            STORAGE_FILE_NAME,
            CONTENT_TYPE,
            FILE_SIZE,
            CREATED_AT);

    assertEquals(FILE_ATTACHMENT_ID, fileAttachment.getId());
    assertEquals(ORIGINAL_FILE_NAME, fileAttachment.getOriginalFileName());
    assertEquals(STORAGE_FILE_NAME, fileAttachment.getStorageFileName());
    assertEquals(CONTENT_TYPE, fileAttachment.getContentType());
    assertEquals(FILE_SIZE, fileAttachment.getSize());
    assertEquals(CREATED_AT, fileAttachment.getCreatedAt());
  }

  @Test
  void reconstitute_shouldThrowNullPointerException_whenIdIsNull() {
    assertThrows(
        NullPointerException.class,
        () ->
            FileAttachment.reconstitute(
                null, ORIGINAL_FILE_NAME, STORAGE_FILE_NAME, CONTENT_TYPE, FILE_SIZE, CREATED_AT));
  }

  @Test
  void reconstitute_shouldThrowNullPointerException_whenCreatedAtIsNull() {
    assertThrows(
        NullPointerException.class,
        () ->
            FileAttachment.reconstitute(
                FILE_ATTACHMENT_ID,
                ORIGINAL_FILE_NAME,
                STORAGE_FILE_NAME,
                CONTENT_TYPE,
                FILE_SIZE,
                null));
  }

  @Test
  void equals_shouldReturnTrue_whenObjectsAreTheSame() {
    FileAttachment fileAttachment =
        FileAttachment.reconstitute(
            FILE_ATTACHMENT_ID,
            ORIGINAL_FILE_NAME,
            STORAGE_FILE_NAME,
            CONTENT_TYPE,
            FILE_SIZE,
            CREATED_AT);

    assertEquals(fileAttachment, fileAttachment);
  }

  @Test
  void equals_shouldReturnTrue_whenIdsAreEqual() {
    FileAttachment first =
        FileAttachment.reconstitute(
            FILE_ATTACHMENT_ID,
            ORIGINAL_FILE_NAME,
            STORAGE_FILE_NAME,
            CONTENT_TYPE,
            FILE_SIZE,
            CREATED_AT);

    FileAttachment second =
        FileAttachment.reconstitute(
            FILE_ATTACHMENT_ID,
            "another-name.pdf",
            "another-storage-name",
            "application/octet-stream",
            2048L,
            Instant.parse("2026-10-16T10:00:00Z"));

    assertEquals(first, second);
  }

  @Test
  void equals_shouldReturnFalse_whenIdsAreDifferent() {
    FileAttachment first =
        FileAttachment.reconstitute(
            FILE_ATTACHMENT_ID,
            ORIGINAL_FILE_NAME,
            STORAGE_FILE_NAME,
            CONTENT_TYPE,
            FILE_SIZE,
            CREATED_AT);

    FileAttachment second =
        FileAttachment.reconstitute(
            new FileAttachmentId(UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789013")),
            ORIGINAL_FILE_NAME,
            STORAGE_FILE_NAME,
            CONTENT_TYPE,
            FILE_SIZE,
            CREATED_AT);

    assertNotEquals(first, second);
  }

  @Test
  void equals_shouldReturnFalse_whenObjectIsNull() {
    FileAttachment fileAttachment =
        FileAttachment.reconstitute(
            FILE_ATTACHMENT_ID,
            ORIGINAL_FILE_NAME,
            STORAGE_FILE_NAME,
            CONTENT_TYPE,
            FILE_SIZE,
            CREATED_AT);

    assertNotEquals(null, fileAttachment);
  }

  @Test
  void equals_shouldReturnFalse_whenObjectHasDifferentType() {
    FileAttachment fileAttachment =
        FileAttachment.reconstitute(
            FILE_ATTACHMENT_ID,
            ORIGINAL_FILE_NAME,
            STORAGE_FILE_NAME,
            CONTENT_TYPE,
            FILE_SIZE,
            CREATED_AT);

    assertThat(fileAttachment.equals("fileAttachment")).isFalse();
  }

  @Test
  void hashCode_shouldBeEqual_whenIdsAreEqual() {
    FileAttachment first =
        FileAttachment.reconstitute(
            FILE_ATTACHMENT_ID,
            ORIGINAL_FILE_NAME,
            STORAGE_FILE_NAME,
            CONTENT_TYPE,
            FILE_SIZE,
            CREATED_AT);

    FileAttachment second =
        FileAttachment.reconstitute(
            FILE_ATTACHMENT_ID,
            ORIGINAL_FILE_NAME,
            STORAGE_FILE_NAME,
            CONTENT_TYPE,
            FILE_SIZE,
            CREATED_AT);

    assertEquals(first.hashCode(), second.hashCode());
  }

  @Test
  void hashCode_shouldBeConsistent_whenCalledMultipleTimes() {
    FileAttachment fileAttachment =
        FileAttachment.reconstitute(
            FILE_ATTACHMENT_ID,
            ORIGINAL_FILE_NAME,
            STORAGE_FILE_NAME,
            CONTENT_TYPE,
            FILE_SIZE,
            CREATED_AT);

    assertEquals(fileAttachment.hashCode(), fileAttachment.hashCode());
  }
}

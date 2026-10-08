package com.jeannimi.messenger.adapter.out.persistence.projection;

import java.time.Instant;
import java.util.UUID;

public interface LastMessageProjection {

  UUID getChatId();

  UUID getMessageId();

  UUID getSenderId();

  String getSenderUsername();

  String getSenderHandle();

  String getSenderRole();

  String getEncryptedContent();

  Instant getCreatedAt();
}
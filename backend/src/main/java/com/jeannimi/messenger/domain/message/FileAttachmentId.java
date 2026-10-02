package com.jeannimi.messenger.domain.message;

import com.jeannimi.messenger.domain.common.DomainId;
import java.util.Objects;
import java.util.UUID;

public record FileAttachmentId(UUID value) implements DomainId {

  public FileAttachmentId {
    Objects.requireNonNull(value, "FileAttachment id must not be null");
  }
}

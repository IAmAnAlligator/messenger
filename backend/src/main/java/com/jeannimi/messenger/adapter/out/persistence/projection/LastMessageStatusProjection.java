package com.jeannimi.messenger.adapter.out.persistence.projection;

import java.util.UUID;

public interface LastMessageStatusProjection {

  UUID getChatId();

  String getStatus();
}
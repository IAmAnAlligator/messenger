package com.jeannimi.messenger.adapter.out.persistence.projection;

import java.util.UUID;

public interface UnreadCountProjection {

  UUID getChatId();

  long getUnreadCount();
}
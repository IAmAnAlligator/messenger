package com.jeannimi.messenger.application.presence.service;

import com.jeannimi.messenger.domain.chat.ChatId;
import com.jeannimi.messenger.domain.user.UserId;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Service;

@Service
public class UserPresenceService {

  private final ConcurrentMap<String, SessionPresence> sessions =
      new ConcurrentHashMap<>();

  public synchronized boolean registerSession(
      String sessionId,
      UserId userId) {

    Objects.requireNonNull(sessionId, "sessionId");
    Objects.requireNonNull(userId, "userId");

    boolean wasOnline = isOnline(userId);

    sessions.put(
        sessionId,
        new SessionPresence(userId, null, false));

    return !wasOnline;
  }

  public synchronized void updatePresence(
      String sessionId,
      UserId userId,
      ChatId chatId,
      boolean visible) {

    Objects.requireNonNull(sessionId, "sessionId");
    Objects.requireNonNull(userId, "userId");

    SessionPresence current = sessions.get(sessionId);

    if (current == null) {
      throw new IllegalStateException(
          "WebSocket session is not registered");
    }

    if (!current.userId().equals(userId)) {
      throw new IllegalStateException(
          "Session user does not match authenticated user");
    }

    sessions.put(
        sessionId,
        new SessionPresence(
            userId,
            visible
                ? Objects.requireNonNull(
                chatId,
                "Chat ID is required when visible")
                : null,
            visible));
  }

  public synchronized Optional<UserId> removeSession(
      String sessionId) {

    if (sessionId == null) {
      return Optional.empty();
    }

    SessionPresence removed = sessions.remove(sessionId);

    if (removed == null) {
      return Optional.empty();
    }

    UserId userId = removed.userId();

    if (!isOnline(userId)) {
      return Optional.of(userId);
    }

    return Optional.empty();
  }

  public synchronized boolean isOnline(UserId userId) {
    Objects.requireNonNull(userId, "userId");

    return sessions.values().stream()
        .anyMatch(session ->
            session.userId().equals(userId));
  }

  public synchronized boolean isViewingChat(
      UserId userId,
      ChatId chatId) {

    Objects.requireNonNull(userId, "userId");
    Objects.requireNonNull(chatId, "chatId");

    return sessions.values().stream()
        .anyMatch(session ->
            session.userId().equals(userId)
                && session.visible()
                && chatId.equals(session.chatId()));
  }

  private record SessionPresence(
      UserId userId,
      ChatId chatId,
      boolean visible) {
  }
}
package com.jeannimi.messenger.domain.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.jeannimi.messenger.domain.message.MessageId;
import com.jeannimi.messenger.domain.user.Email;
import com.jeannimi.messenger.domain.user.Handle;
import com.jeannimi.messenger.domain.user.PasswordHash;
import com.jeannimi.messenger.domain.user.Role;
import com.jeannimi.messenger.domain.user.User;
import com.jeannimi.messenger.domain.user.UserId;
import com.jeannimi.messenger.domain.user.Username;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ChatMemberTest {

  private static final UUID CHAT_MEMBER_ID_VALUE =
      UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789012");
  private static final UUID USER_ID_VALUE = UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789013");
  private static final UUID MESSAGE_ID_VALUE =
      UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789014");

  private ChatMemberId chatMemberId;
  private User user;
  private MessageId messageId;

  @BeforeEach
  void setUp() {
    chatMemberId = new ChatMemberId(CHAT_MEMBER_ID_VALUE);
    user =
        User.create(
            new UserId(USER_ID_VALUE),
            new Handle("abc1234"),
            new Username("Alex"),
            new Email("alex@mail.com"),
            new PasswordHash("passwordHash"),
            Role.USER);
    messageId = new MessageId(MESSAGE_ID_VALUE);
  }

  @Test
  void create_shouldInitializeMemberWithCurrentTimeAndNullLastReadMessageId() {
    Instant before = Instant.now();

    ChatMember member = ChatMember.create(chatMemberId, user, ChatRole.MEMBER);

    Instant after = Instant.now();

    assertEquals(chatMemberId, member.getId());
    assertEquals(user, member.getUser());
    assertEquals(ChatRole.MEMBER, member.getRole());
    assertEquals(user.getId(), member.getUserId());
    assertNull(member.getLastReadMessageId());
    assertFalse(member.isAdmin());
    assertFalse(member.getJoinedAt().isBefore(before));
    assertFalse(member.getJoinedAt().isAfter(after));
  }

  @Test
  void create_shouldCreateAdminMember() {
    ChatMember member = ChatMember.create(chatMemberId, user, ChatRole.ADMIN);

    assertEquals(ChatRole.ADMIN, member.getRole());
    assertTrue(member.isAdmin());
  }

  @Test
  void reconstitute_shouldRestoreMemberWithAllFields() {
    Instant joinedAt = Instant.parse("2026-01-01T10:00:00Z");

    ChatMember member =
        ChatMember.reconstitute(chatMemberId, user, ChatRole.ADMIN, joinedAt, messageId);

    assertEquals(chatMemberId, member.getId());
    assertEquals(user, member.getUser());
    assertEquals(ChatRole.ADMIN, member.getRole());
    assertEquals(joinedAt, member.getJoinedAt());
    assertEquals(messageId, member.getLastReadMessageId());
    assertEquals(user.getId(), member.getUserId());
    assertTrue(member.isAdmin());
  }

  @Test
  void reconstitute_shouldAllowNullLastReadMessageId() {
    Instant joinedAt = Instant.parse("2026-01-01T10:00:00Z");

    ChatMember member =
        ChatMember.reconstitute(chatMemberId, user, ChatRole.MEMBER, joinedAt, null);

    assertNull(member.getLastReadMessageId());
  }

  @Test
  void create_shouldThrowNullPointerException_whenIdIsNull() {
    assertThrows(NullPointerException.class, () -> ChatMember.create(null, user, ChatRole.MEMBER));
  }

  @Test
  void create_shouldThrowNullPointerException_whenUserIsNull() {
    assertThrows(
        NullPointerException.class, () -> ChatMember.create(chatMemberId, null, ChatRole.MEMBER));
  }

  @Test
  void create_shouldThrowNullPointerException_whenRoleIsNull() {
    assertThrows(NullPointerException.class, () -> ChatMember.create(chatMemberId, user, null));
  }

  @Test
  void reconstitute_shouldThrowNullPointerException_whenIdIsNull() {
    Instant joinedAt = Instant.parse("2026-01-01T10:00:00Z");

    assertThrows(
        NullPointerException.class,
        () -> ChatMember.reconstitute(null, user, ChatRole.MEMBER, joinedAt, null));
  }

  @Test
  void reconstitute_shouldThrowNullPointerException_whenUserIsNull() {
    Instant joinedAt = Instant.parse("2026-01-01T10:00:00Z");

    assertThrows(
        NullPointerException.class,
        () -> ChatMember.reconstitute(chatMemberId, null, ChatRole.MEMBER, joinedAt, null));
  }

  @Test
  void reconstitute_shouldThrowNullPointerException_whenRoleIsNull() {
    Instant joinedAt = Instant.parse("2026-01-01T10:00:00Z");

    assertThrows(
        NullPointerException.class,
        () -> ChatMember.reconstitute(chatMemberId, user, null, joinedAt, null));
  }

  @Test
  void reconstitute_shouldThrowNullPointerException_whenJoinedAtIsNull() {
    assertThrows(
        NullPointerException.class,
        () -> ChatMember.reconstitute(chatMemberId, user, ChatRole.MEMBER, null, null));
  }

  @Test
  void getUserId_shouldReturnUserId() {
    ChatMember member = ChatMember.create(chatMemberId, user, ChatRole.MEMBER);

    assertEquals(new UserId(USER_ID_VALUE), member.getUserId());
  }

  @Test
  void markAsRead_shouldUpdateLastReadMessageId() {
    ChatMember member = ChatMember.create(chatMemberId, user, ChatRole.MEMBER);

    member.markAsRead(messageId);

    assertEquals(messageId, member.getLastReadMessageId());
  }

  @Test
  void markAsRead_shouldReplacePreviousLastReadMessageId() {
    MessageId previousMessageId =
        new MessageId(UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789015"));
    ChatMember member =
        ChatMember.reconstitute(
            chatMemberId,
            user,
            ChatRole.MEMBER,
            Instant.parse("2026-01-01T10:00:00Z"),
            previousMessageId);

    member.markAsRead(messageId);

    assertEquals(messageId, member.getLastReadMessageId());
  }

  @Test
  void markAsRead_shouldThrowNullPointerException_whenMessageIdIsNull() {
    ChatMember member = ChatMember.create(chatMemberId, user, ChatRole.MEMBER);

    assertThrows(NullPointerException.class, () -> member.markAsRead(null));
    assertNull(member.getLastReadMessageId());
  }

  @Test
  void equals_shouldReturnTrue_whenComparingSameInstance() {
    ChatMember member = ChatMember.create(chatMemberId, user, ChatRole.MEMBER);

    assertEquals(member, member);
  }

  @Test
  void equals_shouldReturnTrue_whenIdsAreEqual() {
    Instant joinedAt = Instant.parse("2026-01-01T10:00:00Z");

    ChatMember first = ChatMember.reconstitute(chatMemberId, user, ChatRole.MEMBER, joinedAt, null);
    ChatMember second =
        ChatMember.reconstitute(chatMemberId, user, ChatRole.ADMIN, joinedAt, messageId);

    assertEquals(first, second);
  }

  @Test
  void equals_shouldReturnFalse_whenIdsAreDifferent() {
    ChatMember first = ChatMember.create(chatMemberId, user, ChatRole.MEMBER);
    ChatMember second =
        ChatMember.create(
            new ChatMemberId(UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789016")),
            user,
            ChatRole.MEMBER);

    assertNotEquals(first, second);
  }

  @Test
  void equals_shouldReturnFalse_whenComparingNull() {
    ChatMember member = ChatMember.create(chatMemberId, user, ChatRole.MEMBER);

    assertNotEquals(null, member);
  }

  @Test
  void equals_shouldReturnFalse_whenComparingDifferentType() {
    ChatMember member = ChatMember.create(chatMemberId, user, ChatRole.MEMBER);

    assertThat(member.equals("ChatMember")).isFalse();
  }

  @Test
  void hashCode_shouldBeEqual_whenIdsAreEqual() {
    Instant joinedAt = Instant.parse("2026-01-01T10:00:00Z");

    ChatMember first = ChatMember.reconstitute(chatMemberId, user, ChatRole.MEMBER, joinedAt, null);
    ChatMember second =
        ChatMember.reconstitute(chatMemberId, user, ChatRole.ADMIN, joinedAt, messageId);

    assertEquals(first.hashCode(), second.hashCode());
  }

  @Test
  void hashCode_shouldDependOnId() {
    ChatMember member = ChatMember.create(chatMemberId, user, ChatRole.MEMBER);

    assertEquals(chatMemberId.hashCode(), member.hashCode());
  }
}

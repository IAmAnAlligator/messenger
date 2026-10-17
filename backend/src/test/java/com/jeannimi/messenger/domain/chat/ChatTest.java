package com.jeannimi.messenger.domain.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jeannimi.messenger.domain.exception.ChatError;
import com.jeannimi.messenger.domain.exception.ChatException;
import com.jeannimi.messenger.domain.user.Email;
import com.jeannimi.messenger.domain.user.Handle;
import com.jeannimi.messenger.domain.user.PasswordHash;
import com.jeannimi.messenger.domain.user.Role;
import com.jeannimi.messenger.domain.user.User;
import com.jeannimi.messenger.domain.user.UserId;
import com.jeannimi.messenger.domain.user.Username;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ChatTest {

  private static final UUID CHAT_UUID = UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789001");

  private static final UUID ANOTHER_CHAT_UUID =
      UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789002");

  private static final UUID USER_UUID = UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789011");

  private static final UUID ANOTHER_USER_UUID =
      UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789012");

  private static final UUID MEMBER_UUID = UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789021");

  private static final UUID ANOTHER_MEMBER_UUID =
      UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789022");

  private static final Instant CREATED_AT = Instant.parse("2026-10-15T10:00:00Z");

  private static final Instant MESSAGE_CREATED_AT = Instant.parse("2026-10-15T11:00:00Z");

  private static final String CHAT_NAME = "Test chat";

  private ChatId chatId;
  private User user;
  private User anotherUser;
  private ChatMember admin;
  private ChatMember member;

  @BeforeEach
  void setUp() {
    chatId = new ChatId(CHAT_UUID);

    user = createUser(USER_UUID, "abc1234", "Alex", "alex@mail.com");

    anotherUser = createUser(ANOTHER_USER_UUID, "def5678", "Maria", "maria@mail.com");

    admin = createMember(MEMBER_UUID, user, ChatRole.ADMIN);

    member = createMember(ANOTHER_MEMBER_UUID, anotherUser, ChatRole.MEMBER);
  }

  // createGroup

  @Test
  void createGroup_shouldCreateChat_whenInputIsValid() {
    Chat chat = Chat.createGroup(chatId, CHAT_NAME, List.of(admin, member));

    assertEquals(chatId, chat.getId());
    assertEquals(CHAT_NAME, chat.getName());
    assertEquals(ChatType.GROUP, chat.getType());
    assertEquals(2, chat.getMembers().size());
    assertNull(chat.getLastMessageAt());
    assertNull(chat.getPrivateKey());
    assertNotNull(chat.getCreatedAt());
  }

  @Test
  void createGroup_shouldTrimName_whenNameContainsWhitespace() {
    Chat chat = Chat.createGroup(chatId, "  Test chat  ", List.of(admin, member));

    assertEquals("Test chat", chat.getName());
  }

  @Test
  void createGroup_shouldThrowNullPointerException_whenIdIsNull() {
    assertThrows(
        NullPointerException.class,
        () -> Chat.createGroup(null, CHAT_NAME, List.of(admin, member)));
  }

  @Test
  void createGroup_shouldThrowNullPointerException_whenMembersAreNull() {
    assertThrows(NullPointerException.class, () -> Chat.createGroup(chatId, CHAT_NAME, null));
  }

  @Test
  void createGroup_shouldThrowChatException_whenNameIsNull() {
    assertThrows(
        NullPointerException.class, () -> Chat.createGroup(chatId, null, List.of(admin, member)));
  }

  @Test
  void createGroup_shouldThrowChatException_whenNameIsBlank() {
    assertThrows(
        ChatException.class, () -> Chat.createGroup(chatId, "   ", List.of(admin, member)));
  }

  @Test
  void createGroup_shouldThrowChatException_whenMembersAreEmpty() {
    assertThrows(ChatException.class, () -> Chat.createGroup(chatId, CHAT_NAME, List.of()));
  }

  @Test
  void createGroup_shouldThrowChatException_whenGroupHasNoAdmin() {
    ChatMember anotherMember =
        createMember(
            UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789023"),
            createUser(
                UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789013"),
                "ghi9012",
                "John",
                "john@mail.com"),
            ChatRole.MEMBER);

    assertThrows(
        ChatException.class,
        () -> Chat.createGroup(chatId, CHAT_NAME, List.of(member, anotherMember)));
  }

  @Test
  void createGroup_shouldThrowChatException_whenGroupHasTooFewMembers() {
    assertThrows(ChatException.class, () -> Chat.createGroup(chatId, CHAT_NAME, List.of(admin)));
  }

  @Test
  void createGroup_shouldThrowChatException_whenMemberLimitIsExceeded() {
    List<ChatMember> members = new ArrayList<>();

    members.add(admin);

    for (int i = 1; i < ChatConstants.MAX_GROUP_MEMBERS + 1; i++) {
      UUID userId = new UUID(1L, i + 100L);
      UUID memberId = new UUID(2L, i + 100L);

      User additionalUser =
          createUser(userId, String.format("%07d", i), "User " + i, "user" + i + "@mail.com");

      members.add(createMember(memberId, additionalUser, ChatRole.MEMBER));
    }

    assertThrows(ChatException.class, () -> Chat.createGroup(chatId, CHAT_NAME, members));
  }

  // createPrivate

  @Test
  void createPrivate_shouldCreateChat_whenInputIsValid() {
    Chat chat = Chat.createPrivate(chatId, admin, member);

    assertEquals(chatId, chat.getId());
    assertEquals(ChatType.PRIVATE, chat.getType());
    assertEquals(2, chat.getMembers().size());
    assertNotNull(chat.getPrivateKey());
    assertNotNull(chat.getCreatedAt());
    assertNull(chat.getLastMessageAt());
  }

  @Test
  void createPrivate_shouldCreateNameFromUsernames() {
    Chat chat = Chat.createPrivate(chatId, admin, member);

    assertEquals(
        user.getUsername().getValue()
            + ChatConstants.SEPARATOR
            + anotherUser.getUsername().getValue(),
        chat.getName());
  }

  @Test
  void createPrivate_shouldThrowNullPointerException_whenIdIsNull() {
    assertThrows(NullPointerException.class, () -> Chat.createPrivate(null, admin, member));
  }

  @Test
  void createPrivate_shouldThrowNullPointerException_whenFirstMemberIsNull() {
    assertThrows(NullPointerException.class, () -> Chat.createPrivate(chatId, null, member));
  }

  @Test
  void createPrivate_shouldThrowNullPointerException_whenSecondMemberIsNull() {
    assertThrows(NullPointerException.class, () -> Chat.createPrivate(chatId, admin, null));
  }

  @Test
  void createPrivate_shouldThrowChatException_whenMembersHaveSameUserId() {
    ChatMember anotherMember = createMember(ANOTHER_MEMBER_UUID, user, ChatRole.MEMBER);

    assertThrows(ChatException.class, () -> Chat.createPrivate(chatId, admin, anotherMember));
  }

  // reconstitute

  @Test
  void reconstitute_shouldRestoreGroup_whenStateIsValid() {
    Chat chat =
        Chat.reconstitute(
            chatId,
            CHAT_NAME,
            ChatType.GROUP,
            Set.of(admin, member),
            CREATED_AT,
            MESSAGE_CREATED_AT,
            null);

    assertEquals(chatId, chat.getId());
    assertEquals(CHAT_NAME, chat.getName());
    assertEquals(ChatType.GROUP, chat.getType());
    assertEquals(2, chat.getMembers().size());
    assertEquals(CREATED_AT, chat.getCreatedAt());
    assertEquals(MESSAGE_CREATED_AT, chat.getLastMessageAt());
    assertNull(chat.getPrivateKey());
  }

  @Test
  void reconstitute_shouldThrowChatException_whenGroupHasNoMembers() {
    assertThrows(
        ChatException.class,
        () ->
            Chat.reconstitute(chatId, CHAT_NAME, ChatType.GROUP, Set.of(), CREATED_AT, null, null));
  }

  @Test
  void reconstitute_shouldThrowChatException_whenGroupHasPrivateKey() {
    assertThrows(
        ChatException.class,
        () ->
            Chat.reconstitute(
                chatId,
                CHAT_NAME,
                ChatType.GROUP,
                Set.of(admin, member),
                CREATED_AT,
                null,
                "private-key"));
  }

  @Test
  void reconstitute_shouldThrowChatException_whenPrivateChatHasWrongMemberCount() {
    assertThrows(
        ChatException.class,
        () ->
            Chat.reconstitute(
                chatId,
                CHAT_NAME,
                ChatType.PRIVATE,
                Set.of(admin),
                CREATED_AT,
                null,
                "private-key"));
  }

  @Test
  void reconstitute_shouldThrowChatException_whenPrivateKeyIsMissing() {
    assertThrows(
        ChatException.class,
        () ->
            Chat.reconstitute(
                chatId,
                CHAT_NAME,
                ChatType.PRIVATE,
                Set.of(admin, member),
                CREATED_AT,
                null,
                null));
  }

  @Test
  void reconstitute_shouldThrowChatException_whenPrivateKeyDoesNotMatchMembers() {
    assertThrows(
        ChatException.class,
        () ->
            Chat.reconstitute(
                chatId,
                CHAT_NAME,
                ChatType.PRIVATE,
                Set.of(admin, member),
                CREATED_AT,
                null,
                "invalid-private-key"));
  }

  // buildPrivateKey

  @Test
  void buildPrivateKey_shouldReturnSameKeyRegardlessOfUserOrder() {
    String firstKey = Chat.buildPrivateKey(user.getId(), anotherUser.getId());

    String secondKey = Chat.buildPrivateKey(anotherUser.getId(), user.getId());

    assertEquals(firstKey, secondKey);
  }

  @Test
  void buildPrivateKey_shouldThrowNullPointerException_whenFirstUserIdIsNull() {
    assertThrows(NullPointerException.class, () -> Chat.buildPrivateKey(null, anotherUser.getId()));
  }

  @Test
  void buildPrivateKey_shouldThrowNullPointerException_whenSecondUserIdIsNull() {
    assertThrows(NullPointerException.class, () -> Chat.buildPrivateKey(user.getId(), null));
  }

  // hasMember

  @Test
  void hasMember_shouldReturnTrue_whenUserIsMember() {
    Chat chat = Chat.createGroup(chatId, CHAT_NAME, List.of(admin, member));

    assertTrue(chat.hasMember(user.getId()));
    assertTrue(chat.hasMember(anotherUser.getId()));
  }

  @Test
  void hasMember_shouldReturnFalse_whenUserIsNotMember() {
    Chat chat = Chat.createGroup(chatId, CHAT_NAME, List.of(admin, member));

    User outsider =
        createUser(
            UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789014"),
            "jkl3456",
            "Outsider",
            "outsider@mail.com");

    assertFalse(chat.hasMember(outsider.getId()));
  }

  @Test
  void hasMember_shouldThrowNullPointerException_whenUserIdIsNull() {
    Chat chat = Chat.createGroup(chatId, CHAT_NAME, List.of(admin, member));

    assertThrows(NullPointerException.class, () -> chat.hasMember(null));
  }

  // markMessageAdded

  @Test
  void markMessageAdded_shouldUpdateLastMessageAt() {
    Chat chat = Chat.createGroup(chatId, CHAT_NAME, List.of(admin, member));

    chat.markMessageAdded(MESSAGE_CREATED_AT);

    assertEquals(MESSAGE_CREATED_AT, chat.getLastMessageAt());
  }

  @Test
  void markMessageAdded_shouldThrowNullPointerException_whenTimestampIsNull() {
    Chat chat = Chat.createGroup(chatId, CHAT_NAME, List.of(admin, member));

    assertThrows(NullPointerException.class, () -> chat.markMessageAdded(null));
  }

  // isPrivate

  @Test
  void isPrivate_shouldReturnTrue_whenChatIsPrivate() {
    Chat chat = Chat.createPrivate(chatId, admin, member);

    assertTrue(chat.isPrivate());
  }

  @Test
  void isPrivate_shouldReturnFalse_whenChatIsGroup() {
    Chat chat = Chat.createGroup(chatId, CHAT_NAME, List.of(admin, member));

    assertFalse(chat.isPrivate());
  }

  // addMember

  @Test
  void addMember_shouldAddUser_whenCurrentUserIsAdmin() {
    Chat chat = Chat.createGroup(chatId, CHAT_NAME, List.of(admin, member));

    User thirdUser =
        createUser(
            UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789015"),
            "mno7890",
            "Peter",
            "peter@mail.com");

    ChatMember thirdMember =
        createMember(
            UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789025"), thirdUser, ChatRole.MEMBER);

    chat.addMember(thirdMember, user.getId());

    assertEquals(3, chat.getMembers().size());
    assertTrue(chat.hasMember(thirdUser.getId()));
  }

  @Test
  void addMember_shouldThrowChatException_whenUserIsNotAdmin() {
    Chat chat = Chat.createGroup(chatId, CHAT_NAME, List.of(admin, member));

    User thirdUser =
        createUser(
            UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789015"),
            "mno7890",
            "Peter",
            "peter@mail.com");

    ChatMember thirdMember =
        createMember(
            UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789025"), thirdUser, ChatRole.MEMBER);

    assertThrows(ChatException.class, () -> chat.addMember(thirdMember, anotherUser.getId()));
  }

  @Test
  void addMember_shouldThrowChatException_whenUserAlreadyExists() {
    Chat chat = Chat.createGroup(chatId, CHAT_NAME, List.of(admin, member));

    ChatMember duplicateMember =
        createMember(
            UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789026"), anotherUser, ChatRole.MEMBER);

    assertThrows(ChatException.class, () -> chat.addMember(duplicateMember, user.getId()));
  }

  @Test
  void addMember_shouldThrowNullPointerException_whenMemberIsNull() {
    Chat chat = Chat.createGroup(chatId, CHAT_NAME, List.of(admin, member));

    assertThrows(NullPointerException.class, () -> chat.addMember(null, user.getId()));
  }

  @Test
  void addMember_shouldThrowNullPointerException_whenCurrentUserIdIsNull() {
    Chat chat = Chat.createGroup(chatId, CHAT_NAME, List.of(admin, member));

    assertThrows(NullPointerException.class, () -> chat.addMember(member, null));
  }

  @Test
  void addMember_shouldThrowChatException_whenCalledOnPrivateChat() {
    Chat chat = Chat.createPrivate(chatId, admin, member);

    User thirdUser =
        createUser(
            UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789015"),
            "mno7890",
            "Peter",
            "peter@mail.com");

    ChatMember thirdMember =
        createMember(
            UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789025"), thirdUser, ChatRole.MEMBER);

    assertThrows(ChatException.class, () -> chat.addMember(thirdMember, user.getId()));
  }

  // removeMember

  @Test
  void removeMember_shouldRemoveUser_whenCurrentUserIsAdmin() {
    Chat chat = Chat.createGroup(chatId, CHAT_NAME, List.of(admin, member));

    chat.removeMember(anotherUser.getId(), user.getId());

    assertEquals(1, chat.getMembers().size());
    assertTrue(chat.hasMember(user.getId()));
    assertFalse(chat.hasMember(anotherUser.getId()));
  }

  @Test
  void removeMember_shouldThrowChatException_whenCurrentUserIsNotAdmin() {
    Chat chat = Chat.createGroup(chatId, CHAT_NAME, List.of(admin, member));

    assertThrows(ChatException.class, () -> chat.removeMember(user.getId(), anotherUser.getId()));
  }

  @Test
  void removeMember_shouldThrowChatException_whenAdminRemovesThemself() {
    Chat chat = Chat.createGroup(chatId, CHAT_NAME, List.of(admin, member));

    assertThrows(ChatException.class, () -> chat.removeMember(user.getId(), user.getId()));
  }

  @Test
  void removeMember_shouldThrowChatException_whenTargetIsNotMember() {
    Chat chat = Chat.createGroup(chatId, CHAT_NAME, List.of(admin, member));

    User outsider =
        createUser(
            UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789014"),
            "jkl3456",
            "Outsider",
            "outsider@mail.com");

    assertThrows(ChatException.class, () -> chat.removeMember(outsider.getId(), user.getId()));
  }

  @Test
  void removeMember_shouldThrowNullPointerException_whenTargetUserIdIsNull() {
    Chat chat = Chat.createGroup(chatId, CHAT_NAME, List.of(admin, member));

    assertThrows(NullPointerException.class, () -> chat.removeMember(null, user.getId()));
  }

  @Test
  void removeMember_shouldThrowNullPointerException_whenCurrentUserIdIsNull() {
    Chat chat = Chat.createGroup(chatId, CHAT_NAME, List.of(admin, member));

    assertThrows(NullPointerException.class, () -> chat.removeMember(anotherUser.getId(), null));
  }

  @Test
  void removeMember_shouldThrowChatException_whenCalledOnPrivateChat() {
    Chat chat = Chat.createPrivate(chatId, admin, member);

    assertThrows(ChatException.class, () -> chat.removeMember(anotherUser.getId(), user.getId()));
  }

  // leaveChat

  @Test
  void leaveChat_shouldRemoveMember_whenCurrentUserIsNotAdmin() {
    Chat chat = Chat.createGroup(chatId, CHAT_NAME, List.of(admin, member));

    chat.leaveChat(anotherUser.getId());

    assertEquals(1, chat.getMembers().size());
    assertFalse(chat.hasMember(anotherUser.getId()));
  }

  @Test
  void leaveChat_shouldThrowChatException_whenCurrentUserIsAdmin() {
    Chat chat = Chat.createGroup(chatId, CHAT_NAME, List.of(admin, member));

    assertThrows(ChatException.class, () -> chat.leaveChat(user.getId()));
  }

  @Test
  void leaveChat_shouldThrowChatException_whenUserIsNotMember() {
    Chat chat = Chat.createGroup(chatId, CHAT_NAME, List.of(admin, member));

    User outsider =
        createUser(
            UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789014"),
            "jkl3456",
            "Outsider",
            "outsider@mail.com");

    assertThrows(ChatException.class, () -> chat.leaveChat(outsider.getId()));
  }

  @Test
  void leaveChat_shouldThrowNullPointerException_whenUserIdIsNull() {
    Chat chat = Chat.createGroup(chatId, CHAT_NAME, List.of(admin, member));

    assertThrows(NullPointerException.class, () -> chat.leaveChat(null));
  }

  // renameChat

  @Test
  void renameChat_shouldChangeName_whenCurrentUserIsAdmin() {
    Chat chat = Chat.createGroup(chatId, CHAT_NAME, List.of(admin, member));

    chat.renameChat(user.getId(), "New name");

    assertEquals("New name", chat.getName());
  }

  @Test
  void renameChat_shouldTrimName_whenNameContainsWhitespace() {
    Chat chat = Chat.createGroup(chatId, CHAT_NAME, List.of(admin, member));

    chat.renameChat(user.getId(), "  New name  ");

    assertEquals("New name", chat.getName());
  }

  @Test
  void renameChat_shouldThrowChatException_whenCurrentUserIsNotAdmin() {
    Chat chat = Chat.createGroup(chatId, CHAT_NAME, List.of(admin, member));

    assertThrows(ChatException.class, () -> chat.renameChat(anotherUser.getId(), "New name"));
  }

  @Test
  void renameChat_shouldThrowChatException_whenNameIsBlank() {
    Chat chat = Chat.createGroup(chatId, CHAT_NAME, List.of(admin, member));

    assertThrows(ChatException.class, () -> chat.renameChat(user.getId(), "   "));
  }

  @Test
  void renameChat_shouldThrowNullPointerException_whenUserIdIsNull() {
    Chat chat = Chat.createGroup(chatId, CHAT_NAME, List.of(admin, member));

    assertThrows(NullPointerException.class, () -> chat.renameChat(null, "New name"));
  }

  @Test
  void renameChat_shouldThrowNullPointerException_whenNameIsNull() {
    Chat chat = Chat.createGroup(chatId, CHAT_NAME, List.of(admin, member));

    assertThrows(NullPointerException.class, () -> chat.renameChat(user.getId(), null));
  }

  @Test
  void renameChat_shouldThrowChatException_whenCalledOnPrivateChat() {
    Chat chat = Chat.createPrivate(chatId, admin, member);

    assertThrows(ChatException.class, () -> chat.renameChat(user.getId(), "New name"));
  }

  // ensureCanDelete

  @Test
  void ensureCanDelete_shouldNotThrow_whenAdminDeletesGroup() {
    Chat chat = Chat.createGroup(chatId, CHAT_NAME, List.of(admin, member));

    chat.ensureCanDelete(user.getId());
  }

  @Test
  void ensureCanDelete_shouldNotThrow_whenMemberDeletesPrivateChat() {
    Chat chat = Chat.createPrivate(chatId, admin, member);

    chat.ensureCanDelete(anotherUser.getId());
  }

  @Test
  void ensureCanDelete_shouldThrowChatException_whenMemberDeletesGroup() {
    Chat chat = Chat.createGroup(chatId, CHAT_NAME, List.of(admin, member));

    assertThrows(ChatException.class, () -> chat.ensureCanDelete(anotherUser.getId()));
  }

  @Test
  void ensureCanDelete_shouldThrowChatException_whenUserIsNotMember() {
    Chat chat = Chat.createGroup(chatId, CHAT_NAME, List.of(admin, member));

    User outsider =
        createUser(
            UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789014"),
            "jkl3456",
            "Outsider",
            "outsider@mail.com");

    assertThrows(ChatException.class, () -> chat.ensureCanDelete(outsider.getId()));
  }

  @Test
  void ensureCanDelete_shouldThrowNullPointerException_whenUserIdIsNull() {
    Chat chat = Chat.createGroup(chatId, CHAT_NAME, List.of(admin, member));

    assertThrows(NullPointerException.class, () -> chat.ensureCanDelete(null));
  }

  // getMembers

  @Test
  void getMembers_shouldReturnUnmodifiableSet() {
    Chat chat = Chat.createGroup(chatId, CHAT_NAME, List.of(admin, member));

    assertThrows(UnsupportedOperationException.class, () -> chat.getMembers().clear());
  }

  @Test
  void getMembers_shouldNotExposeOriginalSet() {
    Set<ChatMember> members = new HashSet<>();
    members.add(admin);
    members.add(member);

    Chat chat =
        Chat.reconstitute(chatId, CHAT_NAME, ChatType.GROUP, members, CREATED_AT, null, null);

    members.clear();

    assertEquals(2, chat.getMembers().size());
  }

  // equals

  @Test
  void equals_shouldReturnTrue_whenObjectsAreTheSame() {
    Chat chat = Chat.createGroup(chatId, CHAT_NAME, List.of(admin, member));

    assertEquals(chat, chat);
  }

  @Test
  void equals_shouldReturnTrue_whenIdsAreEqual() {
    Chat first =
        Chat.reconstitute(
            chatId, "First chat", ChatType.GROUP, Set.of(admin, member), CREATED_AT, null, null);

    Chat second =
        Chat.reconstitute(
            chatId,
            "Second chat",
            ChatType.GROUP,
            Set.of(admin, member),
            CREATED_AT,
            MESSAGE_CREATED_AT,
            null);

    assertEquals(first, second);
  }

  @Test
  void equals_shouldReturnFalse_whenIdsAreDifferent() {
    Chat first = Chat.createGroup(chatId, CHAT_NAME, List.of(admin, member));

    Chat second =
        Chat.reconstitute(
            new ChatId(ANOTHER_CHAT_UUID),
            CHAT_NAME,
            ChatType.GROUP,
            Set.of(admin, member),
            CREATED_AT,
            null,
            null);

    assertNotEquals(first, second);
  }

  @Test
  void equals_shouldReturnFalse_whenObjectIsNull() {
    Chat chat = Chat.createGroup(chatId, CHAT_NAME, List.of(admin, member));

    assertNotEquals(null, chat);
  }

  @Test
  void equals_shouldReturnFalse_whenObjectHasDifferentType() {
    Chat chat = Chat.createGroup(chatId, CHAT_NAME, List.of(admin, member));

    assertThat(chat.equals("Chat")).isFalse();
  }

  // hashCode

  @Test
  void hashCode_shouldBeEqual_whenIdsAreEqual() {
    Chat first =
        Chat.reconstitute(
            chatId, "First chat", ChatType.GROUP, Set.of(admin, member), CREATED_AT, null, null);

    Chat second =
        Chat.reconstitute(
            chatId,
            "Second chat",
            ChatType.GROUP,
            Set.of(admin, member),
            CREATED_AT,
            MESSAGE_CREATED_AT,
            null);

    assertEquals(first.hashCode(), second.hashCode());
  }

  @Test
  void hashCode_shouldBeConsistent_whenCalledMultipleTimes() {
    Chat chat = Chat.createGroup(chatId, CHAT_NAME, List.of(admin, member));

    assertEquals(chat.hashCode(), chat.hashCode());
  }

  @Test
  void reconstitute_shouldThrowChatException_whenGroupExceedsMaximumMemberLimit() {
    Set<ChatMember> members = new HashSet<>();
    members.add(admin);

    for (int i = 1; i < ChatConstants.MAX_GROUP_MEMBERS + 1; i++) {
      User additionalUser =
          createUser(
              new UUID(1L, i), String.format("%07d", i), "User " + i, "user" + i + "@mail.com");

      ChatMember additionalMember = createMember(new UUID(2L, i), additionalUser, ChatRole.MEMBER);

      members.add(additionalMember);
    }

    ChatException exception =
        assertThrows(
            ChatException.class,
            () ->
                Chat.reconstitute(
                    chatId, CHAT_NAME, ChatType.GROUP, members, CREATED_AT, null, null));

    assertEquals(ChatError.GROUP_MEMBER_LIMIT_EXCEEDED, exception.getError());
  }

  @Test
  void addMember_shouldThrowChatException_whenGroupHasMaximumMembers() {
    Set<ChatMember> members = new HashSet<>();
    members.add(admin);

    for (int i = 1; i < ChatConstants.MAX_GROUP_MEMBERS; i++) {
      User additionalUser =
          createUser(
              new UUID(1L, i), String.format("%07d", i), "User " + i, "user" + i + "@mail.com");

      ChatMember additionalMember = createMember(new UUID(2L, i), additionalUser, ChatRole.MEMBER);

      members.add(additionalMember);
    }

    Chat chat = Chat.createGroup(chatId, CHAT_NAME, members.stream().toList());

    User newUser =
        createUser(
            UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789099"),
            "newuser",
            "New User",
            "newuser@mail.com");

    ChatMember newMember =
        createMember(
            UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789098"), newUser, ChatRole.MEMBER);

    ChatException exception =
        assertThrows(ChatException.class, () -> chat.addMember(newMember, admin.getUserId()));

    assertEquals(ChatError.GROUP_MEMBER_LIMIT_EXCEEDED, exception.getError());
  }

  @Test
  void removeMember_shouldThrowChatException_whenRemovingLastAdmin() {
    Chat chat = Chat.createGroup(chatId, CHAT_NAME, List.of(admin, member));

    ChatException exception =
        assertThrows(
            ChatException.class, () -> chat.removeMember(admin.getUserId(), admin.getUserId()));

    assertEquals(ChatError.CANNOT_REMOVE_YOURSELF, exception.getError());
  }

  @Test
  void removeMember_shouldRemoveAdmin_whenAnotherAdminRemains() {
    ChatMember anotherAdmin = createMember(ANOTHER_MEMBER_UUID, anotherUser, ChatRole.ADMIN);

    Chat chat = Chat.createGroup(chatId, CHAT_NAME, List.of(admin, anotherAdmin, member));

    chat.removeMember(admin.getUserId(), anotherAdmin.getUserId());

    assertFalse(chat.hasMember(admin.getUserId()));
    assertTrue(chat.hasMember(anotherAdmin.getUserId()));
  }

  @Test
  void removeMember_shouldRemoveMember_whenTargetIsNotAdmin() {
    Chat chat = Chat.createGroup(chatId, CHAT_NAME, List.of(admin, member));

    chat.removeMember(member.getUserId(), admin.getUserId());

    assertFalse(chat.hasMember(member.getUserId()));
    assertTrue(chat.hasMember(admin.getUserId()));
  }

  @Test
  void createGroup_shouldThrowChatException_whenNameIsTooLong() {
    String name = "a".repeat(ChatConstants.MAX_CHAT_NAME_LENGTH + 1);

    ChatException exception =
        assertThrows(
            ChatException.class, () -> Chat.createGroup(chatId, name, List.of(admin, member)));

    assertEquals(ChatError.CHAT_NAME_TOO_LONG, exception.getError());
  }

  @Test
  void removeMember_shouldRemoveMember_whenMemberExistsInChat() {
    Chat chat = Chat.createGroup(chatId, CHAT_NAME, List.of(admin, member));

    chat.removeMember(member.getUserId(), admin.getUserId());

    assertFalse(chat.hasMember(member.getUserId()));
    assertTrue(chat.hasMember(admin.getUserId()));
  }

  @Test
  void reconstitute_shouldThrowChatException_whenPrivateKeyIsNull() {
    ChatException exception =
        assertThrows(
            ChatException.class,
            () ->
                Chat.reconstitute(
                    chatId,
                    CHAT_NAME,
                    ChatType.PRIVATE,
                    Set.of(admin, member),
                    CREATED_AT,
                    null,
                    null));

    assertEquals(ChatError.PRIVATE_CHAT_MUST_HAVE_PRIVATE_KEY, exception.getError());
  }

  @Test
  void reconstitute_shouldThrowChatException_whenPrivateKeyIsBlank() {
    ChatException exception =
        assertThrows(
            ChatException.class,
            () ->
                Chat.reconstitute(
                    chatId,
                    CHAT_NAME,
                    ChatType.PRIVATE,
                    Set.of(admin, member),
                    CREATED_AT,
                    null,
                    "   "));

    assertEquals(ChatError.PRIVATE_CHAT_MUST_HAVE_PRIVATE_KEY, exception.getError());
  }

  @Test
  void reconstitute_shouldThrowChatException_whenPrivateChatHasNoAdmins() {
    ChatMember firstMember = createMember(MEMBER_UUID, user, ChatRole.MEMBER);

    ChatMember secondMember = createMember(ANOTHER_MEMBER_UUID, anotherUser, ChatRole.MEMBER);

    String privateKey = Chat.buildPrivateKey(user.getId(), anotherUser.getId());

    ChatException exception =
        assertThrows(
            ChatException.class,
            () ->
                Chat.reconstitute(
                    chatId,
                    CHAT_NAME,
                    ChatType.PRIVATE,
                    Set.of(firstMember, secondMember),
                    CREATED_AT,
                    null,
                    privateKey));

    assertEquals(ChatError.PRIVATE_CHAT_MUST_HAVE_ONE_ADMIN, exception.getError());
  }

  @Test
  void reconstitute_shouldThrowChatException_whenPrivateChatHasTwoAdmins() {
    ChatMember firstAdmin = createMember(MEMBER_UUID, user, ChatRole.ADMIN);

    ChatMember secondAdmin = createMember(ANOTHER_MEMBER_UUID, anotherUser, ChatRole.ADMIN);

    String privateKey = Chat.buildPrivateKey(user.getId(), anotherUser.getId());

    ChatException exception =
        assertThrows(
            ChatException.class,
            () ->
                Chat.reconstitute(
                    chatId,
                    CHAT_NAME,
                    ChatType.PRIVATE,
                    Set.of(firstAdmin, secondAdmin),
                    CREATED_AT,
                    null,
                    privateKey));

    assertEquals(ChatError.PRIVATE_CHAT_MUST_HAVE_ONE_ADMIN, exception.getError());
  }

  // Helpers

  private static User createUser(UUID id, String handle, String username, String email) {
    return User.create(
        new UserId(id),
        new Handle(handle),
        new Username(username),
        new Email(email),
        new PasswordHash("password-hash"),
        Role.USER);
  }

  private static ChatMember createMember(UUID id, User user, ChatRole role) {
    return ChatMember.create(new ChatMemberId(id), user, role);
  }
}

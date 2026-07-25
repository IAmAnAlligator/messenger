package com.jeannimi.messenger.kafka;

public final class KafkaTopics {

  private KafkaTopics() {}

  public static final String CHAT_EVENTS = "chat.events";

  public static final String CHAT_MESSAGES = "chat.messages";

  public static final String CHAT_READ = "chat.read";

  public static final String CHAT_MESSAGE_DELETED = "chat.delete";
  //
  //  public static final String CHAT_CREATED = "chat.created";
  //
  //  public static final String CHAT_DELETED = "chat.deleted";
  //
  //  public static final String CHAT_MEMBER_ADDED = "chat.member.added";
  //
  //  public static final String CHAT_MEMBER_REMOVED = "chat.member.removed";
  //
  //  public static final String CHAT_MEMBER_LEFT = "chat.member.left";
  //
  //  public static final String CHAT_RENAMED = "chat.renamed";
}

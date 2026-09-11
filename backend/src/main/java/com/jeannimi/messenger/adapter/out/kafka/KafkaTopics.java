package com.jeannimi.messenger.adapter.out.kafka;

public final class KafkaTopics {

  private KafkaTopics() {}

  public static final String CHAT_EVENTS = "chat.events";

  public static final String CHAT_MESSAGES = "chat.messages";

  public static final String CHAT_READ = "chat.read";

  public static final String CHAT_MESSAGE_DELETED = "chat.delete";

  public static final String FILE_DELETE = "file.delete";
}

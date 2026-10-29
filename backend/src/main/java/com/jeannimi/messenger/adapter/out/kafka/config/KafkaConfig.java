package com.jeannimi.messenger.adapter.out.kafka.config;

import com.jeannimi.messenger.adapter.in.kafka.exception.InvalidKafkaEventException;
import com.jeannimi.messenger.adapter.out.kafka.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

  // =========================
  // TOPICS
  // =========================

  @Bean
  public NewTopic chatMessagesTopic() {
    return new NewTopic(KafkaTopics.CHAT_MESSAGES, 3, (short) 1);
  }

  @Bean
  public NewTopic chatEventsTopic() {
    return new NewTopic(KafkaTopics.CHAT_EVENTS, 1, (short) 1);
  }

  @Bean
  public NewTopic chatReadTopic() {
    return new NewTopic(KafkaTopics.CHAT_READ, 1, (short) 1);
  }

  @Bean
  public NewTopic chatMessageDeletedTopic() {
    return new NewTopic(KafkaTopics.CHAT_MESSAGE_DELETED, 1, (short) 1);
  }

  // =========================
  // DEAD LETTER TOPICS
  // =========================

  @Bean
  public NewTopic chatMessagesDltTopic() {
    return new NewTopic(KafkaTopics.CHAT_MESSAGES + "-dlt", 3, (short) 1);
  }

  @Bean
  public NewTopic chatEventsDltTopic() {
    return new NewTopic(KafkaTopics.CHAT_EVENTS + "-dlt", 1, (short) 1);
  }

  @Bean
  public NewTopic chatReadDltTopic() {
    return new NewTopic(KafkaTopics.CHAT_READ + "-dlt", 1, (short) 1);
  }

  @Bean
  public NewTopic chatMessageDeletedDltTopic() {
    return new NewTopic(KafkaTopics.CHAT_MESSAGE_DELETED + "-dlt", 1, (short) 1);
  }

  // =========================
  // COMMON ERROR HANDLER
  // =========================

  @Bean
  public DefaultErrorHandler errorHandler(KafkaTemplate<String, String> kafkaTemplate) {
    DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);

    DefaultErrorHandler errorHandler =
        new DefaultErrorHandler(recoverer, new FixedBackOff(1_000L, 5L));

    errorHandler.addNotRetryableExceptions(InvalidKafkaEventException.class);

    errorHandler.setCommitRecovered(true);

    return errorHandler;
  }

  // =========================
  // CHAT MESSAGES CONSUMER
  // =========================

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, String>
      chatMessagesKafkaListenerContainerFactory(
          ConsumerFactory<String, String> consumerFactory, DefaultErrorHandler errorHandler) {
    return createFactory(consumerFactory, errorHandler);
  }

  // =========================
  // CHAT EVENTS CONSUMER
  // =========================

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, String>
      chatEventsKafkaListenerContainerFactory(
          ConsumerFactory<String, String> consumerFactory, DefaultErrorHandler errorHandler) {
    return createFactory(consumerFactory, errorHandler);
  }

  // =========================
  // READ EVENTS CONSUMER
  // =========================

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, String>
      chatReadKafkaListenerContainerFactory(
          ConsumerFactory<String, String> consumerFactory, DefaultErrorHandler errorHandler) {
    return createFactory(consumerFactory, errorHandler);
  }

  // =========================
  // FILE DELETE CONSUMER
  // =========================

  @Bean(name = "fileDeleteKafkaListenerContainerFactory")
  public ConcurrentKafkaListenerContainerFactory<String, String>
      fileDeleteKafkaListenerContainerFactory(
          ConsumerFactory<String, String> consumerFactory, DefaultErrorHandler errorHandler) {
    return createFactory(consumerFactory, errorHandler);
  }

  // =========================
  // FACTORY CREATION
  // =========================

  private ConcurrentKafkaListenerContainerFactory<String, String> createFactory(
      ConsumerFactory<String, String> consumerFactory, DefaultErrorHandler errorHandler) {
    var factory = new ConcurrentKafkaListenerContainerFactory<String, String>();

    factory.setConsumerFactory(consumerFactory);
    factory.setConcurrency(1);
    factory.setCommonErrorHandler(errorHandler);

    factory.getContainerProperties().setAckMode(AckMode.MANUAL_IMMEDIATE);

    return factory;
  }
}

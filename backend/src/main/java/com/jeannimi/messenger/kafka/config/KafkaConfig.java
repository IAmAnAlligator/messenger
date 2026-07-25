package com.jeannimi.messenger.kafka.config;

import com.jeannimi.messenger.kafka.KafkaTopics;
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

  // =========================
  // ERROR HANDLING
  // =========================

  @Bean
  public DefaultErrorHandler errorHandler(KafkaTemplate<String, String> kafkaTemplate) {

    DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);

    return new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 5));
  }

  // =========================
  // CHAT MESSAGES CONSUMER
  // =========================

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, String>
      chatMessagesKafkaListenerContainerFactory(
          ConsumerFactory<String, String> consumerFactory, DefaultErrorHandler errorHandler) {

    var factory = new ConcurrentKafkaListenerContainerFactory<String, String>();

    factory.setConsumerFactory(consumerFactory);

    // 3 partitions:
    //
    // partition-0 -> consumer-1
    // partition-1 -> consumer-2
    // partition-2 -> consumer-3

    factory.setConcurrency(3);

    factory.setCommonErrorHandler(errorHandler);

    factory.getContainerProperties().setAckMode(AckMode.MANUAL_IMMEDIATE);

    return factory;
  }

  // =========================
  // CHAT EVENTS CONSUMER
  // =========================

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, String>
      chatEventsKafkaListenerContainerFactory(
          ConsumerFactory<String, String> consumerFactory, DefaultErrorHandler errorHandler) {

    var factory = new ConcurrentKafkaListenerContainerFactory<String, String>();

    factory.setConsumerFactory(consumerFactory);

    // события чата:
    //
    // chat.created
    // chat.deleted
    // chat.member.added
    // chat.member.removed
    // chat.renamed

    factory.setConcurrency(1);

    factory.setCommonErrorHandler(errorHandler);

    factory.getContainerProperties().setAckMode(AckMode.MANUAL_IMMEDIATE);

    return factory;
  }

  // =========================
  // READ EVENTS CONSUMER
  // =========================

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, String>
      chatReadKafkaListenerContainerFactory(
          ConsumerFactory<String, String> consumerFactory, DefaultErrorHandler errorHandler) {

    var factory = new ConcurrentKafkaListenerContainerFactory<String, String>();

    factory.setConsumerFactory(consumerFactory);

    // один partition ->
    // один consumer

    factory.setConcurrency(1);

    factory.setCommonErrorHandler(errorHandler);

    factory.getContainerProperties().setAckMode(AckMode.MANUAL_IMMEDIATE);

    return factory;
  }
}

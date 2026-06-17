package com.jeannimi.messenger.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

  @Bean
  public NewTopic chatMessagesTopic() {
    // можно параллелить обработку до 3 consumers
    return new NewTopic("chat.messages", 3, (short) 1);
  }

  @Bean
  public NewTopic chatReadTopic() {
    // порядок важнее, поэтому 1 partition
    return new NewTopic("chat.read", 1, (short) 1);
  }

  @Bean
  public DefaultErrorHandler errorHandler() {
    // Если consumer падает:
    //retry каждые 1 сек
    //максимум 5 попыток
    return new DefaultErrorHandler(new FixedBackOff(1000L, 5));
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
      ConsumerFactory<String, String> consumerFactory, DefaultErrorHandler errorHandler) {

    var factory = new ConcurrentKafkaListenerContainerFactory<String, String>();

    factory.setConsumerFactory(consumerFactory);

    // 3 consumer thread'а
    //параллельная обработка partitions
    factory.setConcurrency(3);

    factory.setCommonErrorHandler(errorHandler);

    // ты сам решаешь, когда commit происходит ack.acknowledge();
    factory.getContainerProperties().setAckMode(AckMode.MANUAL_IMMEDIATE);

    return factory;
  }
}

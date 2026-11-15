package com.jeannimi.messenger.adapter.out.encryption;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(EncryptionProperties.class)
public class EncryptionConfiguration {

  @Bean
  @Qualifier("messageKeyRing")
  public AesGcmKeyRing messageKeyRing(EncryptionProperties properties) {
    return new AesGcmKeyRing(properties.messages());
  }

  @Bean
  @Qualifier("fileKeyRing")
  public AesGcmKeyRing fileKeyRing(EncryptionProperties properties) {
    return new AesGcmKeyRing(properties.files());
  }
}

package com.jeannimi.messenger.adapter.out.encryption;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "encryption")
public record EncryptionProperties(
    KeyRing messages,
    KeyRing files
) {

  public record KeyRing(
      int activeKeyId,
      Map<Integer, String> keys
  ) {
  }
}
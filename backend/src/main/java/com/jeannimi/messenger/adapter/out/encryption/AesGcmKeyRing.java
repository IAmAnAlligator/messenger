package com.jeannimi.messenger.adapter.out.encryption;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.spec.SecretKeySpec;

public final class AesGcmKeyRing {

  private static final int KEY_SIZE_BYTES = 32;

  private final int activeKeyId;
  private final Map<Integer, SecretKeySpec> keys;

  public AesGcmKeyRing(EncryptionProperties.KeyRing properties) {

    if (properties == null || properties.keys() == null || properties.keys().isEmpty()) {
      throw new IllegalStateException("Encryption keys are not configured");
    }

    this.activeKeyId = properties.activeKeyId();

    if (activeKeyId < 1 || activeKeyId > 255) {
      throw new IllegalStateException("Invalid active encryption key ID");
    }

    Map<Integer, SecretKeySpec> keyMap = new HashMap<>();

    properties
        .keys()
        .forEach(
            (keyId, base64Key) -> {
              if (keyId == null || keyId < 1 || keyId > 255) {
                throw new IllegalStateException("Invalid encryption key ID");
              }

              if (base64Key == null || base64Key.isBlank()) {
                throw new IllegalStateException("Encryption key must not be empty: " + keyId);
              }

              try {
                byte[] keyBytes = Base64.getDecoder().decode(base64Key);

                if (keyBytes.length != KEY_SIZE_BYTES) {
                  throw new IllegalStateException("Encryption key must contain exactly 32 bytes");
                }

                keyMap.put(keyId, new SecretKeySpec(keyBytes, "AES"));

              } catch (IllegalArgumentException e) {
                throw new IllegalStateException("Invalid Base64 encryption key: " + keyId, e);
              }
            });

    if (!keyMap.containsKey(activeKeyId)) {
      throw new IllegalStateException("Active encryption key is not configured");
    }

    this.keys = Map.copyOf(keyMap);
  }

  public int activeKeyId() {
    return activeKeyId;
  }

  public SecretKeySpec activeKey() {
    return getKey(activeKeyId);
  }

  public SecretKeySpec getKey(int keyId) {

    SecretKeySpec key = keys.get(keyId);

    if (key == null) {
      throw new IllegalArgumentException("Unknown encryption key ID: " + keyId);
    }

    return key;
  }
}

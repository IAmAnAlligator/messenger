package com.jeannimi.messenger.adapter.out.encryption;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.security.SecureRandom;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AesGcmMessageEncryptionAdapterTest {

  private AesGcmMessageEncryptionAdapter encryption;

  @BeforeEach
  void setUp() {

    byte[] key = new byte[32];

    new SecureRandom().nextBytes(key);

    String base64Key =
        Base64.getEncoder().encodeToString(key);

    encryption =
        new AesGcmMessageEncryptionAdapter(base64Key);
  }

  @Test
  void shouldEncryptAndDecrypt() {

    String plaintext = "Привет, как дела?";

    String encrypted =
        encryption.encrypt(plaintext);

    String decrypted =
        encryption.decrypt(encrypted);

    assertEquals(plaintext, decrypted);
  }

  @Test
  void encryptedTextShouldNotContainPlaintext() {

    String plaintext = "Привет, как дела?";

    String encrypted =
        encryption.encrypt(plaintext);

    assertNotEquals(plaintext, encrypted);
  }

  @Test
  void encryptingSameTextTwiceShouldProduceDifferentCiphertext() {

    String plaintext = "Hello";

    String encrypted1 =
        encryption.encrypt(plaintext);

    String encrypted2 =
        encryption.encrypt(plaintext);

    assertNotEquals(encrypted1, encrypted2);
  }

  @Test
  void shouldRejectModifiedCiphertext() {

    String encrypted =
        encryption.encrypt("Hello");

    String modified =
        encrypted.substring(0, encrypted.length() - 1)
            + (encrypted.endsWith("A") ? "B" : "A");

    assertThrows(
        IllegalStateException.class,
        () -> encryption.decrypt(modified));
  }
}
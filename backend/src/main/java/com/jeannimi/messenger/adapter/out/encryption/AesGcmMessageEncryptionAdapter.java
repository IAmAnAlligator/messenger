package com.jeannimi.messenger.adapter.out.encryption;

import com.jeannimi.messenger.application.port.out.MessageEncryptionPort;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class AesGcmMessageEncryptionAdapter implements MessageEncryptionPort {

  private static final String TRANSFORMATION = "AES/GCM/NoPadding";

  private static final String FORMAT_VERSION = "v1";

  private static final int IV_SIZE_BYTES = 12;
  private static final int TAG_SIZE_BITS = 128;
  private static final int TAG_SIZE_BYTES = TAG_SIZE_BITS / 8;

  private final AesGcmKeyRing keyRing;
  private final SecureRandom secureRandom;

  public AesGcmMessageEncryptionAdapter(@Qualifier("messageKeyRing") AesGcmKeyRing keyRing) {
    this.keyRing = keyRing;
    this.secureRandom = new SecureRandom();
  }

  @Override
  public String encrypt(String plaintext) {

    if (plaintext == null) {
      throw new IllegalArgumentException("Plaintext must not be null");
    }

    int keyId = keyRing.activeKeyId();
    SecretKeySpec key = keyRing.activeKey();

    byte[] iv = generateIv();

    try {
      Cipher cipher = Cipher.getInstance(TRANSFORMATION);

      cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_SIZE_BITS, iv));

      cipher.updateAAD((FORMAT_VERSION + ":" + keyId).getBytes(StandardCharsets.UTF_8));

      byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

      return FORMAT_VERSION
          + ":"
          + keyId
          + ":"
          + Base64.getEncoder().encodeToString(iv)
          + ":"
          + Base64.getEncoder().encodeToString(ciphertext);

    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("Failed to encrypt message", e);
    }
  }

  @Override
  public String decrypt(String encryptedText) {

    if (encryptedText == null) {
      throw new IllegalArgumentException("Encrypted text must not be null");
    }

    String[] parts = encryptedText.split(":", -1);

    if (parts.length != 4 || !FORMAT_VERSION.equals(parts[0])) {
      throw new IllegalArgumentException("Invalid encrypted message format");
    }

    int keyId;

    try {
      keyId = Integer.parseInt(parts[1]);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid encryption key ID", e);
    }

    SecretKeySpec key = keyRing.getKey(keyId);

    byte[] iv;
    byte[] ciphertext;

    try {
      iv = Base64.getDecoder().decode(parts[2]);
      ciphertext = Base64.getDecoder().decode(parts[3]);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid Base64 in encrypted message", e);
    }

    if (iv.length != IV_SIZE_BYTES) {
      throw new IllegalArgumentException("Invalid AES-GCM IV length");
    }

    if (ciphertext.length < TAG_SIZE_BYTES) {
      throw new IllegalArgumentException("Invalid AES-GCM ciphertext length");
    }

    try {
      Cipher cipher = Cipher.getInstance(TRANSFORMATION);

      cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_SIZE_BITS, iv));

      cipher.updateAAD((FORMAT_VERSION + ":" + keyId).getBytes(StandardCharsets.UTF_8));

      byte[] plaintext = cipher.doFinal(ciphertext);

      return new String(plaintext, StandardCharsets.UTF_8);

    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("Failed to decrypt message", e);
    }
  }

  private byte[] generateIv() {
    byte[] iv = new byte[IV_SIZE_BYTES];
    secureRandom.nextBytes(iv);
    return iv;
  }
}

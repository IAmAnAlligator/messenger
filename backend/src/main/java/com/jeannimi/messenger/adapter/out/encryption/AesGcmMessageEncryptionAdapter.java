package com.jeannimi.messenger.adapter.out.encryption;

import com.jeannimi.messenger.application.port.out.MessageEncryptionPort;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AesGcmMessageEncryptionAdapter implements MessageEncryptionPort {

  private static final String ALGORITHM = "AES";
  private static final String TRANSFORMATION = "AES/GCM/NoPadding";

  private static final int KEY_SIZE_BYTES = 32;
  private static final int IV_SIZE_BYTES = 12;
  private static final int TAG_SIZE_BITS = 128;
  private static final int TAG_SIZE_BYTES = TAG_SIZE_BITS / 8;

  private static final String FORMAT_VERSION = "v1";
  private static final byte BINARY_FORMAT_VERSION = 1;

  private final SecretKeySpec secretKey;
  private final SecureRandom secureRandom;

  public AesGcmMessageEncryptionAdapter(
      @Value("${encryption.key}") String base64Key) {

    byte[] keyBytes;

    try {
      keyBytes = Base64.getDecoder().decode(base64Key);
    } catch (IllegalArgumentException e) {
      throw new IllegalStateException(
          "Message encryption key must be valid Base64", e);
    }

    if (keyBytes.length != KEY_SIZE_BYTES) {
      throw new IllegalStateException(
          "Message encryption key must contain exactly 32 bytes");
    }

    this.secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
    this.secureRandom = new SecureRandom();
  }

  // =========================
  // String encryption
  // =========================

  @Override
  public String encrypt(String plaintext) {

    if (plaintext == null) {
      throw new IllegalArgumentException("Plaintext must not be null");
    }

    byte[] iv = generateIv();

    try {
      Cipher cipher = Cipher.getInstance(TRANSFORMATION);

      cipher.init(
          Cipher.ENCRYPT_MODE,
          secretKey,
          new GCMParameterSpec(TAG_SIZE_BITS, iv)
      );

      byte[] ciphertext = cipher.doFinal(
          plaintext.getBytes(StandardCharsets.UTF_8)
      );

      return FORMAT_VERSION
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
      throw new IllegalArgumentException(
          "Encrypted text must not be null");
    }

    String[] parts = encryptedText.split(":", 3);

    if (parts.length != 3) {
      throw new IllegalArgumentException(
          "Invalid encrypted message format");
    }

    if (!FORMAT_VERSION.equals(parts[0])) {
      throw new IllegalArgumentException(
          "Unsupported encrypted message version: " + parts[0]);
    }

    byte[] iv;
    byte[] ciphertext;

    try {
      iv = Base64.getDecoder().decode(parts[1]);
      ciphertext = Base64.getDecoder().decode(parts[2]);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          "Invalid Base64 in encrypted message", e);
    }

    if (iv.length != IV_SIZE_BYTES) {
      throw new IllegalArgumentException(
          "Invalid AES-GCM IV length");
    }

    try {
      Cipher cipher = Cipher.getInstance(TRANSFORMATION);

      cipher.init(
          Cipher.DECRYPT_MODE,
          secretKey,
          new GCMParameterSpec(TAG_SIZE_BITS, iv)
      );

      byte[] plaintext = cipher.doFinal(ciphertext);

      return new String(plaintext, StandardCharsets.UTF_8);

    } catch (GeneralSecurityException e) {
      throw new IllegalStateException(
          "Failed to decrypt message", e);
    }
  }

  // =========================
  // Binary encryption
  // =========================

  @Override
  public byte[] encrypt(byte[] plaintext) {

    if (plaintext == null) {
      throw new IllegalArgumentException(
          "Plaintext must not be null");
    }

    byte[] iv = generateIv();

    try {
      Cipher cipher = Cipher.getInstance(TRANSFORMATION);

      cipher.init(
          Cipher.ENCRYPT_MODE,
          secretKey,
          new GCMParameterSpec(TAG_SIZE_BITS, iv)
      );

      byte[] ciphertext = cipher.doFinal(plaintext);

      ByteBuffer buffer = ByteBuffer.allocate(
          1 + IV_SIZE_BYTES + ciphertext.length
      );

      buffer.put(BINARY_FORMAT_VERSION);
      buffer.put(iv);
      buffer.put(ciphertext);

      return buffer.array();

    } catch (GeneralSecurityException e) {
      throw new IllegalStateException(
          "Failed to encrypt file", e);
    }
  }

  @Override
  public byte[] decrypt(byte[] encryptedData) {

    if (encryptedData == null) {
      throw new IllegalArgumentException(
          "Encrypted data must not be null");
    }

    int minimumLength = 1 + IV_SIZE_BYTES + TAG_SIZE_BYTES;

    if (encryptedData.length < minimumLength) {
      throw new IllegalArgumentException(
          "Invalid encrypted file format");
    }

    ByteBuffer buffer = ByteBuffer.wrap(encryptedData);

    byte version = buffer.get();

    if (version != BINARY_FORMAT_VERSION) {
      throw new IllegalArgumentException(
          "Unsupported encrypted file version: " + version);
    }

    byte[] iv = new byte[IV_SIZE_BYTES];
    buffer.get(iv);

    byte[] ciphertext = new byte[buffer.remaining()];
    buffer.get(ciphertext);

    try {
      Cipher cipher = Cipher.getInstance(TRANSFORMATION);

      cipher.init(
          Cipher.DECRYPT_MODE,
          secretKey,
          new GCMParameterSpec(TAG_SIZE_BITS, iv)
      );

      return cipher.doFinal(ciphertext);

    } catch (GeneralSecurityException e) {
      throw new IllegalStateException(
          "Failed to decrypt file", e);
    }
  }

  private byte[] generateIv() {

    byte[] iv = new byte[IV_SIZE_BYTES];

    secureRandom.nextBytes(iv);

    return iv;
  }
}
package com.jeannimi.messenger.adapter.out.encryption;

import com.jeannimi.messenger.application.port.out.FileEncryptionPort;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class AesGcmFileEncryptionAdapter implements FileEncryptionPort {

  private static final String TRANSFORMATION = "AES/GCM/NoPadding";

  private static final byte FORMAT_VERSION = 1;

  private static final int IV_SIZE_BYTES = 12;
  private static final int TAG_SIZE_BITS = 128;
  private static final int TAG_SIZE_BYTES = TAG_SIZE_BITS / 8;

  private static final int HEADER_SIZE = 1 + 1 + IV_SIZE_BYTES;

  private final AesGcmKeyRing keyRing;
  private final SecureRandom secureRandom;

  public AesGcmFileEncryptionAdapter(@Qualifier("fileKeyRing") AesGcmKeyRing keyRing) {
    this.keyRing = keyRing;
    this.secureRandom = new SecureRandom();
  }

  @Override
  public byte[] encrypt(byte[] plaintext) {

    if (plaintext == null) {
      throw new IllegalArgumentException("Plaintext must not be null");
    }

    int keyId = keyRing.activeKeyId();
    SecretKeySpec key = keyRing.activeKey();

    byte[] iv = generateIv();

    try {
      Cipher cipher = Cipher.getInstance(TRANSFORMATION);

      cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_SIZE_BITS, iv));

      cipher.updateAAD(createAad(keyId));

      byte[] ciphertext = cipher.doFinal(plaintext);

      ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE + ciphertext.length);

      buffer.put(FORMAT_VERSION);
      buffer.put((byte) keyId);
      buffer.put(iv);
      buffer.put(ciphertext);

      return buffer.array();

    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("Failed to encrypt file", e);
    }
  }

  @Override
  public byte[] decrypt(byte[] encryptedData) {

    if (encryptedData == null) {
      throw new IllegalArgumentException("Encrypted data must not be null");
    }

    int minimumLength = HEADER_SIZE + TAG_SIZE_BYTES;

    if (encryptedData.length < minimumLength) {
      throw new IllegalArgumentException("Invalid encrypted file format");
    }

    ByteBuffer buffer = ByteBuffer.wrap(encryptedData);

    byte formatVersion = buffer.get();

    if (formatVersion != FORMAT_VERSION) {
      throw new IllegalArgumentException(
          "Unsupported encrypted file format version: " + formatVersion);
    }

    int keyId = Byte.toUnsignedInt(buffer.get());

    SecretKeySpec key = keyRing.getKey(keyId);

    byte[] iv = new byte[IV_SIZE_BYTES];
    buffer.get(iv);

    byte[] ciphertext = new byte[buffer.remaining()];
    buffer.get(ciphertext);

    try {
      Cipher cipher = Cipher.getInstance(TRANSFORMATION);

      cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_SIZE_BITS, iv));

      cipher.updateAAD(createAad(keyId));

      return cipher.doFinal(ciphertext);

    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("Failed to decrypt file", e);
    }
  }

  private byte[] createAad(int keyId) {
    return new byte[] {FORMAT_VERSION, (byte) keyId};
  }

  private byte[] generateIv() {
    byte[] iv = new byte[IV_SIZE_BYTES];
    secureRandom.nextBytes(iv);
    return iv;
  }
}

package com.jeannimi.messenger.adapter.out.encryption;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AesGcmFileEncryptionAdapterTest {

  private static final int KEY_ID = 1;

  private static final byte FORMAT_VERSION = 1;

  private static final int HEADER_SIZE = 14;

  private static final int TAG_SIZE_BYTES = 16;

  private static final SecretKeySpec KEY = new SecretKeySpec(new byte[32], "AES");

  @Mock private AesGcmKeyRing keyRing;

  private AesGcmFileEncryptionAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new AesGcmFileEncryptionAdapter(keyRing);

    lenient().when(keyRing.activeKeyId()).thenReturn(KEY_ID);
    lenient().when(keyRing.activeKey()).thenReturn(KEY);
    lenient().when(keyRing.getKey(KEY_ID)).thenReturn(KEY);
  }

  @Test
  void encrypt_shouldReturnEncryptedData_whenPlaintextIsValid() {

    byte[] plaintext = "Hello, messenger!".getBytes();

    byte[] encrypted = adapter.encrypt(plaintext);

    assertThat(encrypted).isNotNull().isNotEqualTo(plaintext);

    assertThat(encrypted.length).isEqualTo(HEADER_SIZE + plaintext.length + TAG_SIZE_BYTES);
  }

  @Test
  void decrypt_shouldReturnOriginalPlaintext_whenDataWasEncrypted() {

    byte[] plaintext = "Hello, messenger!".getBytes();

    byte[] encrypted = adapter.encrypt(plaintext);

    byte[] decrypted = adapter.decrypt(encrypted);

    assertThat(decrypted).isEqualTo(plaintext);
  }

  @Test
  void encrypt_shouldSupportEmptyPlaintext() {

    byte[] plaintext = new byte[0];

    byte[] encrypted = adapter.encrypt(plaintext);

    assertThat(encrypted).hasSize(HEADER_SIZE + TAG_SIZE_BYTES);

    assertThat(adapter.decrypt(encrypted)).isEmpty();
  }

  @Test
  void encrypt_shouldGenerateDifferentCiphertext_whenSamePlaintextIsEncryptedTwice() {

    byte[] plaintext = "Same content".getBytes();

    byte[] first = adapter.encrypt(plaintext);
    byte[] second = adapter.encrypt(plaintext);

    assertThat(first).isNotEqualTo(second);

    assertThat(adapter.decrypt(first)).isEqualTo(plaintext);
    assertThat(adapter.decrypt(second)).isEqualTo(plaintext);
  }

  @Test
  void encrypt_shouldThrowIllegalArgumentException_whenPlaintextIsNull() {

    assertThatThrownBy(() -> adapter.encrypt(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Plaintext must not be null");
  }

  @Test
  void decrypt_shouldThrowIllegalArgumentException_whenEncryptedDataIsNull() {

    assertThatThrownBy(() -> adapter.decrypt(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Encrypted data must not be null");
  }

  @Test
  void decrypt_shouldThrowIllegalArgumentException_whenEncryptedDataIsTooShort() {

    byte[] encrypted = new byte[HEADER_SIZE + TAG_SIZE_BYTES - 1];

    assertThatThrownBy(() -> adapter.decrypt(encrypted))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid encrypted file format");
  }

  @Test
  void encrypt_shouldWriteCorrectHeader_whenPlaintextIsValid() {

    byte[] plaintext = "Test".getBytes();

    byte[] encrypted = adapter.encrypt(plaintext);

    ByteBuffer buffer = ByteBuffer.wrap(encrypted);

    byte formatVersion = buffer.get();
    int keyId = Byte.toUnsignedInt(buffer.get());

    assertThat(formatVersion).isEqualTo(FORMAT_VERSION);
    assertThat(keyId).isEqualTo(KEY_ID);
  }

  @Test
  void decrypt_shouldThrowIllegalArgumentException_whenFormatVersionIsUnsupported() {

    byte[] encrypted = adapter.encrypt("Test".getBytes());

    encrypted[0] = 2;

    assertThatThrownBy(() -> adapter.decrypt(encrypted))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Unsupported encrypted file format version: 2");
  }

  @Test
  void decrypt_shouldThrowIllegalArgumentException_whenKeyIdIsUnknown() {

    byte[] encrypted = adapter.encrypt("Test".getBytes());

    encrypted[1] = 2;

    when(keyRing.getKey(2)).thenThrow(new IllegalArgumentException("Unknown key ID: 2"));

    assertThatThrownBy(() -> adapter.decrypt(encrypted))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Unknown key ID: 2");
  }

  @Test
  void decrypt_shouldThrowIllegalStateException_whenCiphertextIsCorrupted() {

    byte[] encrypted = adapter.encrypt("Test".getBytes());

    encrypted[HEADER_SIZE] ^= 1;

    assertThatThrownBy(() -> adapter.decrypt(encrypted))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Failed to decrypt file")
        .hasCauseInstanceOf(javax.crypto.AEADBadTagException.class);
  }

  @Test
  void decrypt_shouldThrowIllegalStateException_whenIvIsModified() {

    byte[] encrypted = adapter.encrypt("Test".getBytes());

    encrypted[2] ^= 1;

    assertThatThrownBy(() -> adapter.decrypt(encrypted))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Failed to decrypt file")
        .hasCauseInstanceOf(javax.crypto.AEADBadTagException.class);
  }

  @Test
  void decrypt_shouldThrowIllegalStateException_whenAuthenticationTagIsModified() {

    byte[] encrypted = adapter.encrypt("Test".getBytes());

    encrypted[encrypted.length - 1] ^= 1;

    assertThatThrownBy(() -> adapter.decrypt(encrypted))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Failed to decrypt file")
        .hasCauseInstanceOf(javax.crypto.AEADBadTagException.class);
  }

  @Test
  void encrypt_shouldUseActiveKey_whenEncryptingData() {

    byte[] plaintext = "Test".getBytes();

    adapter.encrypt(plaintext);

    verify(keyRing).activeKeyId();
    verify(keyRing).activeKey();
  }

  @Test
  void decrypt_shouldUseKeyFromKeyRing_whenDecryptingData() {

    byte[] plaintext = "Test".getBytes();

    byte[] encrypted = adapter.encrypt(plaintext);

    adapter.decrypt(encrypted);

    verify(keyRing).getKey(KEY_ID);
  }
}

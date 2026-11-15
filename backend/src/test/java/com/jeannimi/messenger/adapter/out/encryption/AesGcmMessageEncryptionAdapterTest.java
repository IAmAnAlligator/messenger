package com.jeannimi.messenger.adapter.out.encryption;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Base64;
import javax.crypto.AEADBadTagException;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AesGcmMessageEncryptionAdapterTest {

  private static final int KEY_ID = 1;

  private static final int IV_SIZE_BYTES = 12;

  private static final int TAG_SIZE_BYTES = 16;

  private static final String FORMAT_VERSION = "v1";

  private static final SecretKeySpec KEY = new SecretKeySpec(new byte[32], "AES");

  @Mock private AesGcmKeyRing keyRing;

  private AesGcmMessageEncryptionAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new AesGcmMessageEncryptionAdapter(keyRing);

    lenient().when(keyRing.activeKeyId()).thenReturn(KEY_ID);
    lenient().when(keyRing.activeKey()).thenReturn(KEY);
    lenient().when(keyRing.getKey(KEY_ID)).thenReturn(KEY);
  }

  // ---------------------------------------------------------------------------
  // encrypt()
  // ---------------------------------------------------------------------------

  @Test
  void encrypt_shouldReturnEncryptedText_whenPlaintextIsValid() {

    String plaintext = "Hello, messenger!";

    String encrypted = adapter.encrypt(plaintext);

    assertThat(encrypted).isNotNull().isNotEqualTo(plaintext);

    assertThat(encrypted.split(":", -1)).hasSize(4);

    assertThat(encrypted).startsWith(FORMAT_VERSION + ":");
  }

  @Test
  void encrypt_shouldSupportEmptyPlaintext() {

    String encrypted = adapter.encrypt("");

    assertThat(encrypted).isNotBlank();

    assertThat(adapter.decrypt(encrypted)).isEmpty();
  }

  @Test
  void encrypt_shouldSupportUnicodePlaintext() {

    String plaintext = "Привет, мир! 你好 🌍";

    String encrypted = adapter.encrypt(plaintext);

    assertThat(adapter.decrypt(encrypted)).isEqualTo(plaintext);
  }

  @Test
  void encrypt_shouldGenerateDifferentCiphertext_whenSamePlaintextIsEncryptedTwice() {

    String plaintext = "Same message";

    String first = adapter.encrypt(plaintext);
    String second = adapter.encrypt(plaintext);

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
  void encrypt_shouldUseActiveKey_whenEncryptingMessage() {

    adapter.encrypt("Test");

    verify(keyRing).activeKeyId();
    verify(keyRing).activeKey();
  }

  @Test
  void encrypt_shouldWriteCorrectFormatVersion_whenEncryptingMessage() {

    String encrypted = adapter.encrypt("Test");

    String[] parts = encrypted.split(":", -1);

    assertThat(parts[0]).isEqualTo(FORMAT_VERSION);
  }

  @Test
  void encrypt_shouldWriteCorrectKeyId_whenEncryptingMessage() {

    String encrypted = adapter.encrypt("Test");

    String[] parts = encrypted.split(":", -1);

    assertThat(parts[1]).isEqualTo(String.valueOf(KEY_ID));
  }

  @Test
  void encrypt_shouldGenerateValidIv_whenEncryptingMessage() {

    String encrypted = adapter.encrypt("Test");

    String[] parts = encrypted.split(":", -1);

    byte[] iv = Base64.getDecoder().decode(parts[2]);

    assertThat(iv).hasSize(IV_SIZE_BYTES);
  }

  @Test
  void encrypt_shouldGenerateValidCiphertext_whenEncryptingMessage() {

    String plaintext = "Test";

    String encrypted = adapter.encrypt(plaintext);

    String[] parts = encrypted.split(":", -1);

    byte[] ciphertext = Base64.getDecoder().decode(parts[3]);

    assertThat(ciphertext)
        .hasSize(
            plaintext.getBytes(java.nio.charset.StandardCharsets.UTF_8).length + TAG_SIZE_BYTES);
  }

  // ---------------------------------------------------------------------------
  // decrypt()
  // ---------------------------------------------------------------------------

  @Test
  void decrypt_shouldReturnOriginalPlaintext_whenMessageWasEncrypted() {

    String plaintext = "Hello, messenger!";

    String encrypted = adapter.encrypt(plaintext);

    String decrypted = adapter.decrypt(encrypted);

    assertThat(decrypted).isEqualTo(plaintext);
  }

  @Test
  void decrypt_shouldReturnEmptyString_whenEncryptedPlaintextIsEmpty() {

    String encrypted = adapter.encrypt("");

    assertThat(adapter.decrypt(encrypted)).isEmpty();
  }

  @Test
  void decrypt_shouldPreserveUnicodeCharacters_whenMessageWasEncrypted() {

    String plaintext = "Тестирование AES-GCM: привет 🌍";

    String encrypted = adapter.encrypt(plaintext);

    assertThat(adapter.decrypt(encrypted)).isEqualTo(plaintext);
  }

  @Test
  void decrypt_shouldThrowIllegalArgumentException_whenEncryptedTextIsNull() {

    assertThatThrownBy(() -> adapter.decrypt(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Encrypted text must not be null");
  }

  @Test
  void decrypt_shouldThrowIllegalArgumentException_whenFormatHasWrongNumberOfParts() {

    assertThatThrownBy(() -> adapter.decrypt("v1:1:abc"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid encrypted message format");
  }

  @Test
  void decrypt_shouldThrowIllegalArgumentException_whenFormatVersionIsUnsupported() {

    assertThatThrownBy(() -> adapter.decrypt("v2:1:abc:def"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid encrypted message format");
  }

  @Test
  void decrypt_shouldThrowIllegalArgumentException_whenFormatVersionIsMissing() {

    assertThatThrownBy(() -> adapter.decrypt(":1:abc:def"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid encrypted message format");
  }

  @Test
  void decrypt_shouldThrowIllegalArgumentException_whenKeyIdIsNotNumeric() {

    assertThatThrownBy(() -> adapter.decrypt("v1:abc:abc:def"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid encryption key ID")
        .hasCauseInstanceOf(NumberFormatException.class);
  }

  @Test
  void decrypt_shouldThrowIllegalArgumentException_whenKeyIdIsUnknown() {

    when(keyRing.getKey(2)).thenThrow(new IllegalArgumentException("Unknown encryption key ID: 2"));

    assertThatThrownBy(() -> adapter.decrypt("v1:2:abc:def"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Unknown encryption key ID: 2");
  }

  @Test
  void decrypt_shouldUseKeyFromKeyRing_whenDecryptingMessage() {

    String encrypted = adapter.encrypt("Test");

    adapter.decrypt(encrypted);

    verify(keyRing).getKey(KEY_ID);
  }

  // ---------------------------------------------------------------------------
  // Base64 validation
  // ---------------------------------------------------------------------------

  @Test
  void decrypt_shouldThrowIllegalArgumentException_whenIvContainsInvalidBase64() {

    assertThatThrownBy(() -> adapter.decrypt("v1:1:invalid!:abc"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid Base64 in encrypted message");
  }

  @Test
  void decrypt_shouldThrowIllegalArgumentException_whenCiphertextContainsInvalidBase64() {

    String validIv = Base64.getEncoder().encodeToString(new byte[IV_SIZE_BYTES]);

    assertThatThrownBy(() -> adapter.decrypt("v1:1:" + validIv + ":invalid!"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid Base64 in encrypted message");
  }

  // ---------------------------------------------------------------------------
  // IV validation
  // ---------------------------------------------------------------------------

  @Test
  void decrypt_shouldThrowIllegalArgumentException_whenIvIsTooShort() {

    String shortIv = Base64.getEncoder().encodeToString(new byte[IV_SIZE_BYTES - 1]);

    String ciphertext = Base64.getEncoder().encodeToString(new byte[TAG_SIZE_BYTES]);

    assertThatThrownBy(() -> adapter.decrypt("v1:1:" + shortIv + ":" + ciphertext))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid AES-GCM IV length");
  }

  @Test
  void decrypt_shouldThrowIllegalArgumentException_whenIvIsTooLong() {

    String longIv = Base64.getEncoder().encodeToString(new byte[IV_SIZE_BYTES + 1]);

    String ciphertext = Base64.getEncoder().encodeToString(new byte[TAG_SIZE_BYTES]);

    assertThatThrownBy(() -> adapter.decrypt("v1:1:" + longIv + ":" + ciphertext))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid AES-GCM IV length");
  }

  // ---------------------------------------------------------------------------
  // Ciphertext validation
  // ---------------------------------------------------------------------------

  @Test
  void decrypt_shouldThrowIllegalArgumentException_whenCiphertextIsTooShort() {

    String validIv = Base64.getEncoder().encodeToString(new byte[IV_SIZE_BYTES]);

    String shortCiphertext = Base64.getEncoder().encodeToString(new byte[TAG_SIZE_BYTES - 1]);

    assertThatThrownBy(() -> adapter.decrypt("v1:1:" + validIv + ":" + shortCiphertext))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid AES-GCM ciphertext length");
  }

  @Test
  void decrypt_shouldThrowIllegalArgumentException_whenCiphertextIsEmpty() {

    String validIv = Base64.getEncoder().encodeToString(new byte[IV_SIZE_BYTES]);

    assertThatThrownBy(() -> adapter.decrypt("v1:1:" + validIv + ":"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid AES-GCM ciphertext length");
  }

  // ---------------------------------------------------------------------------
  // AES-GCM authentication
  // ---------------------------------------------------------------------------

  @Test
  void decrypt_shouldThrowIllegalStateException_whenCiphertextIsCorrupted() {

    String encrypted = adapter.encrypt("Test");

    String[] parts = encrypted.split(":", -1);

    byte[] ciphertext = Base64.getDecoder().decode(parts[3]);

    ciphertext[0] ^= 1;

    String corrupted =
        parts[0]
            + ":"
            + parts[1]
            + ":"
            + parts[2]
            + ":"
            + Base64.getEncoder().encodeToString(ciphertext);

    assertThatThrownBy(() -> adapter.decrypt(corrupted))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Failed to decrypt message")
        .hasCauseInstanceOf(AEADBadTagException.class);
  }

  @Test
  void decrypt_shouldThrowIllegalStateException_whenIvIsModified() {

    String encrypted = adapter.encrypt("Test");

    String[] parts = encrypted.split(":", -1);

    byte[] iv = Base64.getDecoder().decode(parts[2]);

    iv[0] ^= 1;

    String corrupted =
        parts[0] + ":" + parts[1] + ":" + Base64.getEncoder().encodeToString(iv) + ":" + parts[3];

    assertThatThrownBy(() -> adapter.decrypt(corrupted))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Failed to decrypt message")
        .hasCauseInstanceOf(AEADBadTagException.class);
  }

  @Test
  void decrypt_shouldThrowIllegalStateException_whenAuthenticationTagIsModified() {

    String encrypted = adapter.encrypt("Test");

    String[] parts = encrypted.split(":", -1);

    byte[] ciphertext = Base64.getDecoder().decode(parts[3]);

    ciphertext[ciphertext.length - 1] ^= 1;

    String corrupted =
        parts[0]
            + ":"
            + parts[1]
            + ":"
            + parts[2]
            + ":"
            + Base64.getEncoder().encodeToString(ciphertext);

    assertThatThrownBy(() -> adapter.decrypt(corrupted))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Failed to decrypt message")
        .hasCauseInstanceOf(AEADBadTagException.class);
  }
}

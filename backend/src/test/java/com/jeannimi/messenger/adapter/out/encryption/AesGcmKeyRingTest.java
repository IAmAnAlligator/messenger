package com.jeannimi.messenger.adapter.out.encryption;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;

class AesGcmKeyRingTest {

  private static final int KEY_SIZE_BYTES = 32;

  private static final int ACTIVE_KEY_ID = 1;

  private static final byte[] VALID_KEY = new byte[KEY_SIZE_BYTES];

  private static final String VALID_BASE64_KEY = Base64.getEncoder().encodeToString(VALID_KEY);

  // ---------------------------------------------------------------------------
  // Constructor: configuration validation
  // ---------------------------------------------------------------------------

  @Test
  void constructor_shouldThrowIllegalStateException_whenPropertiesAreNull() {

    assertThatThrownBy(() -> new AesGcmKeyRing(null))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Encryption keys are not configured");
  }

  @Test
  void constructor_shouldThrowIllegalStateException_whenKeysAreNull() {

    EncryptionProperties.KeyRing properties = new EncryptionProperties.KeyRing(ACTIVE_KEY_ID, null);

    assertThatThrownBy(() -> new AesGcmKeyRing(properties))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Encryption keys are not configured");
  }

  @Test
  void constructor_shouldThrowIllegalStateException_whenKeysAreEmpty() {

    EncryptionProperties.KeyRing properties =
        new EncryptionProperties.KeyRing(ACTIVE_KEY_ID, Map.of());

    assertThatThrownBy(() -> new AesGcmKeyRing(properties))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Encryption keys are not configured");
  }

  // ---------------------------------------------------------------------------
  // Constructor: active key ID validation
  // ---------------------------------------------------------------------------

  @Test
  void constructor_shouldAcceptActiveKeyId_whenIdIsOne() {

    AesGcmKeyRing keyRing = createKeyRing(1);

    assertThat(keyRing.activeKeyId()).isEqualTo(1);
  }

  @Test
  void constructor_shouldAcceptActiveKeyId_whenIdIs255() {

    AesGcmKeyRing keyRing = createKeyRing(255);

    assertThat(keyRing.activeKeyId()).isEqualTo(255);
  }

  @Test
  void constructor_shouldThrowIllegalStateException_whenActiveKeyIdIsZero() {

    EncryptionProperties.KeyRing properties = createProperties(0, Map.of(0, VALID_BASE64_KEY));

    assertThatThrownBy(() -> new AesGcmKeyRing(properties))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Invalid active encryption key ID");
  }

  @Test
  void constructor_shouldThrowIllegalStateException_whenActiveKeyIdIs256() {

    EncryptionProperties.KeyRing properties = createProperties(256, Map.of(256, VALID_BASE64_KEY));

    assertThatThrownBy(() -> new AesGcmKeyRing(properties))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Invalid active encryption key ID");
  }

  // ---------------------------------------------------------------------------
  // Constructor: individual key ID validation
  // ---------------------------------------------------------------------------

  @Test
  void constructor_shouldThrowIllegalStateException_whenKeyIdIsZero() {

    EncryptionProperties.KeyRing properties =
        createProperties(ACTIVE_KEY_ID, Map.of(0, VALID_BASE64_KEY));

    assertThatThrownBy(() -> new AesGcmKeyRing(properties))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Invalid encryption key ID");
  }

  @Test
  void constructor_shouldThrowIllegalStateException_whenKeyIdIs256() {

    EncryptionProperties.KeyRing properties =
        createProperties(ACTIVE_KEY_ID, Map.of(256, VALID_BASE64_KEY));

    assertThatThrownBy(() -> new AesGcmKeyRing(properties))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Invalid encryption key ID");
  }

  // ---------------------------------------------------------------------------
  // Constructor: Base64 validation
  // ---------------------------------------------------------------------------

  @Test
  void constructor_shouldThrowIllegalStateException_whenBase64KeyIsNull() {

    Map<Integer, String> keys = new HashMap<>();
    keys.put(ACTIVE_KEY_ID, null);

    EncryptionProperties.KeyRing properties = createProperties(ACTIVE_KEY_ID, keys);

    assertThatThrownBy(() -> new AesGcmKeyRing(properties))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Encryption key must not be empty: 1");
  }

  @Test
  void constructor_shouldThrowIllegalStateException_whenBase64KeyIsEmpty() {

    EncryptionProperties.KeyRing properties =
        createProperties(ACTIVE_KEY_ID, Map.of(ACTIVE_KEY_ID, ""));

    assertThatThrownBy(() -> new AesGcmKeyRing(properties))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Encryption key must not be empty: 1");
  }

  @Test
  void constructor_shouldThrowIllegalStateException_whenBase64KeyIsBlank() {

    EncryptionProperties.KeyRing properties =
        createProperties(ACTIVE_KEY_ID, Map.of(ACTIVE_KEY_ID, "   "));

    assertThatThrownBy(() -> new AesGcmKeyRing(properties))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Encryption key must not be empty: 1");
  }

  @Test
  void constructor_shouldThrowIllegalStateException_whenBase64IsInvalid() {

    EncryptionProperties.KeyRing properties =
        createProperties(ACTIVE_KEY_ID, Map.of(ACTIVE_KEY_ID, "invalid-base64!"));

    assertThatThrownBy(() -> new AesGcmKeyRing(properties))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Invalid Base64 encryption key: 1")
        .hasCauseInstanceOf(IllegalArgumentException.class);
  }

  // ---------------------------------------------------------------------------
  // Constructor: AES key size validation
  // ---------------------------------------------------------------------------

  @Test
  void constructor_shouldThrowIllegalStateException_whenKeyContains16Bytes() {

    assertInvalidKeySize(16);
  }

  @Test
  void constructor_shouldThrowIllegalStateException_whenKeyContains24Bytes() {

    assertInvalidKeySize(24);
  }

  @Test
  void constructor_shouldThrowIllegalStateException_whenKeyContains31Bytes() {

    assertInvalidKeySize(31);
  }

  @Test
  void constructor_shouldThrowIllegalStateException_whenKeyContains33Bytes() {

    assertInvalidKeySize(33);
  }

  @Test
  void constructor_shouldAcceptKey_whenKeyContainsExactly32Bytes() {

    AesGcmKeyRing keyRing = createKeyRing(ACTIVE_KEY_ID);

    assertThat(keyRing.activeKey()).isInstanceOf(SecretKeySpec.class);

    assertThat(keyRing.activeKey().getAlgorithm()).isEqualTo("AES");

    assertThat(keyRing.activeKey().getEncoded()).containsExactly(VALID_KEY);
  }

  // ---------------------------------------------------------------------------
  // Constructor: active key existence
  // ---------------------------------------------------------------------------

  @Test
  void constructor_shouldThrowIllegalStateException_whenActiveKeyIsNotConfigured() {

    EncryptionProperties.KeyRing properties = createProperties(2, Map.of(1, VALID_BASE64_KEY));

    assertThatThrownBy(() -> new AesGcmKeyRing(properties))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Active encryption key is not configured");
  }

  // ---------------------------------------------------------------------------
  // activeKeyId()
  // ---------------------------------------------------------------------------

  @Test
  void activeKeyId_shouldReturnConfiguredActiveKeyId() {

    AesGcmKeyRing keyRing = createKeyRing(ACTIVE_KEY_ID);

    assertThat(keyRing.activeKeyId()).isEqualTo(ACTIVE_KEY_ID);
  }

  // ---------------------------------------------------------------------------
  // activeKey()
  // ---------------------------------------------------------------------------

  @Test
  void activeKey_shouldReturnActiveEncryptionKey() {

    AesGcmKeyRing keyRing = createKeyRing(ACTIVE_KEY_ID);

    assertThat(keyRing.activeKey().getEncoded()).containsExactly(VALID_KEY);
  }

  // ---------------------------------------------------------------------------
  // getKey()
  // ---------------------------------------------------------------------------

  @Test
  void getKey_shouldReturnKey_whenKeyIdExists() {

    byte[] firstKey = new byte[32];
    byte[] secondKey = new byte[32];

    secondKey[0] = 1;

    Map<Integer, String> keys =
        Map.of(
            1, Base64.getEncoder().encodeToString(firstKey),
            2, Base64.getEncoder().encodeToString(secondKey));

    AesGcmKeyRing keyRing = new AesGcmKeyRing(createProperties(1, keys));

    assertThat(keyRing.getKey(2).getEncoded()).containsExactly(secondKey);
  }

  @Test
  void getKey_shouldThrowIllegalArgumentException_whenKeyIdDoesNotExist() {

    AesGcmKeyRing keyRing = createKeyRing(ACTIVE_KEY_ID);

    assertThatThrownBy(() -> keyRing.getKey(2))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Unknown encryption key ID: 2");
  }

  @Test
  void getKey_shouldReturnKey_whenKeyIdIs255() {

    byte[] key = new byte[32];

    EncryptionProperties.KeyRing properties =
        createProperties(255, Map.of(255, Base64.getEncoder().encodeToString(key)));

    AesGcmKeyRing keyRing = new AesGcmKeyRing(properties);

    assertThat(keyRing.getKey(255).getEncoded()).containsExactly(key);
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  private static AesGcmKeyRing createKeyRing(int activeKeyId) {

    EncryptionProperties.KeyRing properties =
        createProperties(activeKeyId, Map.of(activeKeyId, VALID_BASE64_KEY));

    return new AesGcmKeyRing(properties);
  }

  private static EncryptionProperties.KeyRing createProperties(
      int activeKeyId, Map<Integer, String> keys) {

    return new EncryptionProperties.KeyRing(activeKeyId, keys);
  }

  private static void assertInvalidKeySize(int size) {

    byte[] invalidKey = new byte[size];

    EncryptionProperties.KeyRing properties =
        createProperties(
            ACTIVE_KEY_ID, Map.of(ACTIVE_KEY_ID, Base64.getEncoder().encodeToString(invalidKey)));

    assertThatThrownBy(() -> new AesGcmKeyRing(properties))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Encryption key must contain exactly 32 bytes");
  }
}

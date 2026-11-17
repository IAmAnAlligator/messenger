package com.jeannimi.messenger.adapter.out.encryption;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jeannimi.messenger.application.port.out.file.FileEncryptionPort;
import com.jeannimi.messenger.application.port.out.security.MessageEncryptionPort;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(
    classes = {
      EncryptionConfiguration.class,
      AesGcmMessageEncryptionAdapter.class,
      AesGcmFileEncryptionAdapter.class
    })
@TestPropertySource(
    properties = {
      "encryption.messages.active-key-id=1",
      "encryption.messages.keys[1]=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
      "encryption.messages.keys[2]=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
      "encryption.files.active-key-id=2",
      "encryption.files.keys[2]=AQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQE=",
      "encryption.files.keys[3]=AQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQE="
    })
class EncryptionIntegrationTest {

  @Autowired private MessageEncryptionPort messageEncryption;

  @Autowired private FileEncryptionPort fileEncryption;

  @Autowired
  @Qualifier("messageKeyRing")
  private AesGcmKeyRing messageKeyRing;

  @Autowired
  @Qualifier("fileKeyRing")
  private AesGcmKeyRing fileKeyRing;

  // ---------------------------------------------------------
  // Spring configuration
  // ---------------------------------------------------------

  @Test
  void contextLoads() {
    assertThat(messageEncryption).isNotNull();
    assertThat(fileEncryption).isNotNull();

    assertThat(messageKeyRing).isNotNull();
    assertThat(fileKeyRing).isNotNull();
  }

  @Test
  void shouldCreateSeparateKeyRingsForMessagesAndFiles() {
    assertThat(messageKeyRing.activeKeyId()).isEqualTo(1);
    assertThat(fileKeyRing.activeKeyId()).isEqualTo(2);

    assertThat(messageKeyRing.activeKey().getEncoded())
        .isNotEqualTo(fileKeyRing.activeKey().getEncoded());
  }

  // ---------------------------------------------------------
  // Message encryption
  // ---------------------------------------------------------

  @Test
  void shouldEncryptAndDecryptMessage() {
    String plaintext = "Hello, Messenger!";

    String encrypted = messageEncryption.encrypt(plaintext);
    String decrypted = messageEncryption.decrypt(encrypted);

    assertThat(encrypted).isNotEqualTo(plaintext);
    assertThat(decrypted).isEqualTo(plaintext);
  }

  @Test
  void shouldEncryptAndDecryptUnicodeMessage() {
    String plaintext = "Привет! 你好! 😀";

    String encrypted = messageEncryption.encrypt(plaintext);

    assertThat(messageEncryption.decrypt(encrypted)).isEqualTo(plaintext);
  }

  @Test
  void shouldEncryptAndDecryptEmptyMessage() {
    String plaintext = "";

    String encrypted = messageEncryption.encrypt(plaintext);

    assertThat(messageEncryption.decrypt(encrypted)).isEmpty();
  }

  @Test
  void shouldUseMessageKeyRing() {
    String encrypted = messageEncryption.encrypt("test");

    String[] parts = encrypted.split(":", -1);

    assertThat(parts).hasSize(4);
    assertThat(parts[0]).isEqualTo("v1");
    assertThat(parts[1]).isEqualTo("1");
  }

  @Test
  void shouldGenerateDifferentCiphertextForSameMessage() {
    String plaintext = "Same message";

    String first = messageEncryption.encrypt(plaintext);
    String second = messageEncryption.encrypt(plaintext);

    assertThat(first).isNotEqualTo(second);

    assertThat(messageEncryption.decrypt(first)).isEqualTo(plaintext);

    assertThat(messageEncryption.decrypt(second)).isEqualTo(plaintext);
  }

  // ---------------------------------------------------------
  // File encryption
  // ---------------------------------------------------------

  @Test
  void shouldEncryptAndDecryptFile() {
    byte[] plaintext = "Hello, encrypted file!".getBytes(StandardCharsets.UTF_8);

    byte[] encrypted = fileEncryption.encrypt(plaintext);
    byte[] decrypted = fileEncryption.decrypt(encrypted);

    assertThat(encrypted).isNotEqualTo(plaintext);
    assertThat(decrypted).isEqualTo(plaintext);
  }

  @Test
  void shouldEncryptAndDecryptBinaryFile() {
    byte[] plaintext = new byte[256];

    for (int i = 0; i < plaintext.length; i++) {
      plaintext[i] = (byte) i;
    }

    byte[] encrypted = fileEncryption.encrypt(plaintext);
    byte[] decrypted = fileEncryption.decrypt(encrypted);

    assertThat(decrypted).isEqualTo(plaintext);
  }

  @Test
  void shouldEncryptAndDecryptEmptyFile() {
    byte[] plaintext = new byte[0];

    byte[] encrypted = fileEncryption.encrypt(plaintext);
    byte[] decrypted = fileEncryption.decrypt(encrypted);

    assertThat(decrypted).isEmpty();
  }

  @Test
  void shouldUseFileKeyRing() {
    byte[] encrypted = fileEncryption.encrypt("test".getBytes(StandardCharsets.UTF_8));

    ByteBuffer buffer = ByteBuffer.wrap(encrypted);

    int formatVersion = Byte.toUnsignedInt(buffer.get());
    int keyId = Byte.toUnsignedInt(buffer.get());

    assertThat(formatVersion).isEqualTo(1);
    assertThat(keyId).isEqualTo(2);
  }

  @Test
  void shouldGenerateDifferentCiphertextForSameFile() {
    byte[] plaintext = "Same file".getBytes(StandardCharsets.UTF_8);

    byte[] first = fileEncryption.encrypt(plaintext);
    byte[] second = fileEncryption.encrypt(plaintext);

    assertThat(first).isNotEqualTo(second);

    assertThat(fileEncryption.decrypt(first)).isEqualTo(plaintext);
    assertThat(fileEncryption.decrypt(second)).isEqualTo(plaintext);
  }

  // ---------------------------------------------------------
  // Adapter interoperability
  // ---------------------------------------------------------

  @Test
  void shouldNotDecryptMessageAsFile() {
    String encryptedMessage = messageEncryption.encrypt("secret");

    byte[] encryptedData = encryptedMessage.getBytes(StandardCharsets.UTF_8);

    assertThatThrownBy(() -> fileEncryption.decrypt(encryptedData))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldNotDecryptFileAsMessage() {
    byte[] encryptedFile = fileEncryption.encrypt("secret".getBytes(StandardCharsets.UTF_8));

    String encryptedData = new String(encryptedFile, StandardCharsets.UTF_8);

    assertThatThrownBy(() -> messageEncryption.decrypt(encryptedData))
        .isInstanceOf(IllegalArgumentException.class);
  }

  // ---------------------------------------------------------
  // Key persistence between adapter instances
  // ---------------------------------------------------------

  @Test
  void shouldDecryptMessageWithNewAdapterInstance() {
    String plaintext = "Persistent encrypted message";

    String encrypted = messageEncryption.encrypt(plaintext);

    AesGcmMessageEncryptionAdapter anotherAdapter =
        new AesGcmMessageEncryptionAdapter(messageKeyRing);

    assertThat(anotherAdapter.decrypt(encrypted)).isEqualTo(plaintext);
  }

  @Test
  void shouldDecryptFileWithNewAdapterInstance() {
    byte[] plaintext = "Persistent encrypted file".getBytes(StandardCharsets.UTF_8);

    byte[] encrypted = fileEncryption.encrypt(plaintext);

    AesGcmFileEncryptionAdapter anotherAdapter = new AesGcmFileEncryptionAdapter(fileKeyRing);

    assertThat(anotherAdapter.decrypt(encrypted)).isEqualTo(plaintext);
  }

  @Test
  void shouldRejectMessageWhenKeyIdIsTampered() {
    String plaintext = "Secret message";

    String encrypted = messageEncryption.encrypt(plaintext);

    String[] parts = encrypted.split(":", -1);

    // Подменяем Key ID с 1 на 2.
    // Оба ID существуют и используют одинаковый AES-ключ.
    parts[1] = "2";

    String tampered = String.join(":", parts);

    assertThatThrownBy(() -> messageEncryption.decrypt(tampered))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Failed to decrypt message");
  }
}

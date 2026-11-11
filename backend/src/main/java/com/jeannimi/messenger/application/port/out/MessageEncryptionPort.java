package com.jeannimi.messenger.application.port.out;

public interface MessageEncryptionPort {

  String encrypt(String plaintext);

  String decrypt(String ciphertext);

  byte[] encrypt(byte[] plaintext);

  byte[] decrypt(byte[] encryptedData);
}

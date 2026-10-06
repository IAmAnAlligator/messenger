package com.jeannimi.messenger.application.port.out;

public interface MessageEncryptionPort {

  String encrypt(String plaintext);

  String decrypt(String ciphertext);
}
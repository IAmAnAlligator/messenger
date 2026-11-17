package com.jeannimi.messenger.application.port.out.security;

public interface MessageEncryptionPort {

  String encrypt(String plaintext);

  String decrypt(String encryptedText);
}

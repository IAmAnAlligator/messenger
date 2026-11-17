package com.jeannimi.messenger.application.port.out.file;

public interface FileEncryptionPort {

  byte[] encrypt(byte[] plaintext);

  byte[] decrypt(byte[] encryptedData);
}

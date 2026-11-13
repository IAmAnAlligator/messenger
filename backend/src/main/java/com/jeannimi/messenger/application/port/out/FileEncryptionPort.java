package com.jeannimi.messenger.application.port.out;

public interface FileEncryptionPort {

  byte[] encrypt(byte[] plaintext);

  byte[] decrypt(byte[] encryptedData);

}

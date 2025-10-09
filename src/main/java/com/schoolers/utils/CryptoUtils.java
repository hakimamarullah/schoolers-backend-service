package com.schoolers.utils;

public interface CryptoUtils {

    String encrypt(String plainText);

    String decrypt(String cipherTextBase64);
}

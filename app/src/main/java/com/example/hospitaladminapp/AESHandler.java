package com.example.hospitaladminapp;
import android.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AESHandler {
    private static final String algorithm = "AES";//algorithm to use

    // **************************************************************
    private static final String KEY = "42b69a04f7e593fcd201a3d8f7c65e21";
// THIS IS THE KEY BEING USED AS AN EXAMPLE AND WOULD NEVER NORMALLY BE STORED LIKE THIS.

    // **************************************************************


    //encryption algorithm
    public static String encrypt(String data) throws Exception {
        Cipher cipher = Cipher.getInstance(algorithm);
        SecretKeySpec secretKey = new SecretKeySpec(KEY.getBytes(), algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
    }

    //decryption algorithm
    public static String decrypt(String encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance(algorithm);
        SecretKeySpec secretKey = new SecretKeySpec(KEY.getBytes(), algorithm);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decodedBytes = Base64.decode(encryptedData, Base64.DEFAULT);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes);
    }
}
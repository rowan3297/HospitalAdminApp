package com.example.hospitaladminapp;

import java.security.PrivateKey;
import java.util.Base64;

import javax.crypto.Cipher;

public class RSADecryption {
    // Method to decrypt RSA-encrypted text
    public String decryptRSA(String encryptedText, PrivateKey privateKey) throws
            Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey); // Initialise cipher for decryption
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedText); // Decode from Base64
        byte[] decryptedBytes = cipher.doFinal(decodedBytes); // Decrypt the text
        return new String(decryptedBytes); // Convert decrypted bytes back to a string
    }
}


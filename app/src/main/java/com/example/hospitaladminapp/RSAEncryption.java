package com.example.hospitaladminapp;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.util.Base64;

import javax.crypto.Cipher;

public class RSAEncryption {
    // Method to generate an RSA key pair (public and private keys)
    public KeyPair generateRSAKeyPair() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048); // RSA key size (2048 bits)
        return keyPairGenerator.generateKeyPair();
    }
    // Method to encrypt plain text using RSA
    public String encryptRSA(String plaintext, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey); // Initialise cipher for encryption
        byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes()); // Encrypt the text
        return Base64.getEncoder().encodeToString(encryptedBytes); // Convert to Base64 and return as string
    }
}

package com.cpf.security.session.jdbc;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/** AES-256-GCM Credential 암복호화기입니다. Handle을 AAD로 사용해 행 간 Ciphertext 이동을 차단합니다. */
final class CpfCredentialCipher {
    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;
    private static final SecureRandom RANDOM = new SecureRandom();
    private final SecretKey key;

    CpfCredentialCipher(byte[] keyBytes) {
        if (keyBytes == null || keyBytes.length != 32) {
            throw new IllegalArgumentException("CPF BFF credential key must be exactly 256 bits.");
        }
        this.key = new SecretKeySpec(Arrays.copyOf(keyBytes, keyBytes.length), "AES");
    }

    Encrypted encrypt(String handle, String value) {
        if (value == null) return null;
        byte[] iv = new byte[IV_BYTES];
        RANDOM.nextBytes(iv);
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            cipher.updateAAD(handle.getBytes(StandardCharsets.UTF_8));
            return new Encrypted(iv, cipher.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException failure) {
            throw new IllegalStateException("CPF_BFF_CREDENTIAL_ENCRYPT_FAILED", failure);
        }
    }

    String decrypt(String handle, byte[] iv, byte[] ciphertext) {
        if (ciphertext == null) return null;
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            cipher.updateAAD(handle.getBytes(StandardCharsets.UTF_8));
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException failure) {
            throw new SecurityException("CPF_BFF_CREDENTIAL_INTEGRITY_FAILED", failure);
        }
    }

    record Encrypted(byte[] iv, byte[] ciphertext) {
        Encrypted {
            iv = Arrays.copyOf(iv, iv.length);
            ciphertext = Arrays.copyOf(ciphertext, ciphertext.length);
        }
        @Override public byte[] iv() { return Arrays.copyOf(iv, iv.length); }
        @Override public byte[] ciphertext() { return Arrays.copyOf(ciphertext, ciphertext.length); }
    }
}

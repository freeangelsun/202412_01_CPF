package com.cpf.batch.runtime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/** Center-Cut immutable parameter snapshot AES-256-GCM protector. Key는 외부 Secret Provider/환경에서만 공급합니다. */
@Component
public final class CenterCutParameterProtector {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final byte[] key;

    public CenterCutParameterProtector(@Value("${cpf.center-cut.parameter-key-base64:${CPF_CENTER_CUT_PARAMETER_KEY:}}") String keyBase64) {
        this.key = keyBase64 == null || keyBase64.isBlank() ? null : Base64.getDecoder().decode(keyBase64.trim());
        if (this.key != null && this.key.length != 32) throw new IllegalArgumentException("Center-Cut parameter key must be 256-bit Base64");
    }

    public ProtectedPayload protect(String plainText) {
        requireKey();
        try {
            byte[] iv=new byte[12];RANDOM.nextBytes(iv);
            Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE,new SecretKeySpec(key,"AES"),new GCMParameterSpec(128,iv));
            byte[] encrypted=cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return new ProtectedPayload(Base64.getEncoder().encodeToString(iv)+":"+Base64.getEncoder().encodeToString(encrypted),sha256(plainText));
        } catch (Exception e) { throw new IllegalStateException("Center-Cut parameter encryption failed",e); }
    }

    public String unprotect(String value) {
        requireKey();
        try {
            String[] parts=value.split(":",2); if(parts.length!=2)throw new IllegalArgumentException("Invalid encrypted payload");
            Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE,new SecretKeySpec(key,"AES"),new GCMParameterSpec(128,Base64.getDecoder().decode(parts[0])));
            return new String(cipher.doFinal(Base64.getDecoder().decode(parts[1])),StandardCharsets.UTF_8);
        } catch (Exception e) { throw new IllegalStateException("Center-Cut parameter decryption failed",e); }
    }

    public String sha256(String text) {
        try { return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8))); }
        catch (Exception e) { throw new IllegalStateException(e); }
    }
    private void requireKey(){if(key==null)throw new IllegalStateException("CPF_CENTER_CUT_PARAMETER_KEY is required for Center-Cut execution");}
    public record ProtectedPayload(String cipherText,String sha256) {}
}

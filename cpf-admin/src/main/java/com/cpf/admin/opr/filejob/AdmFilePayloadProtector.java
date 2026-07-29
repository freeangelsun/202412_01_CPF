package com.cpf.admin.opr.filejob;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/** File Job 행 Payload를 AES-256-GCM으로 암호화하며 Key 원문을 설정 파일에 두지 않습니다. */
@Component
public final class AdmFilePayloadProtector {
    private static final String PREFIX = "enc:v1:";
    private static final SecureRandom RANDOM = new SecureRandom();
    private final String keyEnvironmentName;
    private final boolean allowLegacyPlaintextRead;

    public AdmFilePayloadProtector(
            @Value("${cpf.admin.file-job.payload-key-env:CPF_ADM_FILE_JOB_PAYLOAD_KEY}") String keyEnvironmentName,
            @Value("${cpf.admin.file-job.allow-legacy-plaintext-read:false}") boolean allowLegacyPlaintextRead) {
        if (keyEnvironmentName == null || !keyEnvironmentName.matches("[A-Z][A-Z0-9_]{2,100}")) {
            throw new IllegalArgumentException("File Job payload key 환경변수 이름이 올바르지 않습니다.");
        }
        this.keyEnvironmentName = keyEnvironmentName;
        this.allowLegacyPlaintextRead = allowLegacyPlaintextRead;
    }

    public String protect(String plaintext) {
        if (plaintext == null) throw new IllegalArgumentException("암호화 Payload는 null일 수 없습니다.");
        try {
            byte[] nonce = new byte[12];
            RANDOM.nextBytes(nonce);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key(), new GCMParameterSpec(128, nonce));
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] joined = new byte[nonce.length + encrypted.length];
            System.arraycopy(nonce, 0, joined, 0, nonce.length);
            System.arraycopy(encrypted, 0, joined, nonce.length, encrypted.length);
            return PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(joined);
        } catch (RuntimeException error) { throw error; }
        catch (Exception error) { throw new IllegalStateException("File Job Payload 암호화에 실패했습니다.", error); }
    }

    public String unprotect(String protectedValue) {
        if (protectedValue == null) throw new IllegalArgumentException("복호화 Payload는 null일 수 없습니다.");
        if (!protectedValue.startsWith(PREFIX)) {
            if (allowLegacyPlaintextRead) return protectedValue;
            throw new IllegalStateException("암호화되지 않은 Legacy File Job Payload를 읽을 수 없습니다.");
        }
        try {
            byte[] joined = Base64.getUrlDecoder().decode(protectedValue.substring(PREFIX.length()));
            if (joined.length < 12 + 16) throw new IllegalArgumentException("암호화 Payload 길이가 올바르지 않습니다.");
            byte[] nonce = java.util.Arrays.copyOfRange(joined, 0, 12);
            byte[] encrypted = java.util.Arrays.copyOfRange(joined, 12, joined.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key(), new GCMParameterSpec(128, nonce));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (RuntimeException error) { throw error; }
        catch (Exception error) { throw new IllegalStateException("File Job Payload 복호화에 실패했습니다.", error); }
    }

    private SecretKeySpec key() {
        String encoded = System.getenv(keyEnvironmentName);
        if (encoded == null || encoded.isBlank()) {
            throw new IllegalStateException("File Job Payload 암호화 Key 환경변수가 없습니다: " + keyEnvironmentName);
        }
        byte[] decoded;
        try { decoded = Base64.getDecoder().decode(encoded.trim()); }
        catch (IllegalArgumentException error) { throw new IllegalStateException("File Job Payload Key는 Base64여야 합니다.", error); }
        if (decoded.length != 32) throw new IllegalStateException("File Job Payload Key는 AES-256 32byte여야 합니다.");
        return new SecretKeySpec(decoded, "AES");
    }
}

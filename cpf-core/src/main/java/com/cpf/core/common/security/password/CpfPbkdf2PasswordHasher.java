package com.cpf.core.common.security.password;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * PBKDF2-HMAC-SHA256 기반 비밀번호 해시 구현입니다.
 *
 * <p>저장값에는 알고리즘, 형식 버전, 반복 횟수, 키 길이와 개별 salt를 포함합니다.
 * 기존 {@code PBKDF2$...} 형식도 검증하되 로그인 성공 후 최신 형식으로 교체하도록
 * 재해시 필요 상태를 반환합니다.</p>
 */
public final class CpfPbkdf2PasswordHasher implements CpfPasswordHashingPort {
    public static final String ALGORITHM_ID = "pbkdf2-sha256";
    public static final int FORMAT_VERSION = 1;
    public static final int DEFAULT_ITERATIONS = 310_000;
    public static final int DEFAULT_KEY_BITS = 256;
    private static final int SALT_BYTES = 16;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final int iterations;
    private final int keyBits;
    private final char[] pepper;

    public CpfPbkdf2PasswordHasher(int iterations, int keyBits, char[] pepper) {
        if (iterations < 210_000) {
            throw new IllegalArgumentException("PBKDF2 반복 횟수는 210000 이상이어야 합니다.");
        }
        if (keyBits < 256) {
            throw new IllegalArgumentException("PBKDF2 키 길이는 256비트 이상이어야 합니다.");
        }
        this.iterations = iterations;
        this.keyBits = keyBits;
        this.pepper = pepper == null ? new char[0] : pepper.clone();
    }

    @Override
    public String hash(char[] rawPassword) {
        requirePassword(rawPassword);
        byte[] salt = new byte[SALT_BYTES];
        SECURE_RANDOM.nextBytes(salt);
        byte[] derived = derive(rawPassword, salt, iterations, keyBits);
        try {
            return "$cpf$" + ALGORITHM_ID + "$" + FORMAT_VERSION + "$" + iterations + "$" + keyBits
                    + "$" + Base64.getUrlEncoder().withoutPadding().encodeToString(salt)
                    + "$" + Base64.getUrlEncoder().withoutPadding().encodeToString(derived);
        } finally {
            Arrays.fill(derived, (byte) 0);
        }
    }

    @Override
    public CpfPasswordVerification verify(char[] rawPassword, String encodedPassword) {
        if (rawPassword == null || rawPassword.length == 0 || encodedPassword == null || encodedPassword.isBlank()) {
            return CpfPasswordVerification.rejected();
        }
        try {
            ParsedHash parsed = parse(encodedPassword);
            byte[] actual = derive(rawPassword, parsed.salt(), parsed.iterations(), parsed.keyBits());
            boolean matched;
            try {
                matched = MessageDigest.isEqual(parsed.hash(), actual);
            } finally {
                Arrays.fill(actual, (byte) 0);
            }
            return new CpfPasswordVerification(matched, matched && needsRehash(encodedPassword));
        } catch (RuntimeException ex) {
            return CpfPasswordVerification.rejected();
        }
    }

    @Override
    public boolean needsRehash(String encodedPassword) {
        try {
            ParsedHash parsed = parse(encodedPassword);
            return parsed.legacy()
                    || parsed.version() != FORMAT_VERSION
                    || parsed.iterations() < iterations
                    || parsed.keyBits() < keyBits;
        } catch (RuntimeException ex) {
            return true;
        }
    }

    @Override
    public String algorithmId() {
        return ALGORITHM_ID;
    }

    private ParsedHash parse(String encodedPassword) {
        if (encodedPassword.startsWith("PBKDF2$")) {
            String[] parts = encodedPassword.split("\\$");
            if (parts.length != 4) {
                throw new IllegalArgumentException("기존 PBKDF2 저장 형식이 올바르지 않습니다.");
            }
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] hash = Base64.getDecoder().decode(parts[3]);
            return new ParsedHash(0, Integer.parseInt(parts[1]), hash.length * 8, salt, hash, true);
        }

        String[] parts = encodedPassword.split("\\$");
        if (parts.length != 8 || !"cpf".equals(parts[1]) || !ALGORITHM_ID.equals(parts[2])) {
            throw new IllegalArgumentException("CPF 비밀번호 저장 형식이 올바르지 않습니다.");
        }
        int version = Integer.parseInt(parts[3]);
        int storedIterations = Integer.parseInt(parts[4]);
        int storedKeyBits = Integer.parseInt(parts[5]);
        byte[] salt = Base64.getUrlDecoder().decode(parts[6]);
        byte[] hash = Base64.getUrlDecoder().decode(parts[7]);
        return new ParsedHash(version, storedIterations, storedKeyBits, salt, hash, false);
    }

    private byte[] derive(char[] rawPassword, byte[] salt, int workFactor, int derivedKeyBits) {
        char[] material = new char[rawPassword.length + pepper.length];
        System.arraycopy(rawPassword, 0, material, 0, rawPassword.length);
        System.arraycopy(pepper, 0, material, rawPassword.length, pepper.length);
        PBEKeySpec spec = new PBEKeySpec(material, salt, workFactor, derivedKeyBits);
        Arrays.fill(material, '\0');
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (Exception ex) {
            throw new IllegalStateException("PBKDF2 비밀번호 해시 처리에 실패했습니다.", ex);
        } finally {
            spec.clearPassword();
        }
    }

    private void requirePassword(char[] rawPassword) {
        if (rawPassword == null || rawPassword.length == 0) {
            throw new IllegalArgumentException("비밀번호는 필수입니다.");
        }
    }

    private record ParsedHash(
            int version,
            int iterations,
            int keyBits,
            byte[] salt,
            byte[] hash,
            boolean legacy) {
    }
}

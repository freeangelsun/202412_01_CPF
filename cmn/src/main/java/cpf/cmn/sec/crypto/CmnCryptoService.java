package cpf.cmn.sec.crypto;

import cpf.cmn.utils.TextUtils;
import cpf.pfw.common.exception.CpfExternalServiceException;
import cpf.pfw.common.exception.CpfValidationException;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.HexFormat;

/**
 * CMN 보안 유틸리티 서비스입니다.
 * Base64, HMAC, AES-GCM, PBKDF2, 난수 생성처럼 여러 업무 모듈이 공유하는 보안 기능을 제공합니다.
 */
@Service
public class CmnCryptoService extends cpf.cmn.common.base.CmnBaseService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int AES_GCM_IV_BYTES = 12;
    private static final int AES_GCM_TAG_BITS = 128;
    private static final int PBKDF2_ITERATIONS = 120_000;
    private static final int PBKDF2_KEY_BITS = 256;

    /**
     * 일반 문자열을 Base64 문자열로 변환합니다.
     *
     * @param plainText 원문
     * @return Base64 문자열
     */
    public String base64Encode(String plainText) {
        return Base64.getEncoder().encodeToString(nullToEmpty(plainText).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Base64 문자열을 원문 문자열로 복원합니다.
     *
     * @param encoded Base64 문자열
     * @return 원문 문자열
     */
    public String base64Decode(String encoded) {
        try {
            return new String(Base64.getDecoder().decode(TextUtils.requireText(encoded, "encoded")), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            throw new CpfValidationException("Base64 값이 올바르지 않습니다.");
        }
    }

    /**
     * 바이트 배열을 URL-safe Base64 문자열로 변환합니다.
     *
     * @param bytes 원본 바이트
     * @return URL-safe Base64 문자열
     */
    public String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes == null ? new byte[0] : bytes);
    }

    /**
     * 문자열을 URL-safe Base64 문자열로 변환합니다.
     *
     * @param plainText 원문
     * @return URL-safe Base64 문자열
     */
    public String base64UrlEncode(String plainText) {
        return base64UrlEncode(nullToEmpty(plainText).getBytes(StandardCharsets.UTF_8));
    }

    public byte[] base64UrlDecode(String encoded) {
        try {
            return Base64.getUrlDecoder().decode(TextUtils.requireText(encoded, "encoded"));
        } catch (IllegalArgumentException ex) {
            throw new CpfValidationException("Base64 URL 값이 올바르지 않습니다.");
        }
    }

    public String base64UrlDecodeToString(String encoded) {
        return new String(base64UrlDecode(encoded), StandardCharsets.UTF_8);
    }

    /**
     * SHA-256 해시를 hex 문자열로 반환합니다.
     *
     * @param plainText 원문
     * @return SHA-256 hex 문자열
     */
    public String sha256Hex(String plainText) {
        return HexFormat.of().formatHex(sha256(nullToEmpty(plainText).getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * SHA-256 해시를 URL-safe Base64 문자열로 반환합니다.
     *
     * @param plainText 원문
     * @return SHA-256 URL-safe Base64 문자열
     */
    public String sha256Base64Url(String plainText) {
        return base64UrlEncode(sha256(nullToEmpty(plainText).getBytes(StandardCharsets.UTF_8)));
    }

    public String hmacSha256Base64Url(String message, String secret) {
        return base64UrlEncode(hmacSha256Bytes(message, secret));
    }

    /**
     * HMAC-SHA256 결과를 hex 문자열로 반환합니다.
     *
     * @param message 서명 대상 메시지
     * @param secret 서명 secret
     * @return HMAC-SHA256 hex 문자열
     */
    public String hmacSha256Hex(String message, String secret) {
        return HexFormat.of().formatHex(hmacSha256Bytes(message, secret));
    }

    public byte[] hmacSha256Bytes(String message, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(TextUtils.requireText(secret, "secret").getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return mac.doFinal(nullToEmpty(message).getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new CpfExternalServiceException("HMAC-SHA256 처리에 실패했습니다.", ex);
        }
    }

    /**
     * AES-GCM 방식으로 문자열을 암호화합니다.
     *
     * @param plainText 원문
     * @param secret 암호화 secret
     * @return IV와 암호문을 점으로 연결한 문자열
     */
    public String aesGcmEncrypt(String plainText, String secret) {
        try {
            byte[] iv = randomBytes(AES_GCM_IV_BYTES);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey(secret), new GCMParameterSpec(AES_GCM_TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(nullToEmpty(plainText).getBytes(StandardCharsets.UTF_8));
            return base64UrlEncode(iv) + "." + base64UrlEncode(encrypted);
        } catch (Exception ex) {
            throw new CpfExternalServiceException("AES-GCM 암호화에 실패했습니다.", ex);
        }
    }

    /**
     * AES-GCM 암호문을 복호화합니다.
     *
     * @param cipherText IV와 암호문을 점으로 연결한 문자열
     * @param secret 복호화 secret
     * @return 복호화된 원문
     */
    public String aesGcmDecrypt(String cipherText, String secret) {
        try {
            String[] parts = TextUtils.requireText(cipherText, "cipherText").split("\\.");
            if (parts.length != 2) {
                throw new CpfValidationException("AES-GCM 암호문 형식이 올바르지 않습니다.");
            }
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, aesKey(secret), new GCMParameterSpec(AES_GCM_TAG_BITS, base64UrlDecode(parts[0])));
            return new String(cipher.doFinal(base64UrlDecode(parts[1])), StandardCharsets.UTF_8);
        } catch (CpfValidationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new CpfExternalServiceException("AES-GCM 복호화에 실패했습니다.", ex);
        }
    }

    /**
     * PBKDF2 방식으로 비밀번호 저장용 해시를 생성합니다.
     *
     * @param password 원문 비밀번호
     * @return PBKDF2 저장 문자열
     */
    public String pbkdf2Hash(String password) {
        try {
            byte[] salt = randomBytes(16);
            byte[] hash = pbkdf2(TextUtils.requireText(password, "password").toCharArray(), salt, PBKDF2_ITERATIONS, PBKDF2_KEY_BITS);
            return "PBKDF2$" + PBKDF2_ITERATIONS + "$"
                    + Base64.getEncoder().encodeToString(salt) + "$"
                    + Base64.getEncoder().encodeToString(hash);
        } catch (Exception ex) {
            throw new CpfExternalServiceException("PBKDF2 해시 생성에 실패했습니다.", ex);
        }
    }

    /**
     * 비밀번호와 저장된 PBKDF2 해시가 일치하는지 확인합니다.
     *
     * @param password 원문 비밀번호
     * @param storedHash 저장된 PBKDF2 해시
     * @return 일치하면 true
     */
    public boolean pbkdf2Matches(String password, String storedHash) {
        try {
            String[] parts = TextUtils.requireText(storedHash, "storedHash").split("\\$");
            if (parts.length != 4 || !"PBKDF2".equals(parts[0])) {
                return false;
            }
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            byte[] actual = pbkdf2(TextUtils.requireText(password, "password").toCharArray(), salt, Integer.parseInt(parts[1]), expected.length * 8);
            return MessageDigest.isEqual(expected, actual);
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * 보안 난수 기반 토큰을 생성합니다.
     *
     * @param byteLength 생성할 난수 바이트 길이
     * @return URL-safe Base64 토큰
     */
    public String secureRandomToken(int byteLength) {
        return base64UrlEncode(randomBytes(Math.max(16, byteLength)));
    }

    public String secureRandomHex(int byteLength) {
        return HexFormat.of().formatHex(randomBytes(Math.max(16, byteLength)));
    }

    private SecretKeySpec aesKey(String secret) {
        return new SecretKeySpec(sha256(TextUtils.requireText(secret, "secret").getBytes(StandardCharsets.UTF_8)), "AES");
    }

    private byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyBits) throws Exception {
        KeySpec spec = new PBEKeySpec(password, salt, iterations, keyBits);
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
    }

    private byte[] sha256(byte[] bytes) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(bytes);
        } catch (Exception ex) {
            throw new CpfExternalServiceException("SHA-256 처리에 실패했습니다.", ex);
        }
    }

    private byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        SECURE_RANDOM.nextBytes(bytes);
        return bytes;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}

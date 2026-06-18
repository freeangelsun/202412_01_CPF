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
 * CMN 蹂댁븞/?뷀샇??怨듯넻 ?쒕퉬?ㅼ엯?덈떎.
 *
 * <p>?낅Т 媛쒕컻?먭? Base64, SHA-256, HMAC, AES-GCM, PBKDF2 媛숈? 湲곕낯 蹂댁븞 泥섎━瑜? * 媛쒕퀎 援ы쁽?섏? ?딅룄濡?怨듯넻 API濡??쒓났?⑸땲?? ?댁쁺 ?ㅼ? ?쒗겕由우? ?뚯뒪媛 ?꾨땲?? * ?섍꼍 蹂?? Vault, KMS, ?щ궡 ?ㅺ?由??쒖뒪?쒖뿉??二쇱엯?댁빞 ?⑸땲??</p>
 */
@Service
public class CmnCryptoService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int AES_GCM_IV_BYTES = 12;
    private static final int AES_GCM_TAG_BITS = 128;
    private static final int PBKDF2_ITERATIONS = 120_000;
    private static final int PBKDF2_KEY_BITS = 256;

    /**
     * 臾몄옄?댁쓣 Base64濡??몄퐫?⑺빀?덈떎.
     *
     * @param plainText ?먮Ц
     * @return Base64 臾몄옄??     */
    public String base64Encode(String plainText) {
        return Base64.getEncoder().encodeToString(nullToEmpty(plainText).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Base64 臾몄옄?댁쓣 UTF-8 臾몄옄?대줈 ?붿퐫?⑺빀?덈떎.
     *
     * @param encoded Base64 臾몄옄??     * @return ?먮Ц
     */
    public String base64Decode(String encoded) {
        try {
            return new String(Base64.getDecoder().decode(TextUtils.requireText(encoded, "encoded")), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            throw new CpfValidationException("Base64 ?붿퐫?⑹뿉 ?ㅽ뙣?덉뒿?덈떎.");
        }
    }

    /**
     * URL-safe Base64濡??몄퐫?⑺빀?덈떎.
     *
     * @param bytes ?먮낯 諛붿씠??     * @return ?⑤뵫???쒓굅??URL-safe Base64
     */
    public String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes == null ? new byte[0] : bytes);
    }

    /**
     * URL-safe Base64 臾몄옄?댁쓣 ?붿퐫?⑺빀?덈떎.
     *
     * @param encoded URL-safe Base64 臾몄옄??     * @return ?붿퐫??諛붿씠??     */
    public String base64UrlEncode(String plainText) {
        return base64UrlEncode(nullToEmpty(plainText).getBytes(StandardCharsets.UTF_8));
    }

    public byte[] base64UrlDecode(String encoded) {
        try {
            return Base64.getUrlDecoder().decode(TextUtils.requireText(encoded, "encoded"));
        } catch (IllegalArgumentException ex) {
            throw new CpfValidationException("Base64Url ?붿퐫?⑹뿉 ?ㅽ뙣?덉뒿?덈떎.");
        }
    }

    /**
     * SHA-256 ?댁떆瑜?16吏꾩닔 臾몄옄?대줈 諛섑솚?⑸땲??
     *
     * @param plainText ?먮Ц
     * @return SHA-256 hex
     */
    public String base64UrlDecodeToString(String encoded) {
        return new String(base64UrlDecode(encoded), StandardCharsets.UTF_8);
    }

    public String sha256Hex(String plainText) {
        return HexFormat.of().formatHex(sha256(nullToEmpty(plainText).getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * HMAC-SHA256 ?쒕챸??URL-safe Base64 臾몄옄?대줈 諛섑솚?⑸땲??
     *
     * @param message ?쒕챸 ???臾몄옄??     * @param secret  ?쒕챸 ?쒗겕由?     * @return HMAC-SHA256 Base64Url
     */
    public String sha256Base64Url(String plainText) {
        return base64UrlEncode(sha256(nullToEmpty(plainText).getBytes(StandardCharsets.UTF_8)));
    }

    public String hmacSha256Base64Url(String message, String secret) {
        return base64UrlEncode(hmacSha256Bytes(message, secret));
    }

    /**
     * HMAC-SHA256 ?쒕챸 諛붿씠?몃? 諛섑솚?⑸땲??
     *
     * @param message ?쒕챸 ???臾몄옄??     * @param secret  ?쒕챸 ?쒗겕由?     * @return HMAC-SHA256 諛붿씠??     */
    public String hmacSha256Hex(String message, String secret) {
        return HexFormat.of().formatHex(hmacSha256Bytes(message, secret));
    }

    public byte[] hmacSha256Bytes(String message, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(TextUtils.requireText(secret, "secret").getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return mac.doFinal(nullToEmpty(message).getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new CpfExternalServiceException("HMAC-SHA256 泥섎━???ㅽ뙣?덉뒿?덈떎.", ex);
        }
    }

    /**
     * AES-GCM 諛⑹떇?쇰줈 臾몄옄?댁쓣 ?뷀샇?뷀빀?덈떎.
     *
     * @param plainText ?먮Ц
     * @param secret    ?뷀샇???쒗겕由?     * @return {@code iv.cipherText} ?뺤떇 ?뷀샇臾?     */
    public String aesGcmEncrypt(String plainText, String secret) {
        try {
            byte[] iv = randomBytes(AES_GCM_IV_BYTES);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey(secret), new GCMParameterSpec(AES_GCM_TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(nullToEmpty(plainText).getBytes(StandardCharsets.UTF_8));
            return base64UrlEncode(iv) + "." + base64UrlEncode(encrypted);
        } catch (Exception ex) {
            throw new CpfExternalServiceException("AES-GCM ?뷀샇?붿뿉 ?ㅽ뙣?덉뒿?덈떎.", ex);
        }
    }

    /**
     * AES-GCM ?뷀샇臾몄쓣 蹂듯샇?뷀빀?덈떎.
     *
     * @param cipherText {@code iv.cipherText} ?뺤떇 ?뷀샇臾?     * @param secret     ?뷀샇???쒗겕由?     * @return ?먮Ц
     */
    public String aesGcmDecrypt(String cipherText, String secret) {
        try {
            String[] parts = TextUtils.requireText(cipherText, "cipherText").split("\\.");
            if (parts.length != 2) {
                throw new CpfValidationException("AES-GCM ?뷀샇臾??뺤떇???щ컮瑜댁? ?딆뒿?덈떎.");
            }
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, aesKey(secret), new GCMParameterSpec(AES_GCM_TAG_BITS, base64UrlDecode(parts[0])));
            return new String(cipher.doFinal(base64UrlDecode(parts[1])), StandardCharsets.UTF_8);
        } catch (CpfValidationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new CpfExternalServiceException("AES-GCM 蹂듯샇?붿뿉 ?ㅽ뙣?덉뒿?덈떎.", ex);
        }
    }

    /**
     * PBKDF2 鍮꾨?踰덊샇 ?댁떆瑜??앹꽦?⑸땲??
     *
     * @param password ?먮Ц 鍮꾨?踰덊샇
     * @return PBKDF2 ?댁떆 臾몄옄??     */
    public String pbkdf2Hash(String password) {
        try {
            byte[] salt = randomBytes(16);
            byte[] hash = pbkdf2(TextUtils.requireText(password, "password").toCharArray(), salt, PBKDF2_ITERATIONS, PBKDF2_KEY_BITS);
            return "PBKDF2$" + PBKDF2_ITERATIONS + "$"
                    + Base64.getEncoder().encodeToString(salt) + "$"
                    + Base64.getEncoder().encodeToString(hash);
        } catch (Exception ex) {
            throw new CpfExternalServiceException("PBKDF2 ?댁떆 ?앹꽦???ㅽ뙣?덉뒿?덈떎.", ex);
        }
    }

    /**
     * PBKDF2 鍮꾨?踰덊샇 ?댁떆瑜?寃利앺빀?덈떎.
     *
     * @param password   ?먮Ц 鍮꾨?踰덊샇
     * @param storedHash ??λ맂 ?댁떆
     * @return ?쇱튂 ?щ?
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
     * URL-safe ?쒖닔 ?좏겙???앹꽦?⑸땲??
     *
     * @param byteLength ?쒖닔 諛붿씠??湲몄씠
     * @return ?좏겙 臾몄옄??     */
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
            throw new CpfExternalServiceException("SHA-256 泥섎━???ㅽ뙣?덉뒿?덈떎.", ex);
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


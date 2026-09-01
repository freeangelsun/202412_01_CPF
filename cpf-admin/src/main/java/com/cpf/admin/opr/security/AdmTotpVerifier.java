package com.cpf.admin.opr.security;

import com.cpf.core.api.error.CpfValidationException;
import com.cpf.security.api.secret.CpfSecretValue;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Locale;

/**
 * RFC 6238 TOTP verifier used by ADM MFA authentication.
 *
 * <p>The referenced secret must be RFC 4648 Base32. The verifier accepts a bounded
 * one-step clock-skew window and compares codes in constant time. Secret material is
 * zeroed after use and is never logged or returned.</p>
 */
@Component
public class AdmTotpVerifier {
    private static final long PERIOD_SECONDS = 30L;
    private static final int DIGITS = 6;
    private static final int WINDOW = 1;
    private final Clock clock;

    public AdmTotpVerifier() {
        this(Clock.systemUTC());
    }

    AdmTotpVerifier(Clock cpfStarterClock) {
        this.clock = cpfStarterClock;
    }

    public boolean verify(CpfSecretValue secretValue, String otpCode) {
        return verify(secretValue, otpCode, clock.instant());
    }

    boolean verify(CpfSecretValue secretValue, String otpCode, Instant instant) {
        if (secretValue == null) throw new CpfValidationException("MFA secret을 확인할 수 없습니다.");
        String normalizedCode = normalizeCode(otpCode);
        char[] encoded = secretValue.copy();
        byte[] secret = null;
        try {
            secret = decodeBase32(encoded);
            long counter = Math.floorDiv(instant.getEpochSecond(), PERIOD_SECONDS);
            for (int offset = -WINDOW; offset <= WINDOW; offset++) {
                String expected = generate(secret, counter + offset);
                if (constantTimeEquals(expected, normalizedCode)) return true;
            }
            return false;
        } finally {
            Arrays.fill(encoded, '\0');
            if (secret != null) Arrays.fill(secret, (byte) 0);
        }
    }

    private static String normalizeCode(String otpCode) {
        if (otpCode == null) throw new CpfValidationException("otpCode는 필수입니다.");
        String normalized = otpCode.trim();
        if (!normalized.matches("\\d{" + DIGITS + "}")) {
            throw new CpfValidationException("otpCode 형식이 올바르지 않습니다.");
        }
        return normalized;
    }

    private static String generate(byte[] secret, long counter) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(secret, "HmacSHA1"));
            byte[] digest = mac.doFinal(ByteBuffer.allocate(Long.BYTES).putLong(counter).array());
            int offset = digest[digest.length - 1] & 0x0f;
            int binary = ((digest[offset] & 0x7f) << 24)
                    | ((digest[offset + 1] & 0xff) << 16)
                    | ((digest[offset + 2] & 0xff) << 8)
                    | (digest[offset + 3] & 0xff);
            int value = binary % 1_000_000;
            return String.format(Locale.ROOT, "%0" + DIGITS + "d", value);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("JVM TOTP algorithm을 초기화할 수 없습니다.", ex);
        }
    }

    private static boolean constantTimeEquals(String left, String right) {
        byte[] a = left.getBytes(StandardCharsets.US_ASCII);
        byte[] b = right.getBytes(StandardCharsets.US_ASCII);
        int diff = a.length ^ b.length;
        int max = Math.max(a.length, b.length);
        for (int i = 0; i < max; i++) {
            byte av = i < a.length ? a[i] : 0;
            byte bv = i < b.length ? b[i] : 0;
            diff |= av ^ bv;
        }
        return diff == 0;
    }

    private static byte[] decodeBase32(char[] chars) {
        StringBuilder normalized = new StringBuilder(chars.length);
        for (char c : chars) {
            if (Character.isWhitespace(c) || c == '-' || c == '=') continue;
            char upper = Character.toUpperCase(c);
            if ((upper < 'A' || upper > 'Z') && (upper < '2' || upper > '7')) {
                throw new CpfValidationException("MFA secret 형식이 올바르지 않습니다.");
            }
            normalized.append(upper);
        }
        if (normalized.isEmpty()) throw new CpfValidationException("MFA secret은 비어 있을 수 없습니다.");
        byte[] result = new byte[(normalized.length() * 5) / 8];
        int buffer = 0;
        int bitsLeft = 0;
        int index = 0;
        for (int i = 0; i < normalized.length(); i++) {
            char c = normalized.charAt(i);
            int value = c >= 'A' && c <= 'Z' ? c - 'A' : c - '2' + 26;
            buffer = (buffer << 5) | value;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                result[index++] = (byte) ((buffer >> (bitsLeft - 8)) & 0xff);
                bitsLeft -= 8;
            }
        }
        return index == result.length ? result : Arrays.copyOf(result, index);
    }
}

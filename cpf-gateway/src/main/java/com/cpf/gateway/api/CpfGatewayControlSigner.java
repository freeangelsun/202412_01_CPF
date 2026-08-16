package com.cpf.gateway.api;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Objects;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** Gateway Control Plane 요청의 method·target·content·caller·audience를 HMAC-SHA256으로 보호합니다. */
public final class CpfGatewayControlSigner {
    private static final String HMAC = "HmacSHA256";
    public static final String EMPTY_BODY_SHA256 = sha256(new byte[0]);

    private CpfGatewayControlSigner() {
    }

    public static String sign(
            String secret,
            String method,
            String requestTarget,
            String contentType,
            String contentSha256,
            String callerService,
            String operatorId,
            long timestampEpochMillis,
            String nonce,
            String audience,
            String keyId) {
        String canonical = canonical(method, requestTarget, contentType, contentSha256, callerService,
                operatorId, timestampEpochMillis, nonce, audience, keyId);
        try {
            Mac mac = Mac.getInstance(HMAC);
            mac.init(new SecretKeySpec(required(secret, "secret").getBytes(StandardCharsets.UTF_8), HMAC));
            return HexFormat.of().formatHex(mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.GeneralSecurityException ex) {
            throw new IllegalStateException("Gateway Control HMAC을 계산할 수 없습니다.", ex);
        }
    }

    public static boolean verify(
            String secret,
            String method,
            String requestTarget,
            String contentType,
            String contentSha256,
            String callerService,
            String operatorId,
            long timestampEpochMillis,
            String nonce,
            String audience,
            String keyId,
            String signature) {
        String expected = sign(secret, method, requestTarget, contentType, contentSha256, callerService,
                operatorId, timestampEpochMillis, nonce, audience, keyId);
        byte[] expectedBytes = expected.getBytes(StandardCharsets.US_ASCII);
        byte[] actualBytes = Objects.toString(signature, "").trim().toLowerCase(Locale.ROOT)
                .getBytes(StandardCharsets.US_ASCII);
        return MessageDigest.isEqual(expectedBytes, actualBytes);
    }

    public static String canonical(
            String method,
            String requestTarget,
            String contentType,
            String contentSha256,
            String callerService,
            String operatorId,
            long timestampEpochMillis,
            String nonce,
            String audience,
            String keyId) {
        return required(method, "method").toUpperCase(Locale.ROOT) + "\n"
                + normalizedTarget(requestTarget) + "\n"
                + normalizedContentType(contentType) + "\n"
                + requiredLowerHex(contentSha256, "contentSha256") + "\n"
                + required(callerService, "callerService") + "\n"
                + required(operatorId, "operatorId") + "\n"
                + timestampEpochMillis + "\n"
                + required(nonce, "nonce") + "\n"
                + required(audience, "audience") + "\n"
                + required(keyId, "keyId");
    }

    public static String sha256(byte[] body) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(body == null ? new byte[0] : body));
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256을 사용할 수 없습니다.", ex);
        }
    }

    public static void requireFresh(long timestampEpochMillis, Clock clock, Duration allowedSkew) {
        Instant requestedAt;
        try {
            requestedAt = Instant.ofEpochMilli(timestampEpochMillis);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Gateway Control timestamp가 올바르지 않습니다.", ex);
        }
        Duration skew = Duration.between(requestedAt, clock.instant()).abs();
        if (skew.compareTo(allowedSkew) > 0) {
            throw new IllegalArgumentException("Gateway Control 요청 시각 허용 범위를 초과했습니다.");
        }
    }

    /** 기존 Caller의 Source Compatibility용이며 신규 Control 요청에는 사용하지 않습니다. */
    @Deprecated(forRemoval = true)
    public static String sign(
            String secret, String method, String requestTarget, String callerService, String operatorId,
            long timestampEpochMillis, String nonce) {
        return sign(secret, method, requestTarget, "", EMPTY_BODY_SHA256, callerService, operatorId,
                timestampEpochMillis, nonce, "LEGACY", "legacy");
    }

    /** 기존 Caller의 Source Compatibility용이며 신규 Control 요청에는 사용하지 않습니다. */
    @Deprecated(forRemoval = true)
    public static boolean verify(
            String secret, String method, String requestTarget, String callerService, String operatorId,
            long timestampEpochMillis, String nonce, String signature) {
        return verify(secret, method, requestTarget, "", EMPTY_BODY_SHA256, callerService, operatorId,
                timestampEpochMillis, nonce, "LEGACY", "legacy", signature);
    }

    private static String normalizedTarget(String target) {
        String result = required(target, "requestTarget");
        if (!result.startsWith("/") || result.indexOf('\r') >= 0 || result.indexOf('\n') >= 0) {
            throw new IllegalArgumentException("requestTarget must be an absolute-path without control characters");
        }
        return result;
    }

    private static String normalizedContentType(String value) {
        if (value == null || value.isBlank()) return "";
        return value.trim().toLowerCase(Locale.ROOT).replaceAll("\\s*;\\s*", ";");
    }

    private static String requiredLowerHex(String value, String field) {
        String result = required(value, field).toLowerCase(Locale.ROOT);
        if (!result.matches("[0-9a-f]{64}")) throw new IllegalArgumentException(field + " must be SHA-256 hex");
        return result;
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        return value.trim();
    }
}

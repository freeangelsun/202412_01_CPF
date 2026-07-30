package com.cpf.core.api.gateway;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Objects;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** Gateway Control Plane 요청을 HMAC-SHA256으로 서명·검증하는 topology-independent utility입니다. */
public final class CpfGatewayControlSigner {
    private static final String HMAC = "HmacSHA256";

    private CpfGatewayControlSigner() {
    }

    public static String sign(
            String secret,
            String method,
            String requestTarget,
            String callerService,
            String operatorId,
            long timestampEpochMillis,
            String nonce) {
        String canonical = canonical(method, requestTarget, callerService, operatorId, timestampEpochMillis, nonce);
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
            String callerService,
            String operatorId,
            long timestampEpochMillis,
            String nonce,
            String signature) {
        String expected = sign(secret, method, requestTarget, callerService, operatorId, timestampEpochMillis, nonce);
        byte[] expectedBytes = expected.getBytes(StandardCharsets.US_ASCII);
        byte[] actualBytes = Objects.toString(signature, "").trim().toLowerCase(java.util.Locale.ROOT)
                .getBytes(StandardCharsets.US_ASCII);
        return MessageDigest.isEqual(expectedBytes, actualBytes);
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

    public static String canonical(
            String method,
            String requestTarget,
            String callerService,
            String operatorId,
            long timestampEpochMillis,
            String nonce) {
        return required(method, "method").toUpperCase(java.util.Locale.ROOT) + "\n"
                + required(requestTarget, "requestTarget") + "\n"
                + required(callerService, "callerService") + "\n"
                + required(operatorId, "operatorId") + "\n"
                + timestampEpochMillis + "\n"
                + required(nonce, "nonce");
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }
}

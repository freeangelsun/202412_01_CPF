package com.cpf.core.api.gateway;

import java.time.Instant;
import java.util.Objects;

/**
 * Gateway Control API의 다중 Instance Replay 방지 Claim Port입니다.
 *
 * <p>구현은 동일 Audience/Key/Caller/Nonce 조합에 대해 단 한 번만 {@code true}를 반환해야 하며,
 * 저장소 장애를 성공으로 간주해서는 안 됩니다.</p>
 */
public interface CpfGatewayControlNoncePort {

    boolean claim(NonceClaim claim);

    record NonceClaim(
            String audience,
            String keyId,
            String callerId,
            String nonce,
            Instant claimedAt,
            Instant expiresAt) {
        public NonceClaim {
            audience = required(audience, "audience");
            keyId = required(keyId, "keyId");
            callerId = required(callerId, "callerId");
            nonce = required(nonce, "nonce");
            claimedAt = Objects.requireNonNull(claimedAt, "claimedAt");
            expiresAt = Objects.requireNonNull(expiresAt, "expiresAt");
            if (!expiresAt.isAfter(claimedAt)) {
                throw new IllegalArgumentException("expiresAt must be after claimedAt");
            }
        }

        private static String required(String value, String field) {
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException(field + " is required");
            }
            return value.trim();
        }
    }
}

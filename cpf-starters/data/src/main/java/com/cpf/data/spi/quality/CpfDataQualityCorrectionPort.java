package com.cpf.data.spi.quality;

import com.cpf.data.api.quality.CpfDataQualityOperations;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Owner-side mutation SPI for data-quality correction.
 *
 * <p>This is not a caller authorization API. The sole production framework consumer is the ADM-owned final correction gateway. The gateway
 * verifies the immutable approval snapshot, short-lived HMAC capability and durable single-use
 * nonce before any provider mutation is invoked.</p>
 */
/** CpfDataQualityCorrectionPort 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfDataQualityCorrectionPort {
    CpfDataQualityOperations.QuarantineItem correctApproved(ApprovedCorrection command);

    record ApprovedCorrection(
            String quarantineId,
            long expectedVersion,
            Map<String, Object> corrected,
            String actorId,
            String reason,
            String approvalExecutionReference,
            String payloadHash,
            String nonce,
            String proof,
            Instant approvedAt) {
        public ApprovedCorrection {
            if (quarantineId == null || quarantineId.isBlank()) throw new IllegalArgumentException("quarantineId is required");
            if (expectedVersion < 1) throw new IllegalArgumentException("expectedVersion must be positive");
            if (corrected == null || corrected.isEmpty()) throw new IllegalArgumentException("corrected payload is required");
            corrected = Collections.unmodifiableMap(new LinkedHashMap<>(corrected));
            if (actorId == null || actorId.isBlank()) throw new IllegalArgumentException("actorId is required");
            if (reason == null || reason.isBlank()) throw new IllegalArgumentException("reason is required");
            if (approvalExecutionReference == null || approvalExecutionReference.isBlank())
                throw new IllegalArgumentException("approvalExecutionReference is required");
            if (payloadHash == null || !payloadHash.matches("[0-9a-fA-F]{64}"))
                throw new IllegalArgumentException("payloadHash is required");
            if (nonce == null || nonce.length() < 16) throw new IllegalArgumentException("nonce is required");
            if (proof == null || proof.length() < 32) throw new IllegalArgumentException("proof is required");
            if (approvedAt == null) throw new IllegalArgumentException("approvedAt is required");
        }
    }
}

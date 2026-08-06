package com.cpf.core.spi.data.quality;

import com.cpf.core.api.data.quality.CpfDataQualityOperations;

import java.time.Instant;
import java.util.Map;

/**
 * Owner-side mutation SPI for data-quality correction.
 *
 * <p>This is not a caller authorization API. The sole framework consumer is the ADM Owner Command
 * adapter after it verifies the database-backed single-use execution reservation and immutable
 * snapshot hash.</p>
 */
public interface CpfDataQualityCorrectionPort {
    CpfDataQualityOperations.QuarantineItem correctApproved(ApprovedCorrection command);

    record ApprovedCorrection(
            String quarantineId,
            long expectedVersion,
            Map<String, Object> corrected,
            String actorId,
            String reason,
            String approvalExecutionReference,
            Instant approvedAt) {
        public ApprovedCorrection {
            if (quarantineId == null || quarantineId.isBlank()) throw new IllegalArgumentException("quarantineId is required");
            if (expectedVersion < 1) throw new IllegalArgumentException("expectedVersion must be positive");
            if (corrected == null || corrected.isEmpty()) throw new IllegalArgumentException("corrected payload is required");
            corrected = Map.copyOf(corrected);
            if (actorId == null || actorId.isBlank()) throw new IllegalArgumentException("actorId is required");
            if (reason == null || reason.isBlank()) throw new IllegalArgumentException("reason is required");
            if (approvalExecutionReference == null || approvalExecutionReference.isBlank())
                throw new IllegalArgumentException("approvalExecutionReference is required");
            if (approvedAt == null) throw new IllegalArgumentException("approvedAt is required");
        }
    }
}

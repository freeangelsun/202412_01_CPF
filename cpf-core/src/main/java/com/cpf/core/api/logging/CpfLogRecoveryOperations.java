package com.cpf.core.api.logging;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 운영자가 poison 로그 복구 항목을 승인된 명령 범위 안에서 재시도하기 위한 공개 계약입니다.
 *
 * <p>승인은 복구 ID, 현재 재시도 횟수, 요청자와 사유를 포함한 명령 해시에 결합됩니다.
 * 따라서 다른 poison 항목이나 상태가 변경된 항목에 승인을 재사용할 수 없습니다.</p>
 */
public interface CpfLogRecoveryOperations {
    Duration MAX_APPROVAL_LIFETIME = Duration.ofHours(24);

    PoisonRetryResult retryPoison(PoisonRetryCommand command, PoisonRetryApproval approval);

    record PoisonRetryCommand(
            String recoveryEventId,
            int expectedAttemptCount,
            String requesterId,
            String reason) {
        private static final Pattern RECOVERY_ID = Pattern.compile("[0-9a-f]{64}");

        public PoisonRetryCommand {
            recoveryEventId = requireMatch(recoveryEventId, RECOVERY_ID, "recoveryEventId");
            if (expectedAttemptCount < 0 || expectedAttemptCount > 10_000) {
                throw new IllegalArgumentException("expectedAttemptCount must be between 0 and 10000");
            }
            requesterId = requireText(requesterId, 128, "requesterId");
            reason = requireText(reason, 512, "reason");
        }

        public String commandHash() {
            String canonical = recoveryEventId + '\n'
                    + expectedAttemptCount + '\n'
                    + requesterId + '\n'
                    + reason;
            try {
                return HexFormat.of().formatHex(
                        MessageDigest.getInstance("SHA-256")
                                .digest(canonical.getBytes(StandardCharsets.UTF_8)));
            } catch (NoSuchAlgorithmException impossible) {
                throw new IllegalStateException("SHA-256 is unavailable", impossible);
            }
        }

        @Override
        public String toString() {
            return "PoisonRetryCommand[recoveryEventId=" + recoveryEventId
                    + ", expectedAttemptCount=" + expectedAttemptCount
                    + ", requesterId=" + requesterId
                    + ", reason=[REDACTED]]";
        }
    }

    record PoisonRetryApproval(
            String approvalId,
            String approverId,
            String commandHash,
            Instant approvedAt,
            Instant expiresAt) {
        private static final Pattern SHA256 = Pattern.compile("[0-9a-f]{64}");

        public PoisonRetryApproval {
            approvalId = requireText(approvalId, 128, "approvalId");
            approverId = requireText(approverId, 128, "approverId");
            commandHash = requireMatch(commandHash, SHA256, "commandHash");
            approvedAt = Objects.requireNonNull(approvedAt, "approvedAt");
            expiresAt = Objects.requireNonNull(expiresAt, "expiresAt");
            if (!expiresAt.isAfter(approvedAt)) {
                throw new IllegalArgumentException("expiresAt must be after approvedAt");
            }
            if (expiresAt.isAfter(safePlus(approvedAt, MAX_APPROVAL_LIFETIME))) {
                throw new IllegalArgumentException("approval lifetime exceeds 24 hours");
            }
        }

        public static PoisonRetryApproval approve(
                String approvalId,
                String approverId,
                PoisonRetryCommand command,
                Instant approvedAt,
                Duration lifetime) {
            Objects.requireNonNull(command, "command");
            Objects.requireNonNull(lifetime, "lifetime");
            if (lifetime.isZero() || lifetime.isNegative() || lifetime.compareTo(MAX_APPROVAL_LIFETIME) > 0) {
                throw new IllegalArgumentException("approval lifetime must be between 1ns and 24 hours");
            }
            return new PoisonRetryApproval(
                    approvalId,
                    approverId,
                    command.commandHash(),
                    approvedAt,
                    safePlus(approvedAt, lifetime));
        }

        public boolean activeAt(Instant now) {
            Objects.requireNonNull(now, "now");
            return !now.isBefore(approvedAt) && now.isBefore(expiresAt);
        }
    }

    enum PoisonRetryStatus {
        RETRIED,
        NOT_FOUND,
        APPROVAL_REQUIRED,
        SEPARATION_OF_DUTIES,
        APPROVAL_EXPIRED,
        APPROVAL_SCOPE_MISMATCH,
        STALE_ATTEMPT,
        UNKNOWN_RESULT,
        FAILED
    }

    record PoisonRetryResult(
            PoisonRetryStatus status,
            String recoveryEventId,
            String approvalId,
            Instant decidedAt,
            String errorCode) {
        public PoisonRetryResult {
            status = Objects.requireNonNull(status, "status");
            recoveryEventId = requireMatch(
                    recoveryEventId,
                    PoisonRetryCommand.RECOVERY_ID,
                    "recoveryEventId");
            approvalId = approvalId == null ? null : requireText(approvalId, 128, "approvalId");
            decidedAt = Objects.requireNonNull(decidedAt, "decidedAt");
            errorCode = errorCode == null ? null : requireText(errorCode, 128, "errorCode");
        }

        public boolean retried() {
            return status == PoisonRetryStatus.RETRIED;
        }
    }

    static String requireText(String value, int maximumLength, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
        String trimmed = value.trim();
        if (trimmed.length() > maximumLength) {
            throw new IllegalArgumentException(name + " exceeds maximum length");
        }
        return trimmed;
    }

    static String requireMatch(String value, Pattern pattern, String name) {
        String normalized = requireText(value, 512, name);
        if (!pattern.matcher(normalized).matches()) {
            throw new IllegalArgumentException(name + " has an invalid format");
        }
        return normalized;
    }

    static Instant safePlus(Instant instant, Duration duration) {
        try {
            return instant.plus(duration);
        } catch (RuntimeException overflow) {
            throw new IllegalArgumentException("approval expiry exceeds supported instant range", overflow);
        }
    }
}

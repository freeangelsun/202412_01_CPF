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

    /**
     * retryPoison 운영 조작을 수행합니다. 구현체는 인증 주체, 승인, 멱등성, 동시성, 감사와 UNKNOWN/복구 의미를 보존해야 합니다.
     *
     * <p>고위험 운영 Consumer는 가능한 경우 {@code CpfBatchRiskCommand} 기반 overload를 사용하고,
     * 버전 불일치·승인 불일치·결과 불확실성을 fail-closed로 처리해야 합니다.</p>
     * @param command 승인·멱등·감사·expectedVersion을 결합한 위험조치 명령입니다.
     * @param approval 복구 명령 해시에 결합된 승인 정보입니다.
     * @return 조작 결과입니다. 실패·충돌·UNKNOWN 상태를 성공으로 축약해서는 안 됩니다.
     */
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
/**
 * commandHash 공개 조회/판정 계약을 수행합니다. 조회·판정 자체는 업무 상태를 변경하지 않으며 실패나 UNKNOWN을 정상 결과로 오인하지 않습니다.
 * @return 승인 범위를 결합하는 SHA-256 명령 해시입니다.
 */

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
        /**
         * 운영 로그용 문자열을 반환합니다. 민감한 사유와 비밀값은 원문으로 노출하지 않습니다.
         * @return 계약에 정의된 결과입니다.
         */
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
/**
 * approve 운영 조작을 수행합니다. 구현체는 인증 주체, 승인, 멱등성, 동시성, 감사와 UNKNOWN/복구 의미를 보존해야 합니다.
 * @param approvalId 승인 식별자입니다.
 * @param approverId 승인자 식별자입니다.
 * @param command 승인·멱등·감사·expectedVersion을 결합한 위험조치 명령입니다.
 * @param approvedAt 승인 시각입니다.
 * @param lifetime 승인 유효기간입니다. 0보다 크고 최대 허용기간 이하여야 합니다.
 * @return 계약에 정의된 결과입니다.
 * @throws IllegalArgumentException 입력이 계약 범위를 벗어나거나 시간 계산이 안전 범위를 넘는 경우
 */

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
/**
 * activeAt 공개 조회/판정 계약을 수행합니다. 조회·판정 자체는 업무 상태를 변경하지 않으며 실패나 UNKNOWN을 정상 결과로 오인하지 않습니다.
 *
 * <p>고위험 운영 Consumer는 가능한 경우 {@code CpfBatchRiskCommand} 기반 overload를 사용하고,
 * 버전 불일치·승인 불일치·결과 불확실성을 fail-closed로 처리해야 합니다.</p>
 * @param now 승인 유효성을 평가할 현재 시각입니다.
 * @return 조작 결과입니다. 실패·충돌·UNKNOWN 상태를 성공으로 축약해서는 안 됩니다.
 * @throws IllegalArgumentException 입력이 계약 범위를 벗어나거나 시간 계산이 안전 범위를 넘는 경우
 */

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
/**
 * retried 공개 조회/판정 계약을 수행합니다. 조회·판정 자체는 업무 상태를 변경하지 않으며 실패나 UNKNOWN을 정상 결과로 오인하지 않습니다.
 * @return 복구 상태가 RETRIED이면 true입니다.
 */

        public boolean retried() {
            return status == PoisonRetryStatus.RETRIED;
        }
    }

    /**
     * requireText 입력 계약을 검증/정규화합니다. 잘못된 입력은 fail-closed로 거부합니다.
     * @param value 검증할 값입니다.
     * @param maximumLength 허용 최대 문자열 길이입니다.
     * @param name 오류 메시지에 사용할 필드명입니다.
     * @return 계약에 정의된 결과입니다.
     * @throws IllegalArgumentException 입력이 계약 범위를 벗어나거나 시간 계산이 안전 범위를 넘는 경우
     */
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

    /**
     * requireMatch 입력 계약을 검증/정규화합니다. 잘못된 입력은 fail-closed로 거부합니다.
     * @param value 검증할 값입니다.
     * @param pattern 허용 형식을 검증할 정규식입니다.
     * @param name 오류 메시지에 사용할 필드명입니다.
     * @return 계약에 정의된 결과입니다.
     * @throws IllegalArgumentException 입력이 계약 범위를 벗어나거나 시간 계산이 안전 범위를 넘는 경우
     */
    static String requireMatch(String value, Pattern pattern, String name) {
        String normalized = requireText(value, 512, name);
        if (!pattern.matcher(normalized).matches()) {
            throw new IllegalArgumentException(name + " has an invalid format");
        }
        return normalized;
    }

    /**
     * safePlus 입력 계약을 검증/정규화합니다. 잘못된 입력은 fail-closed로 거부합니다.
     * @param instant 기준 시각입니다.
     * @param duration 더할 기간입니다.
     * @return 계약에 정의된 결과입니다.
     * @throws IllegalArgumentException 입력이 계약 범위를 벗어나거나 시간 계산이 안전 범위를 넘는 경우
     */
    static Instant safePlus(Instant instant, Duration duration) {
        try {
            return instant.plus(duration);
        } catch (RuntimeException overflow) {
            throw new IllegalArgumentException("approval expiry exceeds supported instant range", overflow);
        }
    }
}

package com.cpf.core.api.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Objects;
import java.util.regex.Pattern;

/** 민감정보 원문 조회를 독립 승인과 일회성 소비로 제한하는 topology-independent 계약입니다. */
public interface CpfSensitiveDataAccessOperations {
    Duration MAX_ACCESS_LIFETIME = Duration.ofMinutes(15);

    AccessResult request(AccessRequestCommand command);

    AccessResult approve(AccessApprovalCommand command);

    AccessResult reject(AccessRejectionCommand command);

    AccessResult consume(AccessConsumeCommand command);

    AccessResult find(String requestId);

    record AccessRequestCommand(
            String requestId,
            String idempotencyKey,
            String requesterId,
            String resourceType,
            String resourceIdHash,
            String dataScope,
            String reason) {
        private static final Pattern SAFE_ID = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._:-]{2,127}");
        private static final Pattern SHA256 = Pattern.compile("[0-9a-f]{64}");

        public AccessRequestCommand {
            requestId = match(requestId, SAFE_ID, "requestId");
            idempotencyKey = match(idempotencyKey, SAFE_ID, "idempotencyKey");
            requesterId = match(requesterId, SAFE_ID, "requesterId");
            resourceType = match(resourceType, SAFE_ID, "resourceType");
            resourceIdHash = match(resourceIdHash, SHA256, "resourceIdHash");
            dataScope = text(dataScope, 256, "dataScope");
            reason = safeReason(reason);
        }

        public String immutableHash() {
            return sha256(requestId + '\n' + idempotencyKey + '\n' + requesterId + '\n'
                    + resourceType + '\n' + resourceIdHash + '\n' + dataScope + '\n' + reason);
        }

        @Override
        public String toString() {
            return "AccessRequestCommand[requestId=" + requestId
                    + ", idempotencyKey=" + idempotencyKey
                    + ", requesterId=" + requesterId
                    + ", resourceType=" + resourceType
                    + ", resourceIdHash=" + resourceIdHash
                    + ", dataScope=" + dataScope
                    + ", reason=[REDACTED]]";
        }
    }

    record AccessApprovalCommand(
            String requestId,
            long expectedVersion,
            String approverId,
            Duration validFor) {
        public AccessApprovalCommand {
            requestId = match(requestId, AccessRequestCommand.SAFE_ID, "requestId");
            approverId = match(approverId, AccessRequestCommand.SAFE_ID, "approverId");
            if (expectedVersion < 1L) {
                throw new IllegalArgumentException("expectedVersion must be positive");
            }
            validFor = Objects.requireNonNull(validFor, "validFor");
            if (validFor.isZero() || validFor.isNegative() || validFor.compareTo(MAX_ACCESS_LIFETIME) > 0) {
                throw new IllegalArgumentException("validFor must be between 1ns and 15 minutes");
            }
        }
    }

    record AccessRejectionCommand(
            String requestId,
            long expectedVersion,
            String approverId,
            String reason) {
        public AccessRejectionCommand {
            requestId = match(requestId, AccessRequestCommand.SAFE_ID, "requestId");
            approverId = match(approverId, AccessRequestCommand.SAFE_ID, "approverId");
            if (expectedVersion < 1L) {
                throw new IllegalArgumentException("expectedVersion must be positive");
            }
            reason = safeReason(reason);
        }
    }

    record AccessConsumeCommand(
            String requestId,
            long expectedVersion,
            String accessorId,
            String resourceType,
            String resourceIdHash,
            String dataScope) {
        public AccessConsumeCommand {
            requestId = match(requestId, AccessRequestCommand.SAFE_ID, "requestId");
            accessorId = match(accessorId, AccessRequestCommand.SAFE_ID, "accessorId");
            resourceType = match(resourceType, AccessRequestCommand.SAFE_ID, "resourceType");
            resourceIdHash = match(resourceIdHash, AccessRequestCommand.SHA256, "resourceIdHash");
            dataScope = text(dataScope, 256, "dataScope");
            if (expectedVersion < 1L) {
                throw new IllegalArgumentException("expectedVersion must be positive");
            }
        }
    }

    enum AccessStatus {
        PENDING,
        APPROVED,
        REJECTED,
        CONSUMED,
        EXPIRED,
        NOT_FOUND,
        VERSION_CONFLICT,
        IDEMPOTENCY_CONFLICT,
        SEPARATION_OF_DUTIES,
        SCOPE_MISMATCH,
        ACCESSOR_MISMATCH,
        AUDIT_UNAVAILABLE,
        RESOURCE_EXHAUSTED,
        UNKNOWN_RESULT,
        IDEMPOTENT_REPLAY,
        INVALID_STATE
    }

    record AccessGrant(
            String requestId,
            long version,
            AccessStatus status,
            String requesterId,
            String resourceType,
            String resourceIdHash,
            String dataScope,
            String reason,
            String approverId,
            Instant requestedAt,
            Instant approvedAt,
            Instant expiresAt,
            Instant consumedAt,
            String immutableHash) {
        public AccessGrant {
            requestId = match(requestId, AccessRequestCommand.SAFE_ID, "requestId");
            status = Objects.requireNonNull(status, "status");
            requesterId = match(requesterId, AccessRequestCommand.SAFE_ID, "requesterId");
            resourceType = match(resourceType, AccessRequestCommand.SAFE_ID, "resourceType");
            resourceIdHash = match(resourceIdHash, AccessRequestCommand.SHA256, "resourceIdHash");
            dataScope = text(dataScope, 256, "dataScope");
            reason = safeReason(reason);
            immutableHash = match(immutableHash, AccessRequestCommand.SHA256, "immutableHash");
            requestedAt = Objects.requireNonNull(requestedAt, "requestedAt");
            if (version < 1L) {
                throw new IllegalArgumentException("version must be positive");
            }
        }

        public boolean activeAt(Instant now) {
            return status == AccessStatus.APPROVED
                    && approvedAt != null
                    && expiresAt != null
                    && !now.isBefore(approvedAt)
                    && now.isBefore(expiresAt);
        }

        @Override
        public String toString() {
            return "AccessGrant[requestId=" + requestId
                    + ", version=" + version
                    + ", status=" + status
                    + ", requesterId=" + requesterId
                    + ", resourceType=" + resourceType
                    + ", resourceIdHash=" + resourceIdHash
                    + ", dataScope=" + dataScope
                    + ", reason=[REDACTED]"
                    + ", approverId=" + approverId
                    + ", requestedAt=" + requestedAt
                    + ", approvedAt=" + approvedAt
                    + ", expiresAt=" + expiresAt
                    + ", consumedAt=" + consumedAt
                    + ", immutableHash=" + immutableHash + "]";
        }
    }

    record AccessResult(AccessStatus status, AccessGrant grant, String errorCode) {
        public AccessResult {
            status = Objects.requireNonNull(status, "status");
            if (grant != null && grant.status() != status
                    && status != AccessStatus.VERSION_CONFLICT
                    && status != AccessStatus.IDEMPOTENCY_CONFLICT
                    && status != AccessStatus.SEPARATION_OF_DUTIES
                    && status != AccessStatus.SCOPE_MISMATCH
                    && status != AccessStatus.ACCESSOR_MISMATCH
                    && status != AccessStatus.AUDIT_UNAVAILABLE
                    && status != AccessStatus.RESOURCE_EXHAUSTED
                    && status != AccessStatus.UNKNOWN_RESULT
                    && status != AccessStatus.IDEMPOTENT_REPLAY
                    && status != AccessStatus.INVALID_STATE) {
                throw new IllegalArgumentException("result status and grant status are inconsistent");
            }
            errorCode = errorCode == null ? null : text(errorCode, 128, "errorCode");
        }

        public boolean consumed() {
            return status == AccessStatus.CONSUMED
                    || (status == AccessStatus.IDEMPOTENT_REPLAY
                    && grant != null && grant.status() == AccessStatus.CONSUMED);
        }
    }

    static String safeReason(String value) {
        final String normalized;
        try {
            normalized = CpfSensitiveData.sanitizeAuditReason(value);
        } catch (RuntimeException invalidReason) {
            throw new IllegalArgumentException("reason must be 10..500 safe characters", invalidReason);
        }
        if (normalized.length() < 10 || normalized.indexOf('\r') >= 0 || normalized.indexOf('\n') >= 0) {
            throw new IllegalArgumentException("reason must be 10..500 safe characters");
        }
        return normalized;
    }

    static String text(String value, int maximum, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
        String normalized = value.trim();
        if (normalized.length() > maximum) {
            throw new IllegalArgumentException(name + " exceeds maximum length");
        }
        return normalized;
    }

    static String match(String value, Pattern pattern, String name) {
        String normalized = text(value, 512, name);
        if (!pattern.matcher(normalized).matches()) {
            throw new IllegalArgumentException(name + " has invalid format");
        }
        return normalized;
    }

    static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is unavailable", impossible);
        }
    }
}

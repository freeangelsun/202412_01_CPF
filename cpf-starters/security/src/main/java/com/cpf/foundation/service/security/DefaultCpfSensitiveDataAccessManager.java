package com.cpf.foundation.service.security;

import com.cpf.security.api.CpfSensitiveDataAccessOperations;
import com.cpf.security.spi.CpfSensitiveDataAccessAuditSink;
import com.cpf.security.spi.CpfSensitiveDataAccessStore;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

/** 원문 조회 승인 상태기계의 기본 구현입니다. 원문 값 자체는 저장하거나 감사 이벤트에 기록하지 않습니다. */
public final class DefaultCpfSensitiveDataAccessManager implements CpfSensitiveDataAccessOperations {
    private final CpfSensitiveDataAccessStore store;
    private final CpfSensitiveDataAccessAuditSink auditSink;
    private final Clock clock;

    public DefaultCpfSensitiveDataAccessManager(
            CpfSensitiveDataAccessStore store,
            CpfSensitiveDataAccessAuditSink auditSink,
            Clock clock) {
        this.store = Objects.requireNonNull(store, "store");
        this.auditSink = Objects.requireNonNull(auditSink, "auditSink");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public AccessResult request(AccessRequestCommand command) {
        Objects.requireNonNull(command, "command");
        Instant now = clock.instant();
        AccessGrant pending = new AccessGrant(
                command.requestId(), 1L, AccessStatus.PENDING, command.requesterId(),
                command.resourceType(), command.resourceIdHash(), command.dataScope(), command.reason(),
                null, now, null, null, null, command.immutableHash());
        if (!auditSink.available()) {
            return result(AccessStatus.AUDIT_UNAVAILABLE, null, "CPF_SENSITIVE_ACCESS_AUDIT_UNAVAILABLE");
        }
        final CpfSensitiveDataAccessStore.CreateResult created;
        try {
            created = store.createIfAbsent(pending);
        } catch (RuntimeException storeFailure) {
            return result(AccessStatus.UNKNOWN_RESULT, null, "CPF_SENSITIVE_ACCESS_STORE_UNAVAILABLE");
        }
        if (created.resourceExhausted()) {
            audit("REQUEST_CAPACITY_REJECTED", AccessStatus.RESOURCE_EXHAUSTED,
                    pending, command.requesterId(), now, "CPF_SENSITIVE_ACCESS_RESOURCE_EXHAUSTED");
            return result(AccessStatus.RESOURCE_EXHAUSTED, null,
                    "CPF_SENSITIVE_ACCESS_RESOURCE_EXHAUSTED");
        }
        if (created.created()) {
            if (!audit("REQUEST", AccessStatus.PENDING, pending, command.requesterId(), now, null)) {
                return result(AccessStatus.UNKNOWN_RESULT, pending, "CPF_SENSITIVE_ACCESS_AUDIT_FAILED_AFTER_REQUEST");
            }
            return result(AccessStatus.PENDING, pending, null);
        }
        AccessGrant existing = created.existing();
        if (existing != null && constantTimeEquals(existing.immutableHash(), command.immutableHash())) {
            AccessResult effective = expireIfNeeded(existing, now);
            if (effective.status() == AccessStatus.AUDIT_UNAVAILABLE
                    || effective.status() == AccessStatus.UNKNOWN_RESULT) return effective;
            if (!audit("REQUEST_REPLAY", effective.grant().status(), effective.grant(),
                    command.requesterId(), now, null)) {
                return result(AccessStatus.UNKNOWN_RESULT, effective.grant(),
                        "CPF_SENSITIVE_ACCESS_AUDIT_FAILED_AFTER_REQUEST_REPLAY");
            }
            return result(effective.grant().status(), effective.grant(), null);
        }
        return result(AccessStatus.IDEMPOTENCY_CONFLICT, existing,
                "CPF_SENSITIVE_ACCESS_IDEMPOTENCY_CONFLICT");
    }

    @Override
    public AccessResult approve(AccessApprovalCommand command) {
        Objects.requireNonNull(command, "command");
        Instant now = clock.instant();
        AccessResult lookup = lookup(command.requestId());
        if (lookup.status() == AccessStatus.UNKNOWN_RESULT) return lookup;
        AccessGrant current = lookup.grant();
        if (current == null) return result(AccessStatus.NOT_FOUND, null, "CPF_SENSITIVE_ACCESS_NOT_FOUND");
        if (current.version() == command.expectedVersion() + 1L
                && current.status() == AccessStatus.APPROVED
                && Objects.equals(current.approverId(), command.approverId())) {
            return recoveredReplay("APPROVE_REPLAY", current, command.approverId(), now);
        }
        if (current.version() != command.expectedVersion()) {
            return result(AccessStatus.VERSION_CONFLICT, current, "CPF_SENSITIVE_ACCESS_VERSION_CONFLICT");
        }
        if (current.requesterId().equals(command.approverId())) {
            return result(AccessStatus.SEPARATION_OF_DUTIES, current, "CPF_SENSITIVE_ACCESS_SOD_VIOLATION");
        }
        if (current.status() != AccessStatus.PENDING) {
            return result(AccessStatus.INVALID_STATE, current, "CPF_SENSITIVE_ACCESS_INVALID_STATE");
        }
        if (!auditSink.available()
                || !audit("APPROVE_AUTHORIZED", AccessStatus.PENDING, current,
                command.approverId(), now, null)) {
            return result(AccessStatus.AUDIT_UNAVAILABLE, current,
                    "CPF_SENSITIVE_ACCESS_AUDIT_UNAVAILABLE");
        }
        Instant expiresAt;
        try {
            expiresAt = now.plus(command.validFor());
        } catch (RuntimeException overflow) {
            return result(AccessStatus.INVALID_STATE, current, "CPF_SENSITIVE_ACCESS_EXPIRY_OVERFLOW");
        }
        AccessGrant approved = new AccessGrant(
                current.requestId(), current.version() + 1L, AccessStatus.APPROVED,
                current.requesterId(), current.resourceType(), current.resourceIdHash(), current.dataScope(),
                current.reason(), command.approverId(), current.requestedAt(), now, expiresAt, null,
                current.immutableHash());
        final boolean changed;
        try {
            changed = store.compareAndSet(current.requestId(), current.version(), approved);
        } catch (RuntimeException storeFailure) {
            return result(AccessStatus.UNKNOWN_RESULT, current, "CPF_SENSITIVE_ACCESS_STORE_UNAVAILABLE");
        }
        if (!changed) {
            return conflictAfterCas(current);
        }
        if (!audit("APPROVE", AccessStatus.APPROVED, approved, command.approverId(), now, null)) {
            return result(AccessStatus.UNKNOWN_RESULT, approved,
                    "CPF_SENSITIVE_ACCESS_AUDIT_FAILED_AFTER_APPROVE");
        }
        return result(AccessStatus.APPROVED, approved, null);
    }

    @Override
    public AccessResult reject(AccessRejectionCommand command) {
        Objects.requireNonNull(command, "command");
        Instant now = clock.instant();
        AccessResult lookup = lookup(command.requestId());
        if (lookup.status() == AccessStatus.UNKNOWN_RESULT) return lookup;
        AccessGrant current = lookup.grant();
        if (current == null) return result(AccessStatus.NOT_FOUND, null, "CPF_SENSITIVE_ACCESS_NOT_FOUND");
        if (current.version() == command.expectedVersion() + 1L
                && current.status() == AccessStatus.REJECTED
                && Objects.equals(current.approverId(), command.approverId())) {
            return recoveredReplay("REJECT_REPLAY", current, command.approverId(), now);
        }
        if (current.version() != command.expectedVersion()) {
            return result(AccessStatus.VERSION_CONFLICT, current, "CPF_SENSITIVE_ACCESS_VERSION_CONFLICT");
        }
        if (current.requesterId().equals(command.approverId())) {
            return result(AccessStatus.SEPARATION_OF_DUTIES, current, "CPF_SENSITIVE_ACCESS_SOD_VIOLATION");
        }
        if (current.status() != AccessStatus.PENDING) {
            return result(AccessStatus.INVALID_STATE, current, "CPF_SENSITIVE_ACCESS_INVALID_STATE");
        }
        if (!auditSink.available()
                || !audit("REJECT_AUTHORIZED", AccessStatus.PENDING, current,
                command.approverId(), now, null)) {
            return result(AccessStatus.AUDIT_UNAVAILABLE, current,
                    "CPF_SENSITIVE_ACCESS_AUDIT_UNAVAILABLE");
        }
        AccessGrant rejected = new AccessGrant(
                current.requestId(), current.version() + 1L, AccessStatus.REJECTED,
                current.requesterId(), current.resourceType(), current.resourceIdHash(), current.dataScope(),
                current.reason(), command.approverId(), current.requestedAt(), now, null, null,
                current.immutableHash());
        final boolean changed;
        try {
            changed = store.compareAndSet(current.requestId(), current.version(), rejected);
        } catch (RuntimeException storeFailure) {
            return result(AccessStatus.UNKNOWN_RESULT, current, "CPF_SENSITIVE_ACCESS_STORE_UNAVAILABLE");
        }
        if (!changed) return conflictAfterCas(current);
        if (!audit("REJECT", AccessStatus.REJECTED, rejected, command.approverId(), now, null)) {
            return result(AccessStatus.UNKNOWN_RESULT, rejected,
                    "CPF_SENSITIVE_ACCESS_AUDIT_FAILED_AFTER_REJECT");
        }
        return result(AccessStatus.REJECTED, rejected, null);
    }

    @Override
    public AccessResult consume(AccessConsumeCommand command) {
        Objects.requireNonNull(command, "command");
        Instant now = clock.instant();
        AccessResult lookup = lookup(command.requestId());
        if (lookup.status() == AccessStatus.UNKNOWN_RESULT) return lookup;
        AccessGrant current = lookup.grant();
        if (current == null) return result(AccessStatus.NOT_FOUND, null, "CPF_SENSITIVE_ACCESS_NOT_FOUND");
        if (current.version() == command.expectedVersion() + 1L
                && current.status() == AccessStatus.CONSUMED
                && sameConsumeScope(current, command)) {
            return recoveredReplay("CONSUME_REPLAY", current, command.accessorId(), now);
        }
        if (current.version() != command.expectedVersion()) {
            return result(AccessStatus.VERSION_CONFLICT, current, "CPF_SENSITIVE_ACCESS_VERSION_CONFLICT");
        }
        AccessResult expiry = expireIfNeeded(current, now);
        if (expiry.status() == AccessStatus.EXPIRED
                || expiry.status() == AccessStatus.AUDIT_UNAVAILABLE
                || expiry.status() == AccessStatus.UNKNOWN_RESULT) return expiry;
        current = expiry.grant();
        if (current.status() != AccessStatus.APPROVED) {
            return result(AccessStatus.INVALID_STATE, current, "CPF_SENSITIVE_ACCESS_INVALID_STATE");
        }
        if (!current.requesterId().equals(command.accessorId())) {
            return result(AccessStatus.ACCESSOR_MISMATCH, current, "CPF_SENSITIVE_ACCESS_ACCESSOR_MISMATCH");
        }
        if (!sameConsumeScope(current, command)) {
            return result(AccessStatus.SCOPE_MISMATCH, current, "CPF_SENSITIVE_ACCESS_SCOPE_MISMATCH");
        }
        if (!auditSink.available()
                || !audit("CONSUME_AUTHORIZED", AccessStatus.APPROVED, current,
                command.accessorId(), now, null)) {
            return result(AccessStatus.AUDIT_UNAVAILABLE, current,
                    "CPF_SENSITIVE_ACCESS_AUDIT_UNAVAILABLE");
        }
        AccessGrant consumed = new AccessGrant(
                current.requestId(), current.version() + 1L, AccessStatus.CONSUMED,
                current.requesterId(), current.resourceType(), current.resourceIdHash(), current.dataScope(),
                current.reason(), current.approverId(), current.requestedAt(), current.approvedAt(),
                current.expiresAt(), now, current.immutableHash());
        final boolean changed;
        try {
            changed = store.compareAndSet(current.requestId(), current.version(), consumed);
        } catch (RuntimeException storeFailure) {
            return result(AccessStatus.UNKNOWN_RESULT, current, "CPF_SENSITIVE_ACCESS_STORE_UNAVAILABLE");
        }
        if (!changed) return conflictAfterCas(current);
        if (!audit("CONSUME", AccessStatus.CONSUMED, consumed, command.accessorId(), now, null)) {
            return result(AccessStatus.UNKNOWN_RESULT, consumed,
                    "CPF_SENSITIVE_ACCESS_AUDIT_FAILED_AFTER_CONSUME");
        }
        return result(AccessStatus.CONSUMED, consumed, null);
    }

    @Override
    public AccessResult find(String requestId) {
        AccessResult lookup = lookup(CpfSensitiveDataAccessOperations.text(requestId, 128, "requestId"));
        if (lookup.status() == AccessStatus.UNKNOWN_RESULT || lookup.grant() == null) return lookup;
        return expireIfNeeded(lookup.grant(), clock.instant());
    }

    private AccessResult lookup(String requestId) {
        try {
            AccessGrant current = store.find(requestId).orElse(null);
            return current == null
                    ? result(AccessStatus.NOT_FOUND, null, "CPF_SENSITIVE_ACCESS_NOT_FOUND")
                    : result(current.status(), current, null);
        } catch (RuntimeException storeFailure) {
            return result(AccessStatus.UNKNOWN_RESULT, null, "CPF_SENSITIVE_ACCESS_STORE_UNAVAILABLE");
        }
    }

    private AccessResult conflictAfterCas(AccessGrant fallback) {
        AccessResult latest = lookup(fallback.requestId());
        return latest.status() == AccessStatus.UNKNOWN_RESULT
                ? latest
                : result(AccessStatus.VERSION_CONFLICT,
                latest.grant() == null ? fallback : latest.grant(),
                "CPF_SENSITIVE_ACCESS_VERSION_CONFLICT");
    }

    private AccessResult expireIfNeeded(AccessGrant current, Instant now) {
        if (current.status() != AccessStatus.APPROVED || current.activeAt(now)) {
            return result(current.status(), current, null);
        }
        if (!auditSink.available()
                || !audit("EXPIRE_AUTHORIZED", AccessStatus.APPROVED, current, "SYSTEM", now, null)) {
            return result(AccessStatus.AUDIT_UNAVAILABLE, current,
                    "CPF_SENSITIVE_ACCESS_AUDIT_UNAVAILABLE");
        }
        AccessGrant expired = new AccessGrant(
                current.requestId(), current.version() + 1L, AccessStatus.EXPIRED,
                current.requesterId(), current.resourceType(), current.resourceIdHash(), current.dataScope(),
                current.reason(), current.approverId(), current.requestedAt(), current.approvedAt(),
                current.expiresAt(), current.consumedAt(), current.immutableHash());
        final boolean changed;
        try {
            changed = store.compareAndSet(current.requestId(), current.version(), expired);
        } catch (RuntimeException storeFailure) {
            return result(AccessStatus.UNKNOWN_RESULT, current, "CPF_SENSITIVE_ACCESS_STORE_UNAVAILABLE");
        }
        if (changed) {
            if (!audit("EXPIRE", AccessStatus.EXPIRED, expired, "SYSTEM", now, null)) {
                return result(AccessStatus.UNKNOWN_RESULT, expired,
                        "CPF_SENSITIVE_ACCESS_AUDIT_FAILED_AFTER_EXPIRE");
            }
            return result(AccessStatus.EXPIRED, expired, null);
        }
        return conflictAfterCas(current);
    }

    private AccessResult recoveredReplay(String action, AccessGrant current, String actor, Instant now) {
        if (!auditSink.available() || !audit(action, current.status(), current, actor, now, null)) {
            return result(AccessStatus.AUDIT_UNAVAILABLE, current,
                    "CPF_SENSITIVE_ACCESS_AUDIT_RECOVERY_FAILED");
        }
        return result(AccessStatus.IDEMPOTENT_REPLAY, current, null);
    }

    private boolean audit(
            String action, AccessStatus status, AccessGrant grant, String actor, Instant at, String error) {
        try {
            auditSink.record(action, status, grant, actor, at, error);
            return true;
        } catch (RuntimeException auditFailure) {
            return false;
        }
    }

    private static boolean sameConsumeScope(AccessGrant current, AccessConsumeCommand command) {
        return current.requesterId().equals(command.accessorId())
                && current.resourceType().equals(command.resourceType())
                && constantTimeEquals(current.resourceIdHash(), command.resourceIdHash())
                && current.dataScope().equals(command.dataScope());
    }

    private static AccessResult result(AccessStatus status, AccessGrant grant, String errorCode) {
        return new AccessResult(status, grant, errorCode);
    }

    private static boolean constantTimeEquals(String left, String right) {
        if (left == null || right == null) return false;
        return java.security.MessageDigest.isEqual(
                left.getBytes(java.nio.charset.StandardCharsets.US_ASCII),
                right.getBytes(java.nio.charset.StandardCharsets.US_ASCII));
    }
}

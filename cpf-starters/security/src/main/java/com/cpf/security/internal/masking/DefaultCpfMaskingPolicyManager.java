package com.cpf.security.internal.masking;

import com.cpf.security.api.CpfMaskingPolicyApproval;
import com.cpf.security.api.CpfMaskingPolicyAuditEvent;
import com.cpf.security.api.CpfMaskingPolicyOperations;
import com.cpf.security.api.CpfMaskingPolicyResult;
import com.cpf.security.api.CpfMaskingPolicyRollbackCommand;
import com.cpf.security.api.CpfMaskingPolicyRuntimeStatus;
import com.cpf.security.api.CpfMaskingPolicySnapshot;
import com.cpf.security.api.CpfMaskingPolicyUpdateCommand;
import com.cpf.security.api.CpfMaskingRuntime;
import com.cpf.security.spi.CpfMaskingPolicyAuditSink;
import com.cpf.security.spi.CpfMaskingPolicyStore;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/** Audited, optimistic masking policy control plane backed by a replaceable durable store. */
public final class DefaultCpfMaskingPolicyManager implements CpfMaskingPolicyOperations {
    private final CpfMaskingPolicyStore store;
    private final CpfMaskingPolicyAuditSink auditSink;
    private final Clock clock;
    private final AtomicLong rejectedCommands = new AtomicLong();
    private final AtomicLong auditFailures = new AtomicLong();

    public DefaultCpfMaskingPolicyManager(CpfMaskingPolicyStore store,
            CpfMaskingPolicyAuditSink auditSink, Clock clock) {
        this.store = Objects.requireNonNull(store, "store");
        this.auditSink = Objects.requireNonNull(auditSink, "auditSink");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override public CpfMaskingPolicySnapshot current() {
        try {
            return store.current().orElseThrow(() -> new IllegalStateException("masking policy is not initialized"));
        } catch (RuntimeException failure) {
            throw new IllegalStateException("masking policy store is unavailable", failure);
        }
    }

    @Override public List<CpfMaskingPolicySnapshot> history(int limit) {
        if (limit < 1 || limit > 1_000) throw new IllegalArgumentException("limit must be between 1 and 1000");
        try {
            return List.copyOf(store.history(limit));
        } catch (RuntimeException failure) {
            throw new IllegalStateException("masking policy store is unavailable", failure);
        }
    }

    @Override public synchronized CpfMaskingPolicyResult update(CpfMaskingPolicyUpdateCommand command) {
        Objects.requireNonNull(command, "command");
        Instant now = clock.instant();
        CpfMaskingPolicySnapshot before = safeCurrent();
        CpfMaskingPolicyResult approvalFailure = validateApproval(command.commandHash(), command.actor(),
                command.approval(), before, now);
        if (approvalFailure != null) return approvalFailure;
        CpfMaskingPolicySnapshot next = new CpfMaskingPolicySnapshot(
                command.expectedVersion() + 1L, command.sensitiveKeys(), command.maxLength(),
                command.maskBearerToken(), now, command.actor(), command.reason());
        return apply(command.commandId(), command.commandHash(), command.expectedVersion(),
                command.actor(), command.reason(), command.approval(), before, next);
    }

    @Override public synchronized CpfMaskingPolicyResult rollback(CpfMaskingPolicyRollbackCommand command) {
        Objects.requireNonNull(command, "command");
        Instant now = clock.instant();
        CpfMaskingPolicySnapshot before = safeCurrent();
        CpfMaskingPolicyResult approvalFailure = validateApproval(command.commandHash(), command.actor(),
                command.approval(), before, now);
        if (approvalFailure != null) return approvalFailure;
        final CpfMaskingPolicySnapshot target;
        try {
            target = store.findVersion(command.targetVersion()).orElse(null);
        } catch (RuntimeException failure) {
            return result(CpfMaskingPolicyResult.Status.STORE_UNAVAILABLE, before,
                    "masking policy store is unavailable");
        }
        if (target == null) {
            rejectedCommands.incrementAndGet();
            return result(CpfMaskingPolicyResult.Status.TARGET_VERSION_NOT_FOUND, before,
                    "target masking policy version was not found");
        }
        CpfMaskingPolicySnapshot next = new CpfMaskingPolicySnapshot(
                command.expectedVersion() + 1L, target.sensitiveKeys(), target.maxLength(),
                target.maskBearerToken(), now, command.actor(), command.reason());
        return apply(command.commandId(), command.commandHash(), command.expectedVersion(),
                command.actor(), command.reason(), command.approval(), before, next);
    }

    @Override public CpfMaskingPolicyRuntimeStatus runtimeStatus() {
        try {
            CpfMaskingPolicyRuntimeStatus base = store.runtimeStatus();
            long rejected = base.rejectedCommandCount() + rejectedCommands.get();
            long audit = base.auditFailureCount() + auditFailures.get();
            CpfMaskingPolicyRuntimeStatus.Health health = base.health();
            if (audit > 0L && health == CpfMaskingPolicyRuntimeStatus.Health.UP) {
                health = CpfMaskingPolicyRuntimeStatus.Health.DEGRADED;
            }
            return new CpfMaskingPolicyRuntimeStatus(health, base.activeVersion(), base.historySize(),
                    base.commandRecordCount(), base.maximumHistory(), base.maximumCommandRecords(),
                    rejected, audit, clock.instant());
        } catch (RuntimeException failure) {
            CpfMaskingPolicySnapshot policy = safeCurrent();
            return new CpfMaskingPolicyRuntimeStatus(CpfMaskingPolicyRuntimeStatus.Health.DOWN,
                    policy.version(), 0, 0, 1, 1, rejectedCommands.get(),
                    auditFailures.get(), clock.instant());
        }
    }

    private CpfMaskingPolicyResult apply(String commandId, String commandHash, long expectedVersion,
            String actor, String reason, CpfMaskingPolicyApproval approval,
            CpfMaskingPolicySnapshot before, CpfMaskingPolicySnapshot next) {
        Instant now = clock.instant();
        if (!audit(new CpfMaskingPolicyAuditEvent(CpfMaskingPolicyAuditEvent.Phase.PREPARE,
                hash(commandId), commandHash, hash(actor), hash(approval.approver()),
                before.version(), next.version(), reason, "AUTHORIZED", now))) {
            return result(CpfMaskingPolicyResult.Status.AUDIT_UNAVAILABLE, before,
                    "masking policy audit is unavailable");
        }
        final CpfMaskingPolicyStore.WriteResult write;
        try {
            write = store.compareAndSet(expectedVersion, commandId, commandHash, next);
        } catch (RuntimeException failure) {
            return result(CpfMaskingPolicyResult.Status.STORE_UNAVAILABLE, before,
                    "masking policy store is unavailable");
        }
        if (write.status() == CpfMaskingPolicyStore.Status.UNKNOWN) {
            CpfMaskingPolicySnapshot uncertain = write.snapshot() == null ? next : write.snapshot();
            auditUnknown(commandId, commandHash, actor, approval.approver(), before, uncertain, reason,
                    "durable commit outcome is unknown");
            return result(CpfMaskingPolicyResult.Status.UNKNOWN_RESULT, uncertain,
                    "durable masking policy commit outcome is unknown");
        }
        CpfMaskingPolicyResult mapped = mapWriteResult(write);
        if (write.status() != CpfMaskingPolicyStore.Status.APPLIED) return mapped;

        CpfMaskingRuntime.PolicyUpdateResult runtimeUpdate;
        try {
            runtimeUpdate = CpfMaskingRuntime.compareAndSetPolicy(expectedVersion,
                    next.sensitiveKeys(), next.maxLength(), next.maskBearerToken(),
                    next.updatedAt(), commandHash);
        } catch (RuntimeException failure) {
            auditUnknown(commandId, commandHash, actor, approval.approver(), before, next, reason,
                    "runtime policy update failed");
            return result(CpfMaskingPolicyResult.Status.UNKNOWN_RESULT, next,
                    "policy persisted but runtime update outcome is unknown");
        }
        if (!runtimeUpdate.applied()) {
            auditUnknown(commandId, commandHash, actor, approval.approver(), before, next, reason,
                    "runtime version diverged");
            return result(CpfMaskingPolicyResult.Status.UNKNOWN_RESULT, next,
                    "policy persisted but runtime version diverged");
        }
        if (!audit(new CpfMaskingPolicyAuditEvent(CpfMaskingPolicyAuditEvent.Phase.APPLIED,
                hash(commandId), commandHash, hash(actor), hash(approval.approver()),
                before.version(), next.version(), reason, "APPLIED", clock.instant()))) {
            return result(CpfMaskingPolicyResult.Status.UNKNOWN_RESULT, next,
                    "policy applied but completion audit failed");
        }
        return result(CpfMaskingPolicyResult.Status.APPLIED, next, "");
    }

    private CpfMaskingPolicyResult validateApproval(String commandHash, String actor,
            CpfMaskingPolicyApproval approval, CpfMaskingPolicySnapshot current, Instant now) {
        if (approval == null || !MessageDigest.isEqual(
                commandHash.getBytes(StandardCharsets.US_ASCII),
                approval.commandHash().getBytes(StandardCharsets.US_ASCII))
                || approval.approver().equals(actor)
                || now.isBefore(approval.approvedAt()) || !now.isBefore(approval.expiresAt())) {
            rejectedCommands.incrementAndGet();
            return result(CpfMaskingPolicyResult.Status.APPROVAL_REQUIRED, current,
                    "valid independent approval is required");
        }
        return null;
    }

    private CpfMaskingPolicyResult mapWriteResult(CpfMaskingPolicyStore.WriteResult write) {
        return switch (write.status()) {
            case APPLIED -> result(CpfMaskingPolicyResult.Status.APPLIED, write.snapshot(), "");
            case IDEMPOTENT_REPLAY -> result(CpfMaskingPolicyResult.Status.IDEMPOTENT_REPLAY,
                    write.snapshot(), "command was already applied");
            case VERSION_CONFLICT -> reject(CpfMaskingPolicyResult.Status.VERSION_CONFLICT,
                    write.snapshot(), "masking policy version conflict");
            case COMMAND_CONFLICT -> reject(CpfMaskingPolicyResult.Status.COMMAND_CONFLICT,
                    write.snapshot(), "command id was reused with different content");
            case RESOURCE_EXHAUSTED -> reject(CpfMaskingPolicyResult.Status.RESOURCE_EXHAUSTED,
                    write.snapshot(), "masking policy command capacity is exhausted");
            case UNKNOWN -> result(CpfMaskingPolicyResult.Status.UNKNOWN_RESULT,
                    write.snapshot(), "durable masking policy commit outcome is unknown");
        };
    }

    private CpfMaskingPolicyResult reject(CpfMaskingPolicyResult.Status status,
            CpfMaskingPolicySnapshot snapshot, String message) {
        rejectedCommands.incrementAndGet();
        return result(status, snapshot, message);
    }

    private void auditUnknown(String commandId, String commandHash, String actor, String approver,
            CpfMaskingPolicySnapshot before, CpfMaskingPolicySnapshot after,
            String reason, String result) {
        audit(new CpfMaskingPolicyAuditEvent(CpfMaskingPolicyAuditEvent.Phase.UNKNOWN,
                hash(commandId), commandHash, hash(actor), hash(approver), before.version(),
                after.version(), reason, result, clock.instant()));
    }

    private boolean audit(CpfMaskingPolicyAuditEvent event) {
        try {
            auditSink.record(event);
            return true;
        } catch (RuntimeException failure) {
            auditFailures.incrementAndGet();
            return false;
        }
    }

    private CpfMaskingPolicySnapshot safeCurrent() {
        try {
            return store.current().orElseGet(DefaultCpfMaskingPolicyManager::runtimeSnapshot);
        } catch (RuntimeException failure) {
            return runtimeSnapshot();
        }
    }

    private static CpfMaskingPolicySnapshot runtimeSnapshot() {
        CpfMaskingRuntime.MaskingPolicy policy = CpfMaskingRuntime.currentPolicy();
        return new CpfMaskingPolicySnapshot(policy.version(), policy.sensitiveKeys(), policy.maxLength(),
                policy.maskBearerToken(), policy.updatedAt(), "SYSTEM", "runtime masking policy");
    }

    private static CpfMaskingPolicyResult result(CpfMaskingPolicyResult.Status status,
            CpfMaskingPolicySnapshot snapshot, String message) {
        return new CpfMaskingPolicyResult(status, snapshot, message);
    }

    private static String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 unavailable", impossible);
        }
    }
}

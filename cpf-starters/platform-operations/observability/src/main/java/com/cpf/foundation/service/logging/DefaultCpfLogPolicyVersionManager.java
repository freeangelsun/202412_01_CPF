package com.cpf.foundation.service.logging;

import com.cpf.platform.operations.observability.api.logging.policy.CpfLogPolicyVersionApproval;
import com.cpf.platform.operations.observability.api.logging.policy.CpfLogPolicyVersionAuditEvent;
import com.cpf.platform.operations.observability.api.logging.policy.CpfLogPolicyVersionOperations;
import com.cpf.platform.operations.observability.api.logging.policy.CpfLogPolicyVersionReconcileCommand;
import com.cpf.platform.operations.observability.api.logging.policy.CpfLogPolicyVersionResult;
import com.cpf.platform.operations.observability.api.logging.policy.CpfLogPolicyVersionRollbackCommand;
import com.cpf.platform.operations.observability.api.logging.policy.CpfLogPolicyVersionRuntimeStatus;
import com.cpf.platform.operations.observability.api.logging.policy.CpfLogPolicyVersionSnapshot;
import com.cpf.platform.operations.observability.api.logging.policy.CpfLogPolicyVersionUpdateCommand;
import com.cpf.platform.operations.observability.api.logging.policy.LogPolicyTargetType;
import com.cpf.security.api.CpfSensitiveData;
import com.cpf.platform.operations.observability.spi.logging.CpfLogPolicyVersionApplier;
import com.cpf.platform.operations.observability.spi.logging.CpfLogPolicyVersionAuditSink;
import com.cpf.platform.operations.observability.spi.logging.CpfLogPolicyVersionStore;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/** Approval-bound optimistic control plane with explicit UNKNOWN reconciliation. */
public final class DefaultCpfLogPolicyVersionManager implements CpfLogPolicyVersionOperations {
    private final CpfLogPolicyVersionStore store;
    private final CpfLogPolicyVersionAuditSink audit;
    private final CpfLogPolicyVersionApplier applier;
    private final Clock clock;
    private final AtomicLong auditFailures = new AtomicLong();
    private final AtomicLong applyFailures = new AtomicLong();
    private final AtomicLong unknownResults = new AtomicLong();

    public DefaultCpfLogPolicyVersionManager(CpfLogPolicyVersionStore store,
            CpfLogPolicyVersionAuditSink audit, CpfLogPolicyVersionApplier applier, Clock clock) {
        this.store = Objects.requireNonNull(store, "store");
        this.audit = Objects.requireNonNull(audit, "audit");
        this.applier = Objects.requireNonNull(applier, "applier");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override public CpfLogPolicyVersionSnapshot current(LogPolicyTargetType type, String targetId) {
        try {
            return store.current(type, targetId).orElseGet(() -> store.ensureBaseline(
                    applier.baseline(type, targetId, clock.instant())));
        } catch (RuntimeException failure) {
            throw new IllegalStateException("log policy current state unavailable", failure);
        }
    }
    @Override public List<CpfLogPolicyVersionSnapshot> history(
            LogPolicyTargetType type, String targetId, int limit) {
        current(type, targetId);
        return store.history(type, targetId, Math.max(1, Math.min(limit, 1_000)));
    }

    @Override public CpfLogPolicyVersionResult update(CpfLogPolicyVersionUpdateCommand command) {
        Objects.requireNonNull(command, "command");
        String hash = command.commandHash();
        CpfLogPolicyVersionResult approvalFailure = validateApproval(hash, command.actor(), command.approval());
        if (approvalFailure != null) return approvalFailure;
        CpfLogPolicyVersionSnapshot before = current(command.targetType(), command.targetId());
        if (before.status() != CpfLogPolicyVersionSnapshot.Status.ACTIVE) {
            return result(CpfLogPolicyVersionResult.Status.RECONCILIATION_REQUIRED, before,
                    "current version must be reconciled before mutation");
        }
        CpfLogPolicyVersionSnapshot draft = new CpfLogPolicyVersionSnapshot(command.targetType(), command.targetId(),
                command.expectedVersion() + 1L, CpfLogPolicyVersionSnapshot.Status.DRAFT,
                command.decision().withSource("MANAGED_VERSION:" + (command.expectedVersion() + 1L)),
                clock.instant(), command.actor(), command.reason());
        return commit(command.commandId(), hash, command.actor(), command.reason(), command.approval(), before, draft);
    }

    @Override public CpfLogPolicyVersionResult rollback(CpfLogPolicyVersionRollbackCommand command) {
        Objects.requireNonNull(command, "command");
        String hash = command.commandHash();
        CpfLogPolicyVersionResult approvalFailure = validateApproval(hash, command.actor(), command.approval());
        if (approvalFailure != null) return approvalFailure;
        CpfLogPolicyVersionSnapshot before = current(command.targetType(), command.targetId());
        if (before.status() != CpfLogPolicyVersionSnapshot.Status.ACTIVE) {
            return result(CpfLogPolicyVersionResult.Status.RECONCILIATION_REQUIRED, before,
                    "current version must be reconciled before rollback");
        }
        CpfLogPolicyVersionSnapshot target;
        try { target = store.findVersion(command.targetType(), command.targetId(), command.targetVersion()).orElse(null); }
        catch (RuntimeException failure) { return result(CpfLogPolicyVersionResult.Status.STORE_UNAVAILABLE, before, "store unavailable"); }
        if (target == null) return result(CpfLogPolicyVersionResult.Status.TARGET_VERSION_NOT_FOUND, before, "target version not found");
        CpfLogPolicyVersionSnapshot draft = new CpfLogPolicyVersionSnapshot(command.targetType(), command.targetId(),
                command.expectedVersion() + 1L, CpfLogPolicyVersionSnapshot.Status.DRAFT,
                target.decision().withSource("ROLLBACK_FROM:" + target.version()), clock.instant(),
                command.actor(), command.reason());
        return commit(command.commandId(), hash, command.actor(), command.reason(), command.approval(), before, draft);
    }

    @Override public CpfLogPolicyVersionResult reconcile(CpfLogPolicyVersionReconcileCommand command) {
        Objects.requireNonNull(command, "command");
        String hash = command.commandHash();
        CpfLogPolicyVersionResult approvalFailure = validateApproval(hash, command.actor(), command.approval());
        if (approvalFailure != null) return approvalFailure;
        CpfLogPolicyVersionSnapshot current = current(command.targetType(), command.targetId());
        if (current.version() != command.expectedVersion()) {
            return result(CpfLogPolicyVersionResult.Status.VERSION_CONFLICT, current, "version conflict");
        }
        if (current.status() == CpfLogPolicyVersionSnapshot.Status.ACTIVE) {
            return result(CpfLogPolicyVersionResult.Status.IDEMPOTENT_REPLAY, current, "already active");
        }
        CpfLogPolicyVersionAuditEvent prepare = event(CpfLogPolicyVersionAuditEvent.Phase.PREPARE,
                command.commandId(), hash, command.actor(), command.approval(), current, current,
                command.reason(), "RECONCILE_PREPARE");
        try { audit.record(prepare); }
        catch (RuntimeException failure) {
            auditFailures.incrementAndGet();
            return result(CpfLogPolicyVersionResult.Status.AUDIT_UNAVAILABLE, current, "pre-reconcile audit unavailable");
        }
        try { applier.apply(current); }
        catch (RuntimeException failure) {
            applyFailures.incrementAndGet(); unknownResults.incrementAndGet();
            mark(current, CpfLogPolicyVersionSnapshot.Status.UNKNOWN, command.actor(), command.reason());
            return result(CpfLogPolicyVersionResult.Status.UNKNOWN_RESULT, current, "runtime re-apply failed");
        }
        CpfLogPolicyVersionSnapshot active = mark(current, CpfLogPolicyVersionSnapshot.Status.ACTIVE,
                command.actor(), command.reason());
        if (active == null) {
            unknownResults.incrementAndGet();
            return result(CpfLogPolicyVersionResult.Status.UNKNOWN_RESULT, current, "status promotion outcome is unknown");
        }
        try { audit.record(event(CpfLogPolicyVersionAuditEvent.Phase.APPLIED, command.commandId(), hash,
                command.actor(), command.approval(), current, active, command.reason(), "RECONCILED")); }
        catch (RuntimeException failure) {
            auditFailures.incrementAndGet(); unknownResults.incrementAndGet();
            return result(CpfLogPolicyVersionResult.Status.UNKNOWN_RESULT, active, "reconcile audit unavailable");
        }
        return result(CpfLogPolicyVersionResult.Status.APPLIED, active, "RECONCILED");
    }

    @Override public CpfLogPolicyVersionRuntimeStatus runtimeStatus() {
        CpfLogPolicyVersionRuntimeStatus source;
        try { source = store.runtimeStatus(); }
        catch (RuntimeException failure) {
            return new CpfLogPolicyVersionRuntimeStatus(CpfLogPolicyVersionRuntimeStatus.Health.DOWN,
                    0, 0, 0, 1, 2, 16, 0L, unknownResults.get(), auditFailures.get(), applyFailures.get(), clock.instant());
        }
        long auditCount = auditFailures.get();
        long applyCount = applyFailures.get();
        long unknownCount = Math.max(source.unknownResultCount(), unknownResults.get());
        CpfLogPolicyVersionRuntimeStatus.Health health = source.health();
        if (health == CpfLogPolicyVersionRuntimeStatus.Health.UP
                && (auditCount > 0 || applyCount > 0 || unknownCount > 0)) {
            health = CpfLogPolicyVersionRuntimeStatus.Health.DEGRADED;
        }
        return new CpfLogPolicyVersionRuntimeStatus(health, source.targetCount(), source.versionCount(),
                source.commandCount(), source.maximumTargets(), source.maximumHistoryPerTarget(),
                source.maximumCommandRecords(), source.rejectedCommandCount(), unknownCount,
                auditCount, applyCount, clock.instant());
    }

    private CpfLogPolicyVersionResult commit(String commandId, String hash, String actor, String reason,
            CpfLogPolicyVersionApproval approval, CpfLogPolicyVersionSnapshot before,
            CpfLogPolicyVersionSnapshot draft) {
        try { audit.record(event(CpfLogPolicyVersionAuditEvent.Phase.PREPARE, commandId, hash,
                actor, approval, before, draft, reason, "PREPARE")); }
        catch (RuntimeException failure) {
            auditFailures.incrementAndGet();
            return result(CpfLogPolicyVersionResult.Status.AUDIT_UNAVAILABLE, before, "pre-commit audit unavailable");
        }
        CpfLogPolicyVersionStore.WriteResult write;
        try { write = store.compareAndSet(before.version(), commandId, hash, draft); }
        catch (RuntimeException failure) { return result(CpfLogPolicyVersionResult.Status.STORE_UNAVAILABLE, before, "store unavailable"); }
        CpfLogPolicyVersionResult.Status mapped = map(write.status());
        CpfLogPolicyVersionSnapshot committed = write.snapshot();
        if (mapped == CpfLogPolicyVersionResult.Status.UNKNOWN_RESULT) {
            unknownResults.incrementAndGet();
            return result(mapped, committed, "store commit outcome is unknown");
        }
        if (mapped == CpfLogPolicyVersionResult.Status.IDEMPOTENT_REPLAY) {
            return result(mapped, committed, mapped.name());
        }
        if (mapped != CpfLogPolicyVersionResult.Status.APPLIED) return result(mapped, committed, mapped.name());
        try { applier.apply(committed); }
        catch (RuntimeException failure) {
            applyFailures.incrementAndGet(); unknownResults.incrementAndGet();
            CpfLogPolicyVersionSnapshot unknown = mark(committed, CpfLogPolicyVersionSnapshot.Status.UNKNOWN, actor, reason);
            recordUnknown(commandId, hash, actor, approval, before, unknown == null ? committed : unknown, reason,
                    "RUNTIME_APPLY_FAILED");
            return result(CpfLogPolicyVersionResult.Status.UNKNOWN_RESULT,
                    unknown == null ? committed : unknown, "committed version requires reconciliation");
        }
        CpfLogPolicyVersionSnapshot active = mark(committed, CpfLogPolicyVersionSnapshot.Status.ACTIVE, actor, reason);
        if (active == null) {
            unknownResults.incrementAndGet();
            return result(CpfLogPolicyVersionResult.Status.UNKNOWN_RESULT, committed, "status promotion outcome is unknown");
        }
        try { audit.record(event(CpfLogPolicyVersionAuditEvent.Phase.APPLIED, commandId, hash,
                actor, approval, before, active, reason, "APPLIED")); }
        catch (RuntimeException failure) {
            auditFailures.incrementAndGet(); unknownResults.incrementAndGet();
            return result(CpfLogPolicyVersionResult.Status.UNKNOWN_RESULT, active,
                    "version applied but completion audit is unavailable");
        }
        return result(CpfLogPolicyVersionResult.Status.APPLIED, active, "APPLIED");
    }

    private CpfLogPolicyVersionSnapshot mark(CpfLogPolicyVersionSnapshot source,
            CpfLogPolicyVersionSnapshot.Status status, String actor, String reason) {
        try {
            CpfLogPolicyVersionStore.StatusResult changed = store.updateStatus(source.targetType(), source.targetId(),
                    source.version(), source.status(), status, actor, reason);
            return changed.updated() ? changed.snapshot() : null;
        } catch (RuntimeException failure) { return null; }
    }
    private void recordUnknown(String commandId, String hash, String actor,
            CpfLogPolicyVersionApproval approval, CpfLogPolicyVersionSnapshot before,
            CpfLogPolicyVersionSnapshot after, String reason, String result) {
        try { audit.record(event(CpfLogPolicyVersionAuditEvent.Phase.UNKNOWN, commandId, hash,
                actor, approval, before, after, reason, result)); }
        catch (RuntimeException ignored) { auditFailures.incrementAndGet(); }
    }
    private CpfLogPolicyVersionResult validateApproval(String hash, String actor,
            CpfLogPolicyVersionApproval approval) {
        if (approval == null) {
            return result(CpfLogPolicyVersionResult.Status.APPROVAL_REQUIRED, null,
                    "independent approval is required");
        }
        if (actor.equals(approval.approver())) {
            return result(CpfLogPolicyVersionResult.Status.APPROVAL_INVALID, null,
                    "self approval violates separation of duties");
        }
        Instant now = clock.instant();
        if (!MessageDigest.isEqual(hash.getBytes(StandardCharsets.US_ASCII),
                approval.commandHash().getBytes(StandardCharsets.US_ASCII))
                || approval.approvedAt().isAfter(now)
                || !approval.expiresAt().isAfter(now)) {
            return result(CpfLogPolicyVersionResult.Status.APPROVAL_INVALID, null,
                    "approval is invalid or expired");
        }
        return null;
    }
    private CpfLogPolicyVersionAuditEvent event(CpfLogPolicyVersionAuditEvent.Phase phase,
            String commandId, String hash, String actor, CpfLogPolicyVersionApproval approval,
            CpfLogPolicyVersionSnapshot before, CpfLogPolicyVersionSnapshot after,
            String reason, String result) {
        return new CpfLogPolicyVersionAuditEvent(phase, sha256(commandId), hash,
                sha256(after.targetType().code() + ':' + after.targetId()), sha256(actor),
                sha256(approval.approver()), before.version(), after.version(), before.status(),
                after.status(), CpfSensitiveData.sanitizeAuditReason(reason), result, clock.instant());
    }
    private static CpfLogPolicyVersionResult.Status map(CpfLogPolicyVersionStore.Status status) {
        return switch (status) {
            case APPLIED -> CpfLogPolicyVersionResult.Status.APPLIED;
            case IDEMPOTENT_REPLAY -> CpfLogPolicyVersionResult.Status.IDEMPOTENT_REPLAY;
            case VERSION_CONFLICT -> CpfLogPolicyVersionResult.Status.VERSION_CONFLICT;
            case COMMAND_CONFLICT -> CpfLogPolicyVersionResult.Status.COMMAND_CONFLICT;
            case RESOURCE_EXHAUSTED -> CpfLogPolicyVersionResult.Status.RESOURCE_EXHAUSTED;
            case UNKNOWN -> CpfLogPolicyVersionResult.Status.UNKNOWN_RESULT;
        };
    }
    private static CpfLogPolicyVersionResult result(CpfLogPolicyVersionResult.Status status,
            CpfLogPolicyVersionSnapshot snapshot, String message) {
        return new CpfLogPolicyVersionResult(status, snapshot, message);
    }
    private static String sha256(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (NoSuchAlgorithmException unavailable) { throw new IllegalStateException("SHA-256 unavailable", unavailable); }
    }
}

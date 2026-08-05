package com.cpf.core.internal.locking;

import com.cpf.core.api.locking.CpfLockManager;
import com.cpf.core.api.locking.CpfLockRuntimeStatus;
import com.cpf.core.common.logging.SensitiveDataMasker;
import com.cpf.core.spi.locking.CpfLockAuditSink;
import com.cpf.core.spi.locking.CpfLockStore;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Default lease state machine with optimistic CAS, owner epochs and fencing tokens. */
public final class DefaultCpfLockManager implements CpfLockManager, CpfLockRuntimeStatus {
    private static final Duration MIN_LEASE = Duration.ofMillis(100);
    private static final Duration MAX_LEASE = Duration.ofHours(24);
    private static final Duration MAX_FORCE_RELEASE_APPROVAL_WINDOW = Duration.ofMinutes(15);
    private final CpfLockStore store;
    private final CpfLockAuditSink audit;
    private final Clock clock;

    public DefaultCpfLockManager(CpfLockStore store, CpfLockAuditSink audit, Clock clock) {
        this.store = Objects.requireNonNull(store, "store");
        this.audit = audit == null ? CpfLockAuditSink.unavailable() : audit;
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public AcquireResult acquire(String key, String ownerId, String requestId, Duration leaseDuration) {
        String normalizedKey = normalized(key);
        String owner = normalized(ownerId);
        String request = normalized(requestId);
        Duration lease = validateLease(leaseDuration);
        if (normalizedKey == null || owner == null || request == null || lease == null) {
            return new AcquireResult(AcquireStatus.INVALID, null, null, "INVALID_ARGUMENT");
        }
        Instant now = clock.instant();
        final CpfLockStore.UpdateResult updated;
        try {
            updated = store.update(normalizedKey, current -> {
                if (current != null && current.state() == State.ACTIVE
                        && current.leaseUntil().isAfter(now)) {
                    return current;
                }
                long fence = store.nextFence(normalizedKey);
                long version = current == null ? 1L : nextVersion(current.rowVersion());
                return new CpfLockStore.StoredLock(
                        normalizedKey, owner, request, fence, fence, version, now,
                        now.plus(lease), State.ACTIVE, "ACQUIRE", null);
            });
        } catch (CpfLockStore.ResourceExhaustedException capacityFailure) {
            return new AcquireResult(AcquireStatus.RESOURCE_EXHAUSTED, null, null,
                    "LOCK_STORE_RESOURCE_EXHAUSTED");
        } catch (RuntimeException storageFailure) {
            return new AcquireResult(AcquireStatus.UNKNOWN, null, null, "LOCK_STORE_UNAVAILABLE");
        }
        if (updated == null || updated.after() == null) {
            return new AcquireResult(AcquireStatus.UNKNOWN, null, null, "LOCK_STORE_NO_RESULT");
        }
        CpfLockStore.StoredLock after = updated.after();
        if (after.ownerId().equals(owner) && after.requestId().equals(request)) {
            AcquireStatus status = Objects.equals(updated.before(), after)
                    ? AcquireStatus.IDEMPOTENT_REPLAY : AcquireStatus.ACQUIRED;
            return new AcquireResult(status, token(after), snapshot(after), status.name());
        }
        return new AcquireResult(AcquireStatus.BUSY, null, snapshot(after), "LOCK_HELD");
    }

    @Override
    public RenewResult renew(LockToken token, Duration leaseDuration) {
        Duration lease = validateLease(leaseDuration);
        if (!validToken(token) || lease == null) {
            return new RenewResult(RenewStatus.INVALID, null, "INVALID_ARGUMENT");
        }
        Instant now = clock.instant();
        final CpfLockStore.UpdateResult result;
        try {
            result = store.update(token.key(), current -> {
                if (current == null || current.state() != State.ACTIVE
                        || !current.leaseUntil().isAfter(now)) return current;
                if (!matches(current, token)) return current;
                return new CpfLockStore.StoredLock(
                        current.key(), current.ownerId(), current.requestId(),
                        current.fencingToken(), current.ownerEpoch(), nextVersion(current.rowVersion()),
                        current.acquiredAt(), now.plus(lease), State.ACTIVE,
                        "RENEW", current.lastAuditId());
            });
        } catch (RuntimeException storageFailure) {
            return new RenewResult(RenewStatus.UNKNOWN, null, "LOCK_STORE_UNAVAILABLE");
        }
        if (result == null) return new RenewResult(RenewStatus.UNKNOWN, null, "LOCK_STORE_NO_RESULT");
        CpfLockStore.StoredLock current = result.after();
        if (current == null) return new RenewResult(RenewStatus.NOT_FOUND, null, "NOT_FOUND");
        if (current.state() != State.ACTIVE || !current.leaseUntil().isAfter(now)) {
            return new RenewResult(RenewStatus.EXPIRED, null, "EXPIRED");
        }
        if (!current.ownerId().equals(token.ownerId())) {
            return new RenewResult(RenewStatus.NOT_OWNER, null, "NOT_OWNER");
        }
        if (!transitionedFrom(result, token)) {
            return new RenewResult(RenewStatus.STALE_TOKEN, null, "STALE_TOKEN");
        }
        return new RenewResult(RenewStatus.RENEWED, token(current), "RENEWED");
    }

    @Override
    public ReleaseResult release(LockToken token, String reason) {
        if (!validToken(token)) return new ReleaseResult(ReleaseStatus.INVALID, null, "INVALID_ARGUMENT");
        Instant now = clock.instant();
        final CpfLockStore.UpdateResult result;
        try {
            result = store.update(token.key(), current -> {
                if (current == null || current.state() != State.ACTIVE
                        || !current.leaseUntil().isAfter(now)) return current;
                if (!matches(current, token)) return current;
                return new CpfLockStore.StoredLock(
                        current.key(), current.ownerId(), current.requestId(),
                        current.fencingToken(), current.ownerEpoch(), nextVersion(current.rowVersion()),
                        current.acquiredAt(), current.leaseUntil(), State.RELEASED,
                        safeReason(reason), current.lastAuditId());
            });
        } catch (RuntimeException storageFailure) {
            return new ReleaseResult(ReleaseStatus.UNKNOWN, null, "LOCK_STORE_UNAVAILABLE");
        }
        if (result == null) return new ReleaseResult(ReleaseStatus.UNKNOWN, null, "LOCK_STORE_NO_RESULT");
        CpfLockStore.StoredLock current = result.after();
        if (current == null) return new ReleaseResult(ReleaseStatus.NOT_FOUND, null, "NOT_FOUND");
        if (current.ownerId() != null && !current.ownerId().equals(token.ownerId())) {
            return new ReleaseResult(ReleaseStatus.NOT_OWNER, snapshot(current), "NOT_OWNER");
        }
        if (current.state() == State.ACTIVE && !current.leaseUntil().isAfter(now)) {
            return new ReleaseResult(ReleaseStatus.EXPIRED, snapshot(current), "EXPIRED");
        }
        if (transitionedFrom(result, token) && current.state() == State.RELEASED) {
            return new ReleaseResult(ReleaseStatus.RELEASED, snapshot(current), "RELEASED");
        }
        if (sameOwnershipEpoch(current, token) && current.state() != State.ACTIVE
                && current.rowVersion() > token.version()) {
            return new ReleaseResult(ReleaseStatus.IDEMPOTENT_REPLAY, snapshot(current), "ALREADY_RELEASED");
        }
        return new ReleaseResult(ReleaseStatus.STALE_TOKEN, snapshot(current), "STALE_TOKEN");
    }

    @Override
    public boolean validateFence(String key, long fencingToken) {
        String normalizedKey = normalized(key);
        if (normalizedKey == null || fencingToken <= 0) return false;
        Instant now = clock.instant();
        try {
            return store.find(normalizedKey)
                    .filter(lock -> lock.state() == State.ACTIVE)
                    .filter(lock -> lock.leaseUntil().isAfter(now))
                    .map(lock -> lock.fencingToken() == fencingToken)
                    .orElse(false);
        } catch (RuntimeException storageFailure) {
            return false;
        }
    }

    @Override
    public boolean validateToken(LockToken token) {
        if (!validToken(token)) return false;
        Instant now = clock.instant();
        try {
            return store.find(token.key())
                    .filter(lock -> lock.state() == State.ACTIVE)
                    .filter(lock -> lock.leaseUntil().isAfter(now))
                    .map(lock -> matches(lock, token))
                    .orElse(false);
        } catch (RuntimeException storageFailure) {
            return false;
        }
    }

    @Override
    public FindResult findResult(String key) {
        String normalizedKey = normalized(key);
        if (normalizedKey == null) return new FindResult(QueryStatus.INVALID, null, "INVALID_ARGUMENT");
        try {
            return store.find(normalizedKey)
                    .map(lock -> new FindResult(QueryStatus.FOUND, snapshot(lock), "FOUND"))
                    .orElseGet(() -> new FindResult(QueryStatus.NOT_FOUND, null, "NOT_FOUND"));
        } catch (RuntimeException storageFailure) {
            return new FindResult(QueryStatus.UNKNOWN, null, "LOCK_STORE_UNAVAILABLE");
        }
    }

    @Override
    public ListResult listResult(int limit) {
        if (limit < 1 || limit > 1000) {
            return new ListResult(QueryStatus.INVALID, List.of(), "INVALID_LIMIT");
        }
        try {
            return new ListResult(QueryStatus.SUCCESS,
                    store.list(limit).stream().map(DefaultCpfLockManager::snapshot).toList(),
                    "SUCCESS");
        } catch (RuntimeException storageFailure) {
            return new ListResult(QueryStatus.UNKNOWN, List.of(), "LOCK_STORE_UNAVAILABLE");
        }
    }

    @Override
    public Optional<LockSnapshot> find(String key) {
        String normalizedKey = normalized(key);
        if (normalizedKey == null) return Optional.empty();
        try {
            return store.find(normalizedKey).map(DefaultCpfLockManager::snapshot);
        } catch (RuntimeException storageFailure) {
            return Optional.empty();
        }
    }

    @Override
    public List<LockSnapshot> list(int limit) {
        if (limit < 1) return List.of();
        try {
            return store.list(limit).stream().map(DefaultCpfLockManager::snapshot).toList();
        } catch (RuntimeException storageFailure) {
            return List.of();
        }
    }

    @Override
    public RecoveryResult reconcileExpired(int limit) {
        if (limit < 1 || limit > 1000) {
            return new RecoveryResult(RecoveryStatus.INVALID, 0, 0, 0, "INVALID_LIMIT");
        }
        final List<CpfLockStore.StoredLock> candidates;
        try {
            candidates = store.list(limit);
        } catch (RuntimeException storageFailure) {
            return new RecoveryResult(RecoveryStatus.UNKNOWN, 0, 0, 0, "LOCK_STORE_UNAVAILABLE");
        }
        Instant now = clock.instant();
        int recovered = 0;
        int conflicts = 0;
        for (CpfLockStore.StoredLock candidate : candidates) {
            if (candidate == null || candidate.state() != State.ACTIVE || candidate.leaseUntil().isAfter(now)) continue;
            try {
                CpfLockStore.UpdateResult result = store.update(candidate.key(), current -> {
                    if (current == null || current.state() != State.ACTIVE || current.leaseUntil().isAfter(now)) return current;
                    if (current.rowVersion() != candidate.rowVersion()
                            || current.fencingToken() != candidate.fencingToken()
                            || current.ownerEpoch() != candidate.ownerEpoch()) return current;
                    return new CpfLockStore.StoredLock(
                            current.key(), current.ownerId(), current.requestId(),
                            current.fencingToken(), current.ownerEpoch(), nextVersion(current.rowVersion()),
                            current.acquiredAt(), current.leaseUntil(), State.EXPIRED,
                            "LEASE_EXPIRED_RECONCILED", current.lastAuditId());
                });
                if (result != null && result.before() != null && result.after() != null
                        && !Objects.equals(result.before(), result.after())
                        && result.after().state() == State.EXPIRED) recovered++;
                else conflicts++;
            } catch (RuntimeException storageFailure) {
                conflicts++;
            }
        }
        RecoveryStatus status = conflicts == 0 ? RecoveryStatus.SUCCESS
                : recovered == 0 ? RecoveryStatus.UNKNOWN : RecoveryStatus.PARTIAL;
        return new RecoveryResult(status, candidates.size(), recovered, conflicts, status.name());
    }

    @Override
    public LockRuntimeSnapshot lockRuntimeSnapshot(int limit) {
        Instant now = clock.instant();
        if (limit < 1 || limit > 1000) {
            return new LockRuntimeSnapshot(QueryStatus.INVALID, 0, 0, 0, 0, 0, 0L,
                    now, Health.DOWN, "INVALID_LIMIT");
        }
        final List<CpfLockStore.StoredLock> locks;
        final CpfLockStore.CapacitySnapshot capacity;
        try {
            locks = store.list(limit);
            capacity = store.capacitySnapshot();
        } catch (RuntimeException storageFailure) {
            return new LockRuntimeSnapshot(QueryStatus.UNKNOWN, 0, 0, 0, 0, 0, 0L,
                    0, 0, 0L, null, now, Health.DOWN, "LOCK_STORE_UNAVAILABLE");
        }
        int active = 0, expired = 0, released = 0, forceReleased = 0;
        long highestFence = 0L;
        for (CpfLockStore.StoredLock lock : locks) {
            if (lock == null) continue;
            highestFence = Math.max(highestFence, lock.fencingToken());
            switch (lock.state()) {
                case ACTIVE -> { if (lock.leaseUntil().isAfter(now)) active++; else expired++; }
                case EXPIRED -> expired++;
                case RELEASED -> released++;
                case FORCE_RELEASED -> forceReleased++;
            }
        }
        boolean exhausted = capacity.bounded()
                && capacity.trackedKeyCount() >= capacity.maximumTrackedKeys();
        Health health = exhausted ? Health.CAPACITY_EXHAUSTED
                : expired > 0 ? Health.DEGRADED : Health.UP;
        String reason = exhausted ? "LOCK_STORE_CAPACITY_EXHAUSTED"
                : expired > 0 ? "EXPIRED_LOCKS_REQUIRE_RECONCILIATION" : "UP";
        return new LockRuntimeSnapshot(QueryStatus.SUCCESS, locks.size(), active, expired, released,
                forceReleased, highestFence, capacity.trackedKeyCount(), capacity.maximumTrackedKeys(),
                capacity.capacityRejectionCount(), capacity.lastCapacityRejectionAt(),
                now, health, reason);
    }

    @Override
    public ForceReleaseResult forceRelease(
            String key, String operatorId, String reason, ForceReleaseApproval approval) {
        String normalizedKey = normalized(key);
        String actor = normalized(operatorId);
        String why = normalized(reason);
        if (normalizedKey == null || actor == null || why == null) {
            return new ForceReleaseResult(ForceReleaseStatus.INVALID, null, null, "INVALID_ARGUMENT");
        }
        if (approval == null) {
            return new ForceReleaseResult(ForceReleaseStatus.APPROVAL_REQUIRED,
                    find(normalizedKey).orElse(null), null, "APPROVAL_REQUIRED");
        }
        String approver = normalized(approval.approverId());
        String approvalId = normalized(approval.approvalId());
        if (approver == null || approvalId == null) {
            return new ForceReleaseResult(ForceReleaseStatus.INVALID,
                    find(normalizedKey).orElse(null), null, "INVALID_APPROVAL");
        }
        if (actor.equals(approver)) {
            return new ForceReleaseResult(ForceReleaseStatus.SEPARATION_OF_DUTIES,
                    find(normalizedKey).orElse(null), null, "SELF_APPROVAL_FORBIDDEN");
        }
        if (approval.commandHash() == null || approval.commandHash().isBlank()) {
            return new ForceReleaseResult(ForceReleaseStatus.APPROVAL_SCOPE_MISMATCH,
                    find(normalizedKey).orElse(null), null, "APPROVAL_COMMAND_HASH_MISSING");
        }
        String auditId = deterministicAuditId(approvalId, approval.commandHash());
        Instant now = clock.instant();
        final CpfLockStore.StoredLock target;
        try {
            target = store.find(normalizedKey).orElse(null);
        } catch (RuntimeException storageFailure) {
            return new ForceReleaseResult(ForceReleaseStatus.UNKNOWN, null, auditId,
                    "LOCK_STORE_UNAVAILABLE");
        }
        LockSnapshot approvalTarget = snapshot(target);
        if (target != null && target.state() == State.FORCE_RELEASED
                && auditId.equals(target.lastAuditId())) {
            return new ForceReleaseResult(ForceReleaseStatus.IDEMPOTENT_REPLAY,
                    approvalTarget, auditId, "ALREADY_FORCE_RELEASED");
        }
        Duration approvalWindow;
        try {
            approvalWindow = Duration.between(approval.approvedAt(), approval.expiresAt());
        } catch (RuntimeException invalidWindow) {
            return new ForceReleaseResult(ForceReleaseStatus.INVALID,
                    approvalTarget, auditId, "INVALID_APPROVAL_WINDOW");
        }
        if (approvalWindow.compareTo(MAX_FORCE_RELEASE_APPROVAL_WINDOW) > 0) {
            return new ForceReleaseResult(ForceReleaseStatus.APPROVAL_WINDOW_EXCEEDED,
                    approvalTarget, auditId, "APPROVAL_WINDOW_EXCEEDS_LIMIT");
        }
        if (approval.approvedAt().isAfter(now) || !approval.expiresAt().isAfter(now)) {
            return new ForceReleaseResult(ForceReleaseStatus.APPROVAL_EXPIRED,
                    approvalTarget, auditId, "APPROVAL_NOT_ACTIVE");
        }
        if (target == null || target.state() != State.ACTIVE || !target.leaseUntil().isAfter(now)) {
            return new ForceReleaseResult(ForceReleaseStatus.NOT_FOUND,
                    approvalTarget, auditId, "ACTIVE_LOCK_NOT_FOUND");
        }
        ForceReleaseCommand command = new ForceReleaseCommand(
                normalizedKey, actor, why, target.fencingToken(), target.rowVersion());
        if (!command.immutableHash().equalsIgnoreCase(approval.commandHash())) {
            return new ForceReleaseResult(ForceReleaseStatus.APPROVAL_SCOPE_MISMATCH,
                    approvalTarget, auditId, "APPROVAL_COMMAND_HASH_MISMATCH");
        }
        if (approval.approvedAt().isBefore(target.acquiredAt())) {
            return new ForceReleaseResult(ForceReleaseStatus.APPROVAL_SCOPE_MISMATCH,
                    approvalTarget, null, "APPROVAL_PREDATES_CURRENT_LOCK");
        }
        if (!audit.available()) {
            return new ForceReleaseResult(ForceReleaseStatus.AUDIT_UNAVAILABLE,
                    approvalTarget, null, "AUDIT_UNAVAILABLE");
        }

        try {
            audit.record(new CpfLockAuditSink.AuditEvent(
                    auditId + "-AUTH", "FORCE_RELEASE_AUTHORIZED", normalizedKey, actor, approver,
                    safeReason(why), approvalId, target.fencingToken(), now, "AUTHORIZED"));
        } catch (RuntimeException failure) {
            return new ForceReleaseResult(ForceReleaseStatus.AUDIT_UNAVAILABLE,
                    approvalTarget, auditId, "AUDIT_AUTHORIZATION_FAILED");
        }

        final CpfLockStore.UpdateResult result;
        try {
            result = store.update(normalizedKey, current -> {
                if (current == null || current.state() != State.ACTIVE) return current;
                if (current.fencingToken() != target.fencingToken()
                        || current.ownerEpoch() != target.ownerEpoch()
                        || current.rowVersion() != target.rowVersion()) return current;
                return new CpfLockStore.StoredLock(
                        current.key(), current.ownerId(), current.requestId(),
                        current.fencingToken(), current.ownerEpoch(), nextVersion(current.rowVersion()),
                        current.acquiredAt(), current.leaseUntil(), State.FORCE_RELEASED,
                        safeReason(why), auditId);
            });
        } catch (RuntimeException storageFailure) {
            recordResultBestEffort(auditId, normalizedKey, actor, approver, why, approvalId,
                    target.fencingToken(), "LOCK_STORE_UNAVAILABLE");
            return new ForceReleaseResult(ForceReleaseStatus.UNKNOWN,
                    find(normalizedKey).orElse(null), auditId, "LOCK_STORE_UNAVAILABLE");
        }
        if (result == null || result.after() == null) {
            recordResultBestEffort(auditId, normalizedKey, actor, approver, why, approvalId,
                    target.fencingToken(), "LOCK_STORE_NO_RESULT");
            return new ForceReleaseResult(ForceReleaseStatus.UNKNOWN,
                    find(normalizedKey).orElse(null), auditId, "LOCK_STORE_NO_RESULT");
        }
        if (Objects.equals(result.before(), result.after())) {
            LockSnapshot current = snapshot(result.after());
            recordResultBestEffort(auditId, normalizedKey, actor, approver, why, approvalId,
                    result.after().fencingToken(), "APPROVAL_SCOPE_CHANGED");
            return new ForceReleaseResult(ForceReleaseStatus.APPROVAL_SCOPE_MISMATCH,
                    current, auditId, "LOCK_CHANGED_AFTER_APPROVAL");
        }

        LockSnapshot released = snapshot(result.after());
        try {
            audit.record(new CpfLockAuditSink.AuditEvent(
                    auditId + "-RESULT", "FORCE_RELEASE", normalizedKey, actor, approver,
                    safeReason(why), approvalId, released.fencingToken(), clock.instant(), "RELEASED"));
        } catch (RuntimeException failure) {
            return new ForceReleaseResult(ForceReleaseStatus.UNKNOWN, released, auditId,
                    "RELEASED_AUDIT_COMPLETION_FAILED");
        }
        return new ForceReleaseResult(ForceReleaseStatus.RELEASED, released, auditId, "RELEASED");
    }

    private void recordResultBestEffort(
            String auditId, String key, String actor, String approver, String reason,
            String approvalId, long fencingToken, String result) {
        try {
            audit.record(new CpfLockAuditSink.AuditEvent(
                    auditId + "-RESULT", "FORCE_RELEASE", key, actor, approver,
                    safeReason(reason), approvalId, fencingToken, clock.instant(), result));
        } catch (RuntimeException ignored) {
            // The authorization event is already durable. Callers receive the state-derived result.
        }
    }


    private static String deterministicAuditId(String approvalId, String commandHash) {
        String canonical = approvalId + "\n" + commandHash.toLowerCase();
        return java.util.UUID.nameUUIDFromBytes(canonical.getBytes(StandardCharsets.UTF_8)).toString();
    }

    private static boolean transitionedFrom(CpfLockStore.UpdateResult result, LockToken token) {
        return result.before() != null && !Objects.equals(result.before(), result.after())
                && matches(result.before(), token)
                && result.after() != null
                && result.after().rowVersion() == token.version() + 1L;
    }

    private static boolean matches(CpfLockStore.StoredLock lock, LockToken token) {
        return lock.fencingToken() == token.fencingToken()
                && lock.ownerEpoch() == token.ownerEpoch()
                && lock.rowVersion() == token.version()
                && Objects.equals(lock.ownerId(), token.ownerId())
                && Objects.equals(lock.requestId(), token.requestId());
    }

    private static boolean sameOwnershipEpoch(CpfLockStore.StoredLock lock, LockToken token) {
        return lock.fencingToken() == token.fencingToken()
                && lock.ownerEpoch() == token.ownerEpoch()
                && Objects.equals(lock.ownerId(), token.ownerId())
                && Objects.equals(lock.requestId(), token.requestId());
    }

    private static LockToken token(CpfLockStore.StoredLock lock) {
        return new LockToken(
                lock.key(), lock.ownerId(), lock.requestId(), lock.fencingToken(),
                lock.ownerEpoch(), lock.rowVersion(), lock.leaseUntil());
    }

    private static LockSnapshot snapshot(CpfLockStore.StoredLock lock) {
        if (lock == null) return null;
        return new LockSnapshot(
                lock.key(), lock.ownerId(), lock.requestId(), lock.fencingToken(),
                lock.ownerEpoch(), lock.rowVersion(), lock.acquiredAt(), lock.leaseUntil(), lock.state());
    }

    private static boolean validToken(LockToken token) {
        return token != null && normalized(token.key()) != null && normalized(token.ownerId()) != null
                && normalized(token.requestId()) != null && token.fencingToken() > 0
                && token.ownerEpoch() > 0 && token.version() > 0;
    }

    private static long nextVersion(long current) {
        if (current < 1 || current == Long.MAX_VALUE) {
            throw new IllegalStateException("lock row version exhausted");
        }
        return current + 1L;
    }

    private static Duration validateLease(Duration value) {
        return value == null || value.compareTo(MIN_LEASE) < 0 || value.compareTo(MAX_LEASE) > 0 ? null : value;
    }

    private static String normalized(String value) {
        if (value == null) return null;
        String normalized = value.trim();
        return normalized.isEmpty() || normalized.length() > 200 ? null : normalized;
    }

    private static String safeReason(String value) {
        String masked = SensitiveDataMasker.mask(value == null ? "" : value, 500);
        return masked.length() > 500 ? masked.substring(0, 500) : masked;
    }
}

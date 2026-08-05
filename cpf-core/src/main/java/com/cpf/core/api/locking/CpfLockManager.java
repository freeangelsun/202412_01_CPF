package com.cpf.core.api.locking;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

/** Topology-independent lock, lease, owner-epoch and fencing contract. */
public interface CpfLockManager {
    AcquireResult acquire(String key, String ownerId, String requestId, Duration leaseDuration);
    RenewResult renew(LockToken token, Duration leaseDuration);
    ReleaseResult release(LockToken token, String reason);
    boolean validateFence(String key, long fencingToken);

    /**
     * Validates the complete optimistic token, not only the fencing epoch.
     *
     * <p>The default implementation fails closed when the provider cannot read the current
     * snapshot. Custom providers may override this method to use an atomic store-side compare,
     * but must preserve the same owner, request, epoch, version and lease semantics.</p>
     */
    default boolean validateToken(LockToken token) {
        if (token == null || !validateFence(token.key(), token.fencingToken())) {
            return false;
        }
        try {
            return find(token.key())
                    .filter(snapshot -> snapshot.state() == State.ACTIVE)
                    .filter(snapshot -> snapshot.key().equals(token.key()))
                    .filter(snapshot -> token.ownerId().equals(snapshot.ownerId()))
                    .filter(snapshot -> token.requestId().equals(snapshot.requestId()))
                    .filter(snapshot -> snapshot.fencingToken() == token.fencingToken())
                    .filter(snapshot -> snapshot.ownerEpoch() == token.ownerEpoch())
                    .filter(snapshot -> snapshot.version() == token.version())
                    .filter(snapshot -> snapshot.leaseUntil().equals(token.leaseUntil()))
                    .isPresent();
        } catch (RuntimeException storageFailure) {
            return false;
        }
    }

    Optional<LockSnapshot> find(String key);
    List<LockSnapshot> list(int limit);

    /** Status-bearing query that distinguishes an empty result from a storage outage. */
    default FindResult findResult(String key) {
        if (key == null || key.isBlank()) return new FindResult(QueryStatus.INVALID, null, "INVALID_ARGUMENT");
        Optional<LockSnapshot> found = find(key);
        return found.map(snapshot -> new FindResult(QueryStatus.FOUND, snapshot, "FOUND"))
                .orElseGet(() -> new FindResult(QueryStatus.NOT_FOUND, null, "NOT_FOUND"));
    }

    /** Status-bearing list operation for monitoring and readiness consumers. */
    default ListResult listResult(int limit) {
        if (limit < 1 || limit > 1000) return new ListResult(QueryStatus.INVALID, List.of(), "INVALID_LIMIT");
        return new ListResult(QueryStatus.SUCCESS, list(limit), "SUCCESS");
    }

    ForceReleaseResult forceRelease(
            String key, String operatorId, String reason, ForceReleaseApproval approval);

    /** Reconciles naturally expired ACTIVE leases into durable EXPIRED state. */
    default RecoveryResult reconcileExpired(int limit) {
        if (limit < 1 || limit > 1000) return new RecoveryResult(RecoveryStatus.INVALID, 0, 0, 0, "INVALID_LIMIT");
        return new RecoveryResult(RecoveryStatus.UNSUPPORTED, 0, 0, 0, "RECOVERY_NOT_SUPPORTED");
    }

    record LockToken(
            String key,
            String ownerId,
            String requestId,
            long fencingToken,
            long ownerEpoch,
            long version,
            Instant leaseUntil) {
        public LockToken {
            if (key == null || key.isBlank() || ownerId == null || ownerId.isBlank()
                    || requestId == null || requestId.isBlank() || fencingToken < 1
                    || ownerEpoch < 1 || version < 1 || leaseUntil == null) {
                throw new IllegalArgumentException("complete lock token is required");
            }
            key = key.trim();
            ownerId = ownerId.trim();
            requestId = requestId.trim();
        }

        /** Source-compatible constructor for clients compiled before optimistic version exposure. */
        public LockToken(
                String key, String ownerId, String requestId, long fencingToken, Instant leaseUntil) {
            this(key, ownerId, requestId, fencingToken, fencingToken, 1L, leaseUntil);
        }
    }

    record LockSnapshot(
            String key,
            String ownerId,
            String requestId,
            long fencingToken,
            long ownerEpoch,
            long version,
            Instant acquiredAt,
            Instant leaseUntil,
            State state) {
        public LockSnapshot {
            if (key == null || key.isBlank() || fencingToken < 1 || ownerEpoch < 1 || version < 1
                    || acquiredAt == null || leaseUntil == null || state == null) {
                throw new IllegalArgumentException("complete lock snapshot is required");
            }
        }

        /** Source-compatible constructor for the original fencing-only snapshot. */
        public LockSnapshot(
                String key,
                String ownerId,
                String requestId,
                long fencingToken,
                Instant acquiredAt,
                Instant leaseUntil,
                State state) {
            this(key, ownerId, requestId, fencingToken, fencingToken, 1L,
                    acquiredAt, leaseUntil, state);
        }
    }


    enum QueryStatus { SUCCESS, FOUND, NOT_FOUND, UNKNOWN, INVALID }
    record FindResult(QueryStatus status, LockSnapshot snapshot, String reason) {}
    record ListResult(QueryStatus status, List<LockSnapshot> snapshots, String reason) {
        public ListResult { snapshots = snapshots == null ? List.of() : List.copyOf(snapshots); }
    }

    enum State { ACTIVE, RELEASED, EXPIRED, FORCE_RELEASED }
    enum AcquireStatus { ACQUIRED, IDEMPOTENT_REPLAY, BUSY, RESOURCE_EXHAUSTED, UNKNOWN, INVALID }
    record AcquireResult(AcquireStatus status, LockToken token, LockSnapshot current, String reason) {}
    enum RenewStatus { RENEWED, STALE_TOKEN, NOT_OWNER, NOT_FOUND, EXPIRED, UNKNOWN, INVALID }
    record RenewResult(RenewStatus status, LockToken token, String reason) {}
    enum ReleaseStatus { RELEASED, IDEMPOTENT_REPLAY, STALE_TOKEN, NOT_OWNER, NOT_FOUND, EXPIRED, UNKNOWN, INVALID }
    record ReleaseResult(ReleaseStatus status, LockSnapshot snapshot, String reason) {}

    record ForceReleaseCommand(
            String key,
            String requesterId,
            String reason,
            long expectedFencingToken,
            long expectedVersion) {
        public ForceReleaseCommand {
            if (key == null || key.isBlank() || requesterId == null || requesterId.isBlank()
                    || reason == null || reason.isBlank() || expectedFencingToken < 1
                    || expectedVersion < 1) {
                throw new IllegalArgumentException("complete force-release command is required");
            }
            key = key.trim();
            requesterId = requesterId.trim();
            reason = reason.trim();
        }

        /** Legacy commands are intentionally scoped to version 1 and cannot authorize a later state. */
        public ForceReleaseCommand(
                String key, String requesterId, String reason, long expectedFencingToken) {
            this(key, requesterId, reason, expectedFencingToken, 1L);
        }

        public String immutableHash() {
            String canonical = key + "\n" + requesterId + "\n" + reason + "\n"
                    + expectedFencingToken + "\n" + expectedVersion;
            try {
                return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                        .digest(canonical.getBytes(StandardCharsets.UTF_8)));
            } catch (NoSuchAlgorithmException impossible) {
                throw new IllegalStateException("SHA-256 is unavailable", impossible);
            }
        }

        @Override
        public String toString() {
            return "ForceReleaseCommand[key=" + key
                    + ", requesterId=" + requesterId
                    + ", reason=[REDACTED]"
                    + ", expectedFencingToken=" + expectedFencingToken
                    + ", expectedVersion=" + expectedVersion + "]";
        }
    }

    record ForceReleaseApproval(
            String approvalId,
            String approverId,
            Instant approvedAt,
            Instant expiresAt,
            String commandHash) {
        public ForceReleaseApproval {
            if (approvalId == null || approvalId.isBlank()
                    || approverId == null || approverId.isBlank()
                    || approvedAt == null || expiresAt == null
                    || !expiresAt.isAfter(approvedAt)) {
                throw new IllegalArgumentException("complete, bounded approval is required");
            }
            approvalId = approvalId.trim();
            approverId = approverId.trim();
            commandHash = commandHash == null ? null : commandHash.trim().toLowerCase();
            if (commandHash != null && !commandHash.matches("[0-9a-f]{64}")) {
                throw new IllegalArgumentException("commandHash must be a lowercase SHA-256 value");
            }
        }

        /** Compatibility constructor; unscoped approvals fail closed at execution time. */
        public ForceReleaseApproval(
                String approvalId, String approverId, Instant approvedAt, Instant expiresAt) {
            this(approvalId, approverId, approvedAt, expiresAt, null);
        }

        public static ForceReleaseApproval approve(
                String approvalId,
                String approverId,
                ForceReleaseCommand command,
                Instant approvedAt,
                Instant expiresAt) {
            return new ForceReleaseApproval(approvalId, approverId, approvedAt, expiresAt,
                    command.immutableHash());
        }
    }


    enum RecoveryStatus { SUCCESS, PARTIAL, UNKNOWN, INVALID, UNSUPPORTED }
    record RecoveryResult(RecoveryStatus status, int scanned, int recovered, int conflicts, String reason) {
        public RecoveryResult {
            if (scanned < 0 || recovered < 0 || conflicts < 0) {
                throw new IllegalArgumentException("recovery counters must be non-negative");
            }
        }
    }

    enum ForceReleaseStatus {
        RELEASED, IDEMPOTENT_REPLAY, NOT_FOUND, APPROVAL_REQUIRED,
        SEPARATION_OF_DUTIES, APPROVAL_EXPIRED, APPROVAL_WINDOW_EXCEEDED, APPROVAL_SCOPE_MISMATCH,
        AUDIT_UNAVAILABLE, UNKNOWN, INVALID
    }
    record ForceReleaseResult(ForceReleaseStatus status, LockSnapshot snapshot, String auditId, String reason) {}
}

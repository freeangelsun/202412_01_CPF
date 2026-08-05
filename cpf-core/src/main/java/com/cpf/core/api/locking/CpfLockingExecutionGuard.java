package com.cpf.core.api.locking;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/** Executes a critical section only while a valid fencing token is held. */
public final class CpfLockingExecutionGuard {
    private final CpfLockManager manager;

    public CpfLockingExecutionGuard(CpfLockManager manager) {
        this.manager = Objects.requireNonNull(manager, "manager");
    }

    public <T> T execute(
            String key, String ownerId, String requestId, Duration lease, Supplier<T> action) {
        Objects.requireNonNull(action, "action");
        return executeFenced(key, ownerId, requestId, lease, ignored -> action.get());
    }

    /** Supplies the fencing token to a storage adapter so stale-writer rejection can be enforced at commit. */
    public <T> T executeFenced(
            String key,
            String ownerId,
            String requestId,
            Duration lease,
            Function<CpfLockManager.LockToken, T> action) {
        Objects.requireNonNull(action, "action");
        CpfLockManager.AcquireResult acquired = manager.acquire(key, ownerId, requestId, lease);
        if (acquired.status() != CpfLockManager.AcquireStatus.ACQUIRED) {
            throw new LockUnavailableException(key, acquired.status(), acquired.reason());
        }
        CpfLockManager.LockToken token = acquired.token();
        Throwable actionFailure = null;
        try {
            requireValidFence(token);
            T result = action.apply(token);
            requireValidFence(token);
            return result;
        } catch (Throwable failure) {
            actionFailure = failure;
            throw failure;
        } finally {
            CpfLockManager.ReleaseResult released = manager.release(token, "EXECUTION_COMPLETED");
            if (released.status() != CpfLockManager.ReleaseStatus.RELEASED
                    && released.status() != CpfLockManager.ReleaseStatus.IDEMPOTENT_REPLAY) {
                LockReleaseException releaseFailure = new LockReleaseException(
                        key, token.fencingToken(), released.status(), released.reason());
                if (actionFailure != null) actionFailure.addSuppressed(releaseFailure);
                else throw releaseFailure;
            }
        }
    }

    private void requireValidFence(CpfLockManager.LockToken token) {
        if (!manager.validateToken(token)) {
            throw new StaleFenceException(token.key(), token.fencingToken());
        }
    }

    public static final class LockUnavailableException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        private final CpfLockManager.AcquireStatus status;

        public LockUnavailableException(
                String key, CpfLockManager.AcquireStatus status, String reason) {
            super("Lock unavailable: " + key + " [" + status + "] (" + reason + ")");
            this.status = status;
        }

        public CpfLockManager.AcquireStatus status() { return status; }
    }

    public static final class StaleFenceException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public StaleFenceException(String key, long token) {
            super("Stale fencing token: " + key + "/" + token);
        }
    }

    public static final class LockReleaseException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        private final CpfLockManager.ReleaseStatus status;

        public LockReleaseException(
                String key, long token, CpfLockManager.ReleaseStatus status, String reason) {
            super("Lock release is not confirmed: " + key + "/" + token
                    + " [" + status + "] (" + reason + ")");
            this.status = status;
        }

        public CpfLockManager.ReleaseStatus status() { return status; }
    }
}

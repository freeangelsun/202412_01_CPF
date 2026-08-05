package com.cpf.core.api.locking;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** Standalone regression harness for providers that inherit the public default token validator. */
public final class CpfLockManagerDefaultTokenValidationHarness {
    public static void main(String[] args) {
        Instant leaseUntil = Instant.parse("2026-08-05T12:00:00Z");
        CpfLockManager.LockSnapshot current = new CpfLockManager.LockSnapshot(
                "order:42", "node-a", "request-1", 7L, 3L, 11L,
                Instant.parse("2026-08-05T11:55:00Z"), leaseUntil, CpfLockManager.State.ACTIVE);
        FakeManager manager = new FakeManager(current);

        assertTrue(manager.validateToken(new CpfLockManager.LockToken(
                "order:42", "node-a", "request-1", 7L, 3L, 11L, leaseUntil)), "current token");
        assertFalse(manager.validateToken(new CpfLockManager.LockToken(
                "order:42", "node-a", "request-1", 7L, 3L, 10L, leaseUntil)), "stale version");
        assertFalse(manager.validateToken(new CpfLockManager.LockToken(
                "order:42", "node-b", "request-1", 7L, 3L, 11L, leaseUntil)), "wrong owner");
        assertFalse(manager.validateToken(new CpfLockManager.LockToken(
                "order:42", "node-a", "request-2", 7L, 3L, 11L, leaseUntil)), "wrong request");
        assertFalse(manager.validateToken(new CpfLockManager.LockToken(
                "order:42", "node-a", "request-1", 7L, 2L, 11L, leaseUntil)), "stale owner epoch");
        assertFalse(manager.validateToken(new CpfLockManager.LockToken(
                "order:42", "node-a", "request-1", 7L, 3L, 11L, leaseUntil.plusSeconds(1))), "wrong lease");
        manager.failRead = true;
        assertFalse(manager.validateToken(new CpfLockManager.LockToken(
                "order:42", "node-a", "request-1", 7L, 3L, 11L, leaseUntil)), "storage failure");
        System.out.println("CPF_LOCK_DEFAULT_TOKEN_VALIDATION_PASS");
    }

    private static void assertTrue(boolean value, String label) {
        if (!value) throw new AssertionError(label + " must be true");
    }

    private static void assertFalse(boolean value, String label) {
        if (value) throw new AssertionError(label + " must be false");
    }

    private static final class FakeManager implements CpfLockManager {
        private final LockSnapshot snapshot;
        private boolean failRead;

        private FakeManager(LockSnapshot snapshot) {
            this.snapshot = snapshot;
        }

        @Override public boolean validateFence(String key, long fencingToken) {
            return snapshot.key().equals(key)
                    && snapshot.fencingToken() == fencingToken
                    && snapshot.state() == State.ACTIVE;
        }
        @Override public Optional<LockSnapshot> find(String key) {
            if (failRead) throw new IllegalStateException("store unavailable");
            return snapshot.key().equals(key) ? Optional.of(snapshot) : Optional.empty();
        }
        @Override public AcquireResult acquire(String key, String ownerId, String requestId, Duration leaseDuration) {
            throw new UnsupportedOperationException();
        }
        @Override public RenewResult renew(LockToken token, Duration leaseDuration) {
            throw new UnsupportedOperationException();
        }
        @Override public ReleaseResult release(LockToken token, String reason) {
            throw new UnsupportedOperationException();
        }
        @Override public List<LockSnapshot> list(int limit) { return List.of(snapshot); }
        @Override public ForceReleaseResult forceRelease(
                String key, String operatorId, String reason, ForceReleaseApproval approval) {
            throw new UnsupportedOperationException();
        }
    }
}

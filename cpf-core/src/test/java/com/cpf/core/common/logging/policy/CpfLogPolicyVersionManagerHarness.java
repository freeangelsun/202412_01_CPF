package com.cpf.core.common.logging.policy;

import com.cpf.core.api.logging.policy.CpfLogPolicyVersionApproval;
import com.cpf.core.api.logging.policy.CpfLogPolicyVersionResult;
import com.cpf.core.api.logging.policy.CpfLogPolicyVersionRollbackCommand;
import com.cpf.core.api.logging.policy.CpfLogPolicyVersionSnapshot;
import com.cpf.core.api.logging.policy.CpfLogPolicyVersionUpdateCommand;
import com.cpf.core.api.logging.policy.LogCaptureMode;
import com.cpf.core.api.logging.policy.LogPolicyDecision;
import com.cpf.core.api.logging.policy.LogPolicyTargetType;
import com.cpf.core.internal.logging.InMemoryCpfLogPolicyVersionStore;
import com.cpf.core.service.logging.DefaultCpfLogPolicyVersionManager;
import com.cpf.core.spi.logging.CpfLogPolicyVersionApplier;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public final class CpfLogPolicyVersionManagerHarness {
    public static void main(String[] args) throws Exception {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-05T00:00:00Z"));
        InMemoryCpfLogPolicyVersionStore store = new InMemoryCpfLogPolicyVersionStore(8, 8, 32,
                Duration.ofMinutes(5), clock);
        List<Object> audits = new ArrayList<>();
        RecordingApplier applier = new RecordingApplier(clock);
        DefaultCpfLogPolicyVersionManager manager = new DefaultCpfLogPolicyVersionManager(
                store, audits::add, applier, clock);

        LogPolicyDecision first = decision("DEBUG", LogCaptureMode.MASKED_BODY);
        CpfLogPolicyVersionUpdateCommand firstCommand = update("log-policy-cmd-0001", 1L, first,
                "operator-a", "incident token=secret-raw");
        CpfLogPolicyVersionResult applied = manager.update(withApproval(firstCommand, "approver-b", clock));
        require(applied.status() == CpfLogPolicyVersionResult.Status.APPLIED, "first update must apply");
        require(applied.snapshot().version() == 2L && applier.applied.get() == 1, "runtime consumer must apply");
        require(!applied.snapshot().reason().contains("secret-raw"), "reason must be sanitized");

        CpfLogPolicyVersionResult replay = manager.update(withApproval(firstCommand, "approver-b", clock));
        require(replay.status() == CpfLogPolicyVersionResult.Status.IDEMPOTENT_REPLAY, "same command must replay");
        CpfLogPolicyVersionUpdateCommand conflict = update("log-policy-cmd-0001", 1L,
                decision("WARN", LogCaptureMode.NONE), "operator-a", "different");
        require(manager.update(withApproval(conflict, "approver-b", clock)).status()
                == CpfLogPolicyVersionResult.Status.COMMAND_CONFLICT, "command reuse must conflict");

        CpfLogPolicyVersionUpdateCommand stale = update("log-policy-cmd-0002", 1L,
                decision("INFO", LogCaptureMode.NONE), "operator-a", "stale");
        require(manager.update(withApproval(stale, "approver-b", clock)).status()
                == CpfLogPolicyVersionResult.Status.VERSION_CONFLICT, "stale version must conflict");

        CpfLogPolicyVersionUpdateCommand second = update("log-policy-cmd-0003", 2L,
                decision("ERROR", LogCaptureMode.NONE), "operator-a", "reduce capture");
        require(manager.update(withApproval(second, "approver-b", clock)).status()
                == CpfLogPolicyVersionResult.Status.APPLIED, "second update must apply");
        CpfLogPolicyVersionRollbackCommand rollback = new CpfLogPolicyVersionRollbackCommand(
                "log-policy-cmd-0004", LogPolicyTargetType.MODULE, "PAY", 3L,
                2L, "operator-a", "rollback after verification", null);
        rollback = new CpfLogPolicyVersionRollbackCommand(rollback.commandId(), rollback.targetType(), rollback.targetId(), rollback.expectedVersion(),
                rollback.targetVersion(), rollback.actor(),
                rollback.reason(), approval(rollback.commandHash(), "approver-b", clock));
        require(manager.rollback(rollback).status() == CpfLogPolicyVersionResult.Status.APPLIED,
                "rollback must create a new version");
        require(manager.current(LogPolicyTargetType.MODULE, "PAY").version() == 4L,
                "rollback must preserve monotonic version");

        CpfLogPolicyVersionUpdateCommand self = update("log-policy-cmd-0005", 4L,
                decision("INFO", LogCaptureMode.NONE), "operator-a", "self approval");
        self = new CpfLogPolicyVersionUpdateCommand(self.commandId(), self.expectedVersion(),
                self.decision(), self.actor(), self.reason(),
                approval(self.commandHash(), "operator-a", clock));
        require(manager.update(self).status() == CpfLogPolicyVersionResult.Status.APPROVAL_INVALID,
                "self approval must fail closed");

        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger winners = new AtomicInteger();
        Thread[] threads = new Thread[6];
        for (int i = 0; i < threads.length; i++) {
            int index = i;
            threads[i] = new Thread(() -> {
                try {
                    start.await();
                    CpfLogPolicyVersionUpdateCommand command = update("log-policy-race-" + index, 4L,
                            decision("WARN", LogCaptureMode.NONE), "operator-a", "race");
                    if (manager.update(withApproval(command, "approver-b", clock)).status()
                            == CpfLogPolicyVersionResult.Status.APPLIED) winners.incrementAndGet();
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                }
            });
            threads[i].start();
        }
        start.countDown();
        for (Thread thread : threads) thread.join();
        require(winners.get() == 1, "concurrent CAS must have one winner");

        InMemoryCpfLogPolicyVersionStore unknownStore = new InMemoryCpfLogPolicyVersionStore(clock);
        DefaultCpfLogPolicyVersionManager unknown = new DefaultCpfLogPolicyVersionManager(
                unknownStore, event -> { }, new RecordingApplier(clock, true), clock);
        CpfLogPolicyVersionUpdateCommand unknownCommand = update("log-policy-cmd-unknown", 1L,
                first, "operator-a", "apply failure");
        require(unknown.update(withApproval(unknownCommand, "approver-b", clock)).status()
                == CpfLogPolicyVersionResult.Status.UNKNOWN_RESULT, "post-commit apply failure must be unknown");
        require(unknown.current(LogPolicyTargetType.MODULE, "PAY").version() == 2L,
                "unknown result must retain committed state for reconciliation");
        RecordingApplier recoveredApplier = new RecordingApplier(clock);
        DefaultCpfLogPolicyVersionManager recovered = new DefaultCpfLogPolicyVersionManager(
                unknownStore, event -> { }, recoveredApplier, clock);
        com.cpf.core.api.logging.policy.CpfLogPolicyVersionReconcileCommand reconcile =
                new com.cpf.core.api.logging.policy.CpfLogPolicyVersionReconcileCommand(
                        "log-policy-reconcile-01", LogPolicyTargetType.MODULE, "PAY", 2L,
                        "operator-a", "reconcile runtime apply", null);
        reconcile = new com.cpf.core.api.logging.policy.CpfLogPolicyVersionReconcileCommand(
                reconcile.commandId(), reconcile.targetType(), reconcile.targetId(), reconcile.expectedVersion(),
                reconcile.actor(), reconcile.reason(), approval(reconcile.commandHash(), "approver-b", clock));
        require(recovered.reconcile(reconcile).status() == CpfLogPolicyVersionResult.Status.APPLIED,
                "unknown version must reconcile to active");
        require(recovered.current(LogPolicyTargetType.MODULE, "PAY").status()
                == CpfLogPolicyVersionSnapshot.Status.ACTIVE, "reconciled state must be active");

        DefaultCpfLogPolicyVersionManager blocked = new DefaultCpfLogPolicyVersionManager(
                new InMemoryCpfLogPolicyVersionStore(clock), event -> { throw new IllegalStateException("audit down"); },
                new RecordingApplier(clock), clock);
        CpfLogPolicyVersionUpdateCommand blockedCommand = update("log-policy-cmd-blocked", 1L,
                first, "operator-a", "audit down");
        require(blocked.update(withApproval(blockedCommand, "approver-b", clock)).status()
                == CpfLogPolicyVersionResult.Status.AUDIT_UNAVAILABLE, "pre-audit failure must block mutation");
        require(blocked.history(LogPolicyTargetType.MODULE, "PAY", 10).size() == 1
                        && blocked.current(LogPolicyTargetType.MODULE, "PAY").version() == 1L,
                "blocked mutation must retain only the resolved baseline");

        MutableClock ttlClock = new MutableClock(Instant.parse("2026-08-05T01:00:00Z"));
        InMemoryCpfLogPolicyVersionStore ttlStore = new InMemoryCpfLogPolicyVersionStore(
                8, 8, 32, Duration.ofSeconds(30), ttlClock);
        DefaultCpfLogPolicyVersionManager ttlManager = new DefaultCpfLogPolicyVersionManager(
                ttlStore, event -> { }, new RecordingApplier(ttlClock), ttlClock);
        CpfLogPolicyVersionUpdateCommand ttlFirst = update("log-policy-cmd-ttl", 1L,
                decision("DEBUG", LogCaptureMode.NONE), "operator-a", "ttl first");
        require(ttlManager.update(withApproval(ttlFirst, "approver-b", ttlClock)).status()
                == CpfLogPolicyVersionResult.Status.APPLIED, "ttl first command must apply");
        CpfLogPolicyVersionUpdateCommand ttlConflict = update("log-policy-cmd-ttl", 2L,
                decision("WARN", LogCaptureMode.NONE), "operator-a", "ttl conflict");
        require(ttlManager.update(withApproval(ttlConflict, "approver-b", ttlClock)).status()
                == CpfLogPolicyVersionResult.Status.COMMAND_CONFLICT,
                "different payload must conflict before command TTL expiry");
        ttlClock.advance(Duration.ofSeconds(31));
        require(ttlManager.update(withApproval(ttlConflict, "approver-b", ttlClock)).status()
                == CpfLogPolicyVersionResult.Status.APPLIED,
                "expired command record may be reused against the current optimistic version");
        require(ttlManager.current(LogPolicyTargetType.MODULE, "PAY").version() == 3L,
                "command TTL reuse must preserve monotonic policy versioning");

        AtomicInteger auditCalls = new AtomicInteger();
        InMemoryCpfLogPolicyVersionStore completionAuditStore = new InMemoryCpfLogPolicyVersionStore(clock);
        DefaultCpfLogPolicyVersionManager completionAuditFailure = new DefaultCpfLogPolicyVersionManager(
                completionAuditStore, event -> {
                    if (auditCalls.incrementAndGet() == 2) throw new IllegalStateException("completion audit down");
                }, new RecordingApplier(clock), clock);
        CpfLogPolicyVersionUpdateCommand completionCommand = update("log-policy-cmd-completion-audit", 1L,
                decision("DEBUG", LogCaptureMode.NONE), "operator-a", "completion audit failure");
        require(completionAuditFailure.update(withApproval(completionCommand, "approver-b", clock)).status()
                == CpfLogPolicyVersionResult.Status.UNKNOWN_RESULT,
                "post-apply audit failure must report UNKNOWN rather than false success");
        require(completionAuditFailure.current(LogPolicyTargetType.MODULE, "PAY").status()
                        == CpfLogPolicyVersionSnapshot.Status.ACTIVE,
                "post-apply audit failure must retain the applied ACTIVE version");
        require(completionAuditFailure.runtimeStatus().health()
                        == com.cpf.core.api.logging.policy.CpfLogPolicyVersionRuntimeStatus.Health.DEGRADED,
                "completion audit failure must degrade runtime status");

        require(audits.size() >= 6, "prepare and committed audit events must be emitted");
        System.out.println("CPF_LOG_POLICY_VERSION_MANAGER_HARNESS_PASS");
    }

    private static CpfLogPolicyVersionUpdateCommand update(String id, long expected, LogPolicyDecision decision,
            String actor, String reason) {
        return new CpfLogPolicyVersionUpdateCommand(id, expected, decision, actor, reason, null);
    }
    private static CpfLogPolicyVersionUpdateCommand withApproval(CpfLogPolicyVersionUpdateCommand command,
            String approver, Clock clock) {
        return new CpfLogPolicyVersionUpdateCommand(command.commandId(), command.expectedVersion(),
                command.decision(), command.actor(), command.reason(),
                approval(command.commandHash(), approver, clock));
    }
    private static CpfLogPolicyVersionApproval approval(String hash, String approver, Clock clock) {
        return new CpfLogPolicyVersionApproval(hash, approver, clock.instant(), clock.instant().plusSeconds(60));
    }
    private static LogPolicyDecision decision(String level, LogCaptureMode bodyMode) {
        return new LogPolicyDecision(LogPolicyDecision.CURRENT_SCHEMA_VERSION, LogPolicyTargetType.MODULE.code(),
                "PAY", level, true, level, LogCaptureMode.NONE, LogCaptureMode.ALLOWLIST,
                LogCaptureMode.ALLOWLIST, bodyMode, bodyMode, LogCaptureMode.SUMMARY,
                List.of(), List.of("content-type"), List.of(), 1024, 2048, 4096, 4096, 8192,
                "DEFAULT", null, "HARNESS", null, null);
    }
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
    private static final class RecordingApplier implements CpfLogPolicyVersionApplier {
        private final Clock clock;
        private final boolean fail;
        private final AtomicInteger applied = new AtomicInteger();
        private RecordingApplier(Clock clock) { this(clock, false); }
        private RecordingApplier(Clock clock, boolean fail) { this.clock = clock; this.fail = fail; }
        @Override public CpfLogPolicyVersionSnapshot baseline(LogPolicyTargetType type, String targetId, Instant at) {
            return new CpfLogPolicyVersionSnapshot(type, targetId, 1L,
                    CpfLogPolicyVersionSnapshot.Status.ACTIVE, decision("INFO", LogCaptureMode.NONE),
                    at, "CPF_RUNTIME", "baseline");
        }
        @Override public void apply(CpfLogPolicyVersionSnapshot snapshot) {
            if (fail) throw new IllegalStateException("apply failed");
            applied.incrementAndGet();
        }
    }
    private static final class MutableClock extends Clock {
        private Instant instant;
        private MutableClock(Instant instant) { this.instant = instant; }
        @Override public ZoneId getZone() { return ZoneId.of("UTC"); }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return instant; }
        private void advance(Duration duration) { instant = instant.plus(duration); }
    }
}

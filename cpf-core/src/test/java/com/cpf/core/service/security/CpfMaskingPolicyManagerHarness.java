package com.cpf.core.service.security;

import com.cpf.core.api.security.CpfMaskingPolicyApproval;
import com.cpf.core.api.security.CpfMaskingPolicyAuditEvent;
import com.cpf.core.api.security.CpfMaskingPolicyResult;
import com.cpf.core.api.security.CpfMaskingPolicyRollbackCommand;
import com.cpf.core.api.security.CpfMaskingPolicySnapshot;
import com.cpf.core.api.security.CpfMaskingPolicyUpdateCommand;
import com.cpf.core.common.logging.SensitiveDataMasker;
import com.cpf.core.internal.security.InMemoryCpfMaskingPolicyStore;
import com.cpf.core.spi.security.CpfMaskingPolicyAuditSink;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public final class CpfMaskingPolicyManagerHarness {
    private CpfMaskingPolicyManagerHarness() { }

    public static void main(String[] args) throws Exception {
        Clock clock = Clock.fixed(Instant.parse("2026-08-05T04:00:00Z"), ZoneOffset.UTC);
        CpfMaskingPolicySnapshot initial = snapshotFromRuntime();
        List<CpfMaskingPolicyAuditEvent> audit = new ArrayList<>();
        CpfMaskingPolicyAuditSink sink = event -> {
            if (event.actorHash().contains("mask-requester") || event.approverHash().contains("mask-approver")) {
                throw new AssertionError("raw principal leaked");
            }
            audit.add(event);
        };
        InMemoryCpfMaskingPolicyStore store = new InMemoryCpfMaskingPolicyStore(initial, clock, 4, 16);
        DefaultCpfMaskingPolicyManager manager = new DefaultCpfMaskingPolicyManager(store, sink, clock);

        CpfMaskingPolicyUpdateCommand update = new CpfMaskingPolicyUpdateCommand(
                "mask-cmd-0001", initial.version(), Set.of("password", "customerMemo"), 1024, true,
                "mask-requester", "운영 마스킹 정책 변경 사유를 검증합니다 token=do-not-store", null);
        update = withApproval(update, "mask-approver", clock.instant());
        CpfMaskingPolicyResult applied = manager.update(update);
        if (applied.status() != CpfMaskingPolicyResult.Status.APPLIED
                || applied.snapshot().version() != initial.version() + 1L) {
            throw new AssertionError("policy update failed: " + applied);
        }
        if (!SensitiveDataMasker.mask("customerMemo=secret-value").contains("***")) {
            throw new AssertionError("runtime masker did not consume policy");
        }
        CpfMaskingPolicyResult replay = manager.update(update);
        if (replay.status() != CpfMaskingPolicyResult.Status.IDEMPOTENT_REPLAY) {
            throw new AssertionError("idempotent replay failed: " + replay);
        }

        CpfMaskingPolicyUpdateCommand conflict = new CpfMaskingPolicyUpdateCommand(
                update.commandId(), applied.snapshot().version(), Set.of("password", "othersecret"),
                2048, true, "mask-requester", "명령 식별자 재사용 충돌을 검증하는 사유입니다", null);
        conflict = withApproval(conflict, "mask-approver", clock.instant());
        if (manager.update(conflict).status() != CpfMaskingPolicyResult.Status.COMMAND_CONFLICT) {
            throw new AssertionError("command conflict was not rejected");
        }

        CpfMaskingPolicyUpdateCommand stale = new CpfMaskingPolicyUpdateCommand(
                "mask-cmd-0002", initial.version(), Set.of("password"), 2048, true,
                "mask-requester", "오래된 버전 정책 변경을 차단하는 사유입니다", null);
        stale = withApproval(stale, "mask-approver", clock.instant());
        if (manager.update(stale).status() != CpfMaskingPolicyResult.Status.VERSION_CONFLICT) {
            throw new AssertionError("stale version was accepted");
        }

        CpfMaskingPolicyUpdateCommand sameActor = new CpfMaskingPolicyUpdateCommand(
                "mask-cmd-0003", applied.snapshot().version(), Set.of("password"), 2048, true,
                "same-operator", "요청자와 승인자 분리를 검증하는 사유입니다", null);
        sameActor = withApproval(sameActor, "same-operator", clock.instant());
        if (manager.update(sameActor).status() != CpfMaskingPolicyResult.Status.APPROVAL_REQUIRED) {
            throw new AssertionError("separation of duties was bypassed");
        }

        CpfMaskingPolicyRollbackCommand rollback = new CpfMaskingPolicyRollbackCommand(
                "mask-cmd-0004", applied.snapshot().version(), initial.version(),
                "mask-requester", "이전 마스킹 정책으로 롤백하는 안전 사유입니다", null);
        rollback = withApproval(rollback, "mask-approver", clock.instant());
        CpfMaskingPolicyResult rolledBack = manager.rollback(rollback);
        if (rolledBack.status() != CpfMaskingPolicyResult.Status.APPLIED
                || rolledBack.snapshot().version() != initial.version() + 2L) {
            throw new AssertionError("rollback failed: " + rolledBack);
        }
        if (SensitiveDataMasker.mask("customerMemo=secret-value").contains("***")) {
            throw new AssertionError("rollback did not restore policy");
        }
        if (manager.history(10).size() < 3 || audit.size() < 5) {
            throw new AssertionError("history/audit evidence is incomplete");
        }
        for (CpfMaskingPolicyAuditEvent event : audit) {
            if (event.reason().contains("do-not-store")) throw new AssertionError("secret leaked in audit reason");
        }

        concurrency(clock, sink);
        commandTtl();
        preAuditFailure(clock);
        postAuditFailure(clock);
        durableCommitUnknown(clock);
        System.out.println("CPF_MASKING_POLICY_MANAGER_HARNESS_PASS");
    }

    private static void concurrency(Clock clock, CpfMaskingPolicyAuditSink sink) throws Exception {
        CpfMaskingPolicySnapshot initial = snapshotFromRuntime();
        InMemoryCpfMaskingPolicyStore store = new InMemoryCpfMaskingPolicyStore(initial, clock, 8, 64);
        DefaultCpfMaskingPolicyManager manager = new DefaultCpfMaskingPolicyManager(store, sink, clock);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger applied = new AtomicInteger();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread first = thread(manager, clock, start, "mask-race-0001", initial.version(), applied, failure);
        Thread second = thread(manager, clock, start, "mask-race-0002", initial.version(), applied, failure);
        first.start(); second.start(); start.countDown(); first.join(); second.join();
        if (failure.get() != null) throw new AssertionError(failure.get());
        if (applied.get() != 1) throw new AssertionError("exactly one concurrent update must win");
    }

    private static Thread thread(DefaultCpfMaskingPolicyManager manager, Clock clock,
            CountDownLatch start, String commandId, long version, AtomicInteger applied,
            AtomicReference<Throwable> failure) {
        return new Thread(() -> {
            try {
                start.await();
                CpfMaskingPolicyUpdateCommand command = new CpfMaskingPolicyUpdateCommand(
                        commandId, version, Set.of("password", commandId), 768, true,
                        "mask-requester", "동시 정책 변경 경쟁을 검증하는 안전 사유입니다", null);
                command = withApproval(command, "mask-approver", clock.instant());
                CpfMaskingPolicyResult result = manager.update(command);
                if (result.status() == CpfMaskingPolicyResult.Status.APPLIED) applied.incrementAndGet();
                else if (result.status() != CpfMaskingPolicyResult.Status.VERSION_CONFLICT) {
                    throw new AssertionError("unexpected race result " + result.status());
                }
            } catch (Throwable problem) {
                failure.compareAndSet(null, problem);
            }
        }, commandId);
    }


    private static void commandTtl() {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-05T04:00:00Z"));
        CpfMaskingPolicySnapshot initial = snapshotFromRuntime();
        InMemoryCpfMaskingPolicyStore store = new InMemoryCpfMaskingPolicyStore(
                initial, 4, 16, Duration.ofMinutes(5), clock);
        String commandId = "mask-ttl-00001";
        String firstHash = "a".repeat(64);
        CpfMaskingPolicySnapshot first = new CpfMaskingPolicySnapshot(initial.version() + 1L,
                Set.of("password", "ttlfirst"), 512, true, clock.instant(),
                "mask-requester", "first ttl policy reason");
        if (store.compareAndSet(initial.version(), commandId, firstHash, first).status()
                != com.cpf.core.spi.security.CpfMaskingPolicyStore.Status.APPLIED) {
            throw new AssertionError("initial TTL command was not applied");
        }
        CpfMaskingPolicySnapshot second = new CpfMaskingPolicySnapshot(first.version() + 1L,
                Set.of("password", "ttlsecond"), 512, true, clock.instant(),
                "mask-requester", "second ttl policy reason");
        if (store.compareAndSet(first.version(), commandId, "b".repeat(64), second).status()
                != com.cpf.core.spi.security.CpfMaskingPolicyStore.Status.COMMAND_CONFLICT) {
            throw new AssertionError("unexpired command id reuse was not rejected");
        }
        clock.advance(Duration.ofMinutes(6));
        second = new CpfMaskingPolicySnapshot(first.version() + 1L,
                Set.of("password", "ttlsecond"), 512, true, clock.instant(),
                "mask-requester", "second ttl policy reason");
        if (store.compareAndSet(first.version(), commandId, "b".repeat(64), second).status()
                != com.cpf.core.spi.security.CpfMaskingPolicyStore.Status.APPLIED) {
            throw new AssertionError("expired command record was not evicted");
        }
    }

    private static void preAuditFailure(Clock clock) {
        CpfMaskingPolicySnapshot initial = snapshotFromRuntime();
        InMemoryCpfMaskingPolicyStore store = new InMemoryCpfMaskingPolicyStore(initial, clock);
        DefaultCpfMaskingPolicyManager manager = new DefaultCpfMaskingPolicyManager(
                store, event -> { throw new IllegalStateException("audit down"); }, clock);
        CpfMaskingPolicyUpdateCommand command = new CpfMaskingPolicyUpdateCommand(
                "mask-preaudit-01", initial.version(), Set.of("password", "preauditsecret"), 512, true,
                "mask-requester", "사전 감사 장애 시 적용 차단을 검증하는 사유입니다", null);
        command = withApproval(command, "mask-approver", clock.instant());
        CpfMaskingPolicyResult result = manager.update(command);
        if (result.status() != CpfMaskingPolicyResult.Status.AUDIT_UNAVAILABLE
                || store.current().orElseThrow().version() != initial.version()) {
            throw new AssertionError("pre-audit failure must block mutation");
        }
    }

    private static void durableCommitUnknown(Clock clock) {
        CpfMaskingPolicySnapshot initial = snapshotFromRuntime();
        List<CpfMaskingPolicyAuditEvent> events = new ArrayList<>();
        com.cpf.core.spi.security.CpfMaskingPolicyStore uncertainStore =
                new com.cpf.core.spi.security.CpfMaskingPolicyStore() {
                    @Override public java.util.Optional<CpfMaskingPolicySnapshot> current() {
                        return java.util.Optional.of(initial);
                    }
                    @Override public java.util.Optional<CpfMaskingPolicySnapshot> findVersion(long version) {
                        return version == initial.version()
                                ? java.util.Optional.of(initial) : java.util.Optional.empty();
                    }
                    @Override public List<CpfMaskingPolicySnapshot> history(int limit) {
                        return List.of(initial);
                    }
                    @Override public WriteResult compareAndSet(long expectedVersion, String commandId,
                            String commandHash, CpfMaskingPolicySnapshot next) {
                        return new WriteResult(Status.UNKNOWN, next);
                    }
                    @Override public com.cpf.core.api.security.CpfMaskingPolicyRuntimeStatus runtimeStatus() {
                        return new com.cpf.core.api.security.CpfMaskingPolicyRuntimeStatus(
                                com.cpf.core.api.security.CpfMaskingPolicyRuntimeStatus.Health.DEGRADED,
                                initial.version(), 1, 0, 8, 16, 0L, 0L, clock.instant());
                    }
                };
        DefaultCpfMaskingPolicyManager manager = new DefaultCpfMaskingPolicyManager(
                uncertainStore, events::add, clock);
        CpfMaskingPolicyUpdateCommand command = new CpfMaskingPolicyUpdateCommand(
                "mask-unknown-001", initial.version(), Set.of("password", "unknownsecret"), 512, true,
                "mask-requester", "durable commit uncertainty test reason", null);
        command = withApproval(command, "mask-approver", clock.instant());
        CpfMaskingPolicyResult result = manager.update(command);
        if (result.status() != CpfMaskingPolicyResult.Status.UNKNOWN_RESULT
                || events.stream().noneMatch(event -> event.phase()
                        == CpfMaskingPolicyAuditEvent.Phase.UNKNOWN)) {
            throw new AssertionError("durable commit UNKNOWN was not preserved: " + result);
        }
    }

    private static void postAuditFailure(Clock clock) {
        CpfMaskingPolicySnapshot initial = snapshotFromRuntime();
        InMemoryCpfMaskingPolicyStore store = new InMemoryCpfMaskingPolicyStore(initial, clock);
        AtomicInteger calls = new AtomicInteger();
        DefaultCpfMaskingPolicyManager manager = new DefaultCpfMaskingPolicyManager(store, event -> {
            if (calls.incrementAndGet() == 2) throw new IllegalStateException("completion audit down");
        }, clock);
        CpfMaskingPolicyUpdateCommand command = new CpfMaskingPolicyUpdateCommand(
                "mask-postaudit-1", initial.version(), Set.of("password", "postauditsecret"), 512, true,
                "mask-requester", "사후 감사 장애 UNKNOWN 판정을 검증하는 사유입니다", null);
        command = withApproval(command, "mask-approver", clock.instant());
        CpfMaskingPolicyResult result = manager.update(command);
        if (result.status() != CpfMaskingPolicyResult.Status.UNKNOWN_RESULT
                || store.current().orElseThrow().version() != initial.version() + 1L
                || manager.runtimeStatus().health() != com.cpf.core.api.security.CpfMaskingPolicyRuntimeStatus.Health.DEGRADED) {
            throw new AssertionError("post-audit UNKNOWN semantics failed: " + result);
        }
    }

    private static CpfMaskingPolicyUpdateCommand withApproval(CpfMaskingPolicyUpdateCommand command,
            String approver, Instant now) {
        return new CpfMaskingPolicyUpdateCommand(command.commandId(), command.expectedVersion(),
                command.sensitiveKeys(), command.maxLength(), command.maskBearerToken(),
                command.actor(), command.reason(), new CpfMaskingPolicyApproval(
                command.commandHash(), approver, now.minusSeconds(1), now.plusSeconds(300)));
    }

    private static CpfMaskingPolicyRollbackCommand withApproval(CpfMaskingPolicyRollbackCommand command,
            String approver, Instant now) {
        return new CpfMaskingPolicyRollbackCommand(command.commandId(), command.expectedVersion(),
                command.targetVersion(), command.actor(), command.reason(),
                new CpfMaskingPolicyApproval(command.commandHash(), approver,
                        now.minusSeconds(1), now.plusSeconds(300)));
    }

    private static CpfMaskingPolicySnapshot snapshotFromRuntime() {
        SensitiveDataMasker.MaskingPolicy policy = SensitiveDataMasker.currentPolicy();
        return new CpfMaskingPolicySnapshot(policy.version(), policy.sensitiveKeys(), policy.maxLength(),
                policy.maskBearerToken(), policy.updatedAt(), "SYSTEM", "runtime masking policy");
    }
    private static final class MutableClock extends Clock {
        private Instant current;
        MutableClock(Instant current) { this.current = current; }
        void advance(Duration duration) { current = current.plus(duration); }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return current; }
    }

}

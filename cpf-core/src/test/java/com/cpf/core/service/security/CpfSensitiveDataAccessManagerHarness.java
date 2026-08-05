package com.cpf.core.service.security;

import com.cpf.core.api.security.CpfSensitiveDataAccessOperations.AccessApprovalCommand;
import com.cpf.core.api.security.CpfSensitiveDataAccessOperations.AccessConsumeCommand;
import com.cpf.core.api.security.CpfSensitiveDataAccessOperations.AccessRequestCommand;
import com.cpf.core.api.security.CpfSensitiveDataAccessOperations.AccessStatus;
import com.cpf.core.internal.security.InMemoryCpfSensitiveDataAccessStore;
import com.cpf.core.spi.security.CpfSensitiveDataAccessAuditSink;
import com.cpf.core.spi.security.CpfSensitiveDataAccessStore;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/** Raw view approval의 idempotency, CAS, 업무분리, scope, expiry와 one-shot 소비를 검증합니다. */
public final class CpfSensitiveDataAccessManagerHarness {
    private CpfSensitiveDataAccessManagerHarness() {
    }

    public static void main(String[] args) throws Exception {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-05T00:00:00Z"));
        AccessRequestCommand redactedCommand = new AccessRequestCommand(
                "raw-req-redact", "idem-redact", "requester-redact", "LOG", "a".repeat(64),
                "DETAIL", "incident token=raw-secret review");
        check(!redactedCommand.reason().contains("raw-secret"), "request reason must be sanitized");
        check(!redactedCommand.toString().contains("raw-secret"), "request toString must redact reason");
        List<String> audit = new CopyOnWriteArrayList<>();
        CpfSensitiveDataAccessAuditSink sink = (action, result, grant, actor, at, error) ->
                audit.add(action + ':' + result + ':' + actor);
        DefaultCpfSensitiveDataAccessManager manager = new DefaultCpfSensitiveDataAccessManager(
                new InMemoryCpfSensitiveDataAccessStore(), sink, clock);
        String resourceHash = "a".repeat(64);
        AccessRequestCommand request = new AccessRequestCommand(
                "raw-view-001", "idem-001", "operator-a", "TRANSACTION_LOG", resourceHash,
                "DETAIL:ERROR", "장애 원인 분석을 위한 승인 요청");

        var pending = manager.request(request);
        check(pending.status() == AccessStatus.PENDING && pending.grant().version() == 1L, "request pending");
        check(manager.request(request).grant().version() == 1L, "same idempotency request replays");
        AccessRequestCommand conflict = new AccessRequestCommand(
                "raw-view-001", "idem-001", "operator-a", "TRANSACTION_LOG", resourceHash,
                "DETAIL:PAYLOAD", "다른 범위로 재사용하는 승인 요청");
        check(manager.request(conflict).status() == AccessStatus.IDEMPOTENCY_CONFLICT, "idempotency conflict");

        check(manager.approve(new AccessApprovalCommand(
                "raw-view-001", 1L, "operator-a", Duration.ofMinutes(5))).status()
                == AccessStatus.SEPARATION_OF_DUTIES, "requester cannot self approve");
        var approved = manager.approve(new AccessApprovalCommand(
                "raw-view-001", 1L, "approver-b", Duration.ofMinutes(5)));
        check(approved.status() == AccessStatus.APPROVED && approved.grant().version() == 2L, "approved");
        check(manager.approve(new AccessApprovalCommand(
                "raw-view-001", 1L, "approver-c", Duration.ofMinutes(5))).status()
                == AccessStatus.VERSION_CONFLICT, "stale approval blocked");

        check(manager.consume(new AccessConsumeCommand(
                "raw-view-001", 2L, "other-user", "TRANSACTION_LOG", resourceHash, "DETAIL:ERROR")).status()
                == AccessStatus.ACCESSOR_MISMATCH, "different accessor blocked");
        check(manager.consume(new AccessConsumeCommand(
                "raw-view-001", 2L, "operator-a", "TRANSACTION_LOG", "b".repeat(64), "DETAIL:ERROR")).status()
                == AccessStatus.SCOPE_MISMATCH, "different resource blocked");
        var consumed = manager.consume(new AccessConsumeCommand(
                "raw-view-001", 2L, "operator-a", "TRANSACTION_LOG", resourceHash, "DETAIL:ERROR"));
        check(consumed.status() == AccessStatus.CONSUMED && consumed.grant().version() == 3L, "one-shot consumed");
        check(manager.consume(new AccessConsumeCommand(
                "raw-view-001", 3L, "operator-a", "TRANSACTION_LOG", resourceHash, "DETAIL:ERROR")).status()
                == AccessStatus.INVALID_STATE, "second consume blocked");

        AccessRequestCommand expiring = new AccessRequestCommand(
                "raw-view-002", "idem-002", "operator-a", "TRANSACTION_LOG", "c".repeat(64),
                "DETAIL:ERROR", "만료 동작 검증을 위한 승인 요청");
        manager.request(expiring);
        var shortApproval = manager.approve(new AccessApprovalCommand(
                "raw-view-002", 1L, "approver-b", Duration.ofSeconds(1)));
        clock.advance(Duration.ofSeconds(2));
        check(manager.consume(new AccessConsumeCommand(
                "raw-view-002", shortApproval.grant().version(), "operator-a", "TRANSACTION_LOG",
                "c".repeat(64), "DETAIL:ERROR")).status() == AccessStatus.EXPIRED, "expired grant blocked");
        check(audit.stream().anyMatch(value -> value.startsWith("CONSUME:CONSUMED")), "consume audited");
        check(audit.stream().anyMatch(value -> value.startsWith("EXPIRE:EXPIRED")), "expiry audited");

        AccessRequestCommand competing = new AccessRequestCommand(
                "raw-view-003", "idem-003", "operator-a", "TRANSACTION_LOG", "d".repeat(64),
                "DETAIL:ERROR", "동시 소비 단일 승자 검증을 위한 승인 요청");
        manager.request(competing);
        var competingApproval = manager.approve(new AccessApprovalCommand(
                "raw-view-003", 1L, "approver-b", Duration.ofMinutes(5)));
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger winners = new AtomicInteger();
        try (var executor = Executors.newFixedThreadPool(2)) {
            Future<?> left = executor.submit(() -> consumeAfter(start, manager, competingApproval.grant().version(), winners));
            Future<?> right = executor.submit(() -> consumeAfter(start, manager, competingApproval.grant().version(), winners));
            start.countDown();
            left.get();
            right.get();
        }
        check(winners.get() == 1, "concurrent consumers must have exactly one winner");

        var unavailableAudit = new DefaultCpfSensitiveDataAccessManager(
                new InMemoryCpfSensitiveDataAccessStore(), CpfSensitiveDataAccessAuditSink.unavailable(), clock);
        check(unavailableAudit.request(new AccessRequestCommand(
                "raw-view-004", "idem-004", "operator-a", "TRANSACTION_LOG", "e".repeat(64),
                "DETAIL:ERROR", "감사 저장소 장애 차단을 검증하는 승인 요청")).status()
                == AccessStatus.AUDIT_UNAVAILABLE, "audit outage fails closed before request state creation");

        InMemoryCpfSensitiveDataAccessStore recoveryStore = new InMemoryCpfSensitiveDataAccessStore();
        AtomicInteger consumeAuditFailures = new AtomicInteger(1);
        CpfSensitiveDataAccessAuditSink recoveryAudit = (action, result, grant, actor, at, error) -> {
            if ("CONSUME".equals(action) && consumeAuditFailures.getAndDecrement() > 0) {
                throw new IllegalStateException("audit unavailable after consume CAS");
            }
        };
        DefaultCpfSensitiveDataAccessManager recovering = new DefaultCpfSensitiveDataAccessManager(
                recoveryStore, recoveryAudit, clock);
        AccessRequestCommand recoveryRequest = new AccessRequestCommand(
                "raw-view-005", "idem-005", "operator-a", "TRANSACTION_LOG", "f".repeat(64),
                "DETAIL:ERROR", "감사 결과 유실 후 멱등 복구를 검증하는 승인 요청");
        recovering.request(recoveryRequest);
        var recoveryApproval = recovering.approve(new AccessApprovalCommand(
                "raw-view-005", 1L, "approver-b", Duration.ofMinutes(5)));
        AccessConsumeCommand recoveryConsume = new AccessConsumeCommand(
                "raw-view-005", recoveryApproval.grant().version(), "operator-a", "TRANSACTION_LOG",
                "f".repeat(64), "DETAIL:ERROR");
        var unknownConsume = recovering.consume(recoveryConsume);
        check(unknownConsume.status() == AccessStatus.UNKNOWN_RESULT && !unknownConsume.consumed(),
                "post-CAS audit failure is UNKNOWN and does not authorize raw output");
        var replayedConsume = recovering.consume(recoveryConsume);
        check(replayedConsume.status() == AccessStatus.IDEMPOTENT_REPLAY && replayedConsume.consumed(),
                "same consume command recovers audit and authorizes exactly one raw output");

        CpfSensitiveDataAccessStore failingStore = new CpfSensitiveDataAccessStore() {
            @Override public CreateResult createIfAbsent(com.cpf.core.api.security.CpfSensitiveDataAccessOperations.AccessGrant grant) { throw new IllegalStateException("down"); }
            @Override public java.util.Optional<com.cpf.core.api.security.CpfSensitiveDataAccessOperations.AccessGrant> find(String requestId) { throw new IllegalStateException("down"); }
            @Override public boolean compareAndSet(String requestId, long expectedVersion, com.cpf.core.api.security.CpfSensitiveDataAccessOperations.AccessGrant next) { throw new IllegalStateException("down"); }
        };
        DefaultCpfSensitiveDataAccessManager unknownStore = new DefaultCpfSensitiveDataAccessManager(
                failingStore, sink, clock);
        check(unknownStore.find("raw-view-001").status() == AccessStatus.UNKNOWN_RESULT,
                "store outage maps to UNKNOWN_RESULT instead of a raw exception");

        verifyBoundedRetentionAndReplay();
        verifyConcurrentCapacityBound();

        System.out.println("CPF_SENSITIVE_DATA_ACCESS_MANAGER_HARNESS_PASS");
    }


    private static void verifyBoundedRetentionAndReplay() {
        MutableClock capacityClock = new MutableClock(Instant.parse("2026-08-05T01:00:00Z"));
        InMemoryCpfSensitiveDataAccessStore store = new InMemoryCpfSensitiveDataAccessStore(
                2, Duration.ofSeconds(10), capacityClock);
        List<String> audit = new CopyOnWriteArrayList<>();
        DefaultCpfSensitiveDataAccessManager manager = new DefaultCpfSensitiveDataAccessManager(
                store, (action, result, grant, actor, at, error) -> audit.add(action + ':' + result), capacityClock);

        AccessRequestCommand first = capacityRequest("capacity-001", "capacity-idem-001");
        AccessRequestCommand second = capacityRequest("capacity-002", "capacity-idem-002");
        AccessRequestCommand third = capacityRequest("capacity-003", "capacity-idem-003");
        AccessRequestCommand fourth = capacityRequest("capacity-004", "capacity-idem-004");
        check(manager.request(first).status() == AccessStatus.PENDING, "first bounded grant accepted");
        check(manager.request(second).status() == AccessStatus.PENDING, "second bounded grant accepted");
        check(manager.request(third).status() == AccessStatus.RESOURCE_EXHAUSTED,
                "capacity overflow must fail closed");
        check(audit.stream().filter(value -> value.equals("REQUEST:PENDING")).count() == 2L,
                "capacity rejected request must not create a pending audit state");
        check(audit.stream().anyMatch(value -> value.equals("REQUEST_CAPACITY_REJECTED:RESOURCE_EXHAUSTED")),
                "capacity rejection must be audited");
        var exhausted = store.snapshot();
        check(exhausted.grantCount() == 2 && exhausted.maximumGrants() == 2,
                "runtime snapshot exposes bounded count");
        check(exhausted.health() == com.cpf.core.api.security.CpfSensitiveDataAccessRuntimeStatus.Health.CAPACITY_EXHAUSTED,
                "full store reports capacity exhaustion");
        check(exhausted.capacityRejectionCount() == 1L && exhausted.lastCapacityRejectionAt() != null,
                "runtime snapshot exposes rejection evidence");

        check(manager.reject(new com.cpf.core.api.security.CpfSensitiveDataAccessOperations.AccessRejectionCommand(
                first.requestId(), 1L, "approver-capacity", "용량 회수 검증을 위한 승인 거절 사유")).status()
                == AccessStatus.REJECTED, "terminal grant created");
        check(manager.request(first).status() == AccessStatus.REJECTED,
                "terminal grant must preserve replay during retention");
        capacityClock.advance(Duration.ofSeconds(9));
        check(manager.request(fourth).status() == AccessStatus.RESOURCE_EXHAUSTED,
                "terminal grant must not evict before retention");
        capacityClock.advance(Duration.ofSeconds(2));
        check(manager.request(fourth).status() == AccessStatus.PENDING,
                "retained terminal grant evicts after retention");
        var recovered = store.snapshot();
        check(recovered.grantCount() == 2 && recovered.evictionCount() == 1L,
                "post-retention create reclaims exactly one slot");
        check(manager.find(first.requestId()).status() == AccessStatus.NOT_FOUND,
                "evicted terminal grant is no longer addressable");
    }

    private static void verifyConcurrentCapacityBound() throws Exception {
        MutableClock burstClock = new MutableClock(Instant.parse("2026-08-05T02:00:00Z"));
        InMemoryCpfSensitiveDataAccessStore store = new InMemoryCpfSensitiveDataAccessStore(
                3, Duration.ofHours(1), burstClock);
        DefaultCpfSensitiveDataAccessManager manager = new DefaultCpfSensitiveDataAccessManager(
                store, (action, result, grant, actor, at, error) -> { }, burstClock);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger accepted = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();
        try (var executor = Executors.newFixedThreadPool(8)) {
            List<Future<?>> futures = new java.util.ArrayList<>();
            for (int index = 0; index < 20; index++) {
                final int requestIndex = index;
                futures.add(executor.submit(() -> {
                    try {
                        start.await();
                    } catch (InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("capacity harness interrupted", interrupted);
                    }
                    AccessStatus status = manager.request(capacityRequest(
                            "burst-" + String.format(java.util.Locale.ROOT, "%03d", requestIndex),
                            "burst-idem-" + String.format(java.util.Locale.ROOT, "%03d", requestIndex))).status();
                    if (status == AccessStatus.PENDING) accepted.incrementAndGet();
                    else if (status == AccessStatus.RESOURCE_EXHAUSTED) rejected.incrementAndGet();
                    else throw new AssertionError("unexpected burst status: " + status);
                }));
            }
            start.countDown();
            for (Future<?> future : futures) future.get();
        }
        check(accepted.get() == 3 && rejected.get() == 17,
                "concurrent request flood must preserve exact capacity bound");
        check(store.snapshot().grantCount() == 3, "concurrent store count must not exceed maximum");
    }

    private static AccessRequestCommand capacityRequest(String requestId, String idempotencyKey) {
        return new AccessRequestCommand(
                requestId, idempotencyKey, "operator-capacity", "TRANSACTION_LOG", "9".repeat(64),
                "DETAIL:ERROR", "민감정보 승인 저장소 용량 경계를 검증하는 요청 사유");
    }

    private static void consumeAfter(
            CountDownLatch start,
            DefaultCpfSensitiveDataAccessManager manager,
            long version,
            AtomicInteger winners) {
        try {
            start.await();
            if (manager.consume(new AccessConsumeCommand(
                    "raw-view-003", version, "operator-a", "TRANSACTION_LOG",
                    "d".repeat(64), "DETAIL:ERROR")).status() == AccessStatus.CONSUMED) {
                winners.incrementAndGet();
            }
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("consume harness interrupted", interrupted);
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static final class MutableClock extends Clock {
        private Instant instant;
        private MutableClock(Instant instant) { this.instant = instant; }
        private void advance(Duration duration) { instant = instant.plus(duration); }
        @Override public ZoneId getZone() { return ZoneId.of("UTC"); }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return instant; }
    }
}

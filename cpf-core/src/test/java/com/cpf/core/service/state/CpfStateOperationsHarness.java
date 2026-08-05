package com.cpf.core.service.state;

import com.cpf.core.api.state.CpfOperationState;
import com.cpf.core.api.state.CpfStateAuditEvent;
import com.cpf.core.api.state.CpfStateOperations;
import com.cpf.core.api.state.CpfStateQueryResult;
import com.cpf.core.api.state.CpfStateRuntimeStatus;
import com.cpf.core.api.state.CpfStateSearchRequest;
import com.cpf.core.api.state.CpfStateSearchResult;
import com.cpf.core.api.state.CpfStateSnapshot;
import com.cpf.core.api.state.CpfStateTransitionRequest;
import com.cpf.core.api.state.CpfStateTransitionResult;
import com.cpf.core.internal.state.InMemoryCpfStateStore;
import com.cpf.core.spi.state.CpfStateStore;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public final class CpfStateOperationsHarness {
    private CpfStateOperationsHarness() {}

    public static void main(String[] args) throws Exception {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-05T00:00:00Z"));
        InMemoryCpfStateStore store = new InMemoryCpfStateStore(100, 16, Duration.ofHours(1), clock);
        List<CpfStateAuditEvent> audit = new ArrayList<>();
        CpfStateOperations operations = new DefaultCpfStateOperations(store, audit::add, clock);

        CpfStateTransitionResult started = operations.start(
                "job:42", "op-start", "worker-a",
                "start token=raw-secret user@example.com");
        require(started.status() == CpfStateTransitionResult.Status.APPLIED, "start must apply");
        require(started.snapshot().version() == 0L, "start version");
        require(!started.snapshot().reason().contains("raw-secret"), "reason secret must be sanitized");
        require(!started.snapshot().reason().contains("user@example.com"), "reason pii must be sanitized");
        require(operations.start("job:42", "op-start", "worker-a",
                "start token=raw-secret user@example.com").status()
                == CpfStateTransitionResult.Status.IDEMPOTENT_REPLAY, "start replay");
        require(operations.start("job:42", "op-start", "worker-a", "different command").status()
                == CpfStateTransitionResult.Status.OPERATION_CONFLICT,
                "operation-id scope reuse must be rejected");

        CpfStateTransitionRequest unknownCommand = new CpfStateTransitionRequest(
                "job:42", 0L, CpfOperationState.UNKNOWN,
                "op-unknown", "worker-a", "response lost");
        CpfStateTransitionResult unknown = operations.transition(unknownCommand);
        require(unknown.applied() && unknown.snapshot().version() == 1L, "unknown transition");
        CpfStateTransitionResult resumed = operations.start(
                "job:42", "op-reconcile", "worker-b", "reconcile");
        require(resumed.applied() && resumed.snapshot().state() == CpfOperationState.RUNNING,
                "unknown must be resumable");
        CpfStateTransitionResult success = operations.transition(new CpfStateTransitionRequest(
                "job:42", 2L, CpfOperationState.SUCCEEDED,
                "op-success", "worker-b", "confirmed"));
        require(success.applied() && success.snapshot().state().terminal(), "success terminal");
        require(operations.start("job:42", "op-restart", "worker-c", "invalid").status()
                == CpfStateTransitionResult.Status.INVALID_TRANSITION,
                "terminal restart must fail");
        require(operations.transition(unknownCommand).status()
                == CpfStateTransitionResult.Status.IDEMPOTENT_REPLAY,
                "historical operation replay must survive later transitions");

        require(operations.start("job:43", "op-43", "worker", "start").applied(), "second state");
        require(operations.start("other:1", "op-other", "worker", "start").applied(), "third state");
        CpfStateSearchResult page1 = operations.search(new CpfStateSearchRequest(
                "job:", Set.of(), null, 1));
        require(page1.status() == CpfStateSearchResult.Status.SUCCESS
                && page1.items().size() == 1 && page1.nextCursor() != null, "first cursor page");
        CpfStateSearchResult page2 = operations.search(new CpfStateSearchRequest(
                "job:", Set.of(), page1.nextCursor(), 10));
        require(page2.items().size() == 1 && page2.nextCursor() == null, "second cursor page");

        InMemoryCpfStateStore concurrentStore = new InMemoryCpfStateStore();
        CpfStateOperations concurrent = new DefaultCpfStateOperations(concurrentStore, clock);
        require(concurrent.start("job:race", "race-start", "worker", "start").applied(), "race start");
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch fire = new CountDownLatch(1);
        AtomicInteger applied = new AtomicInteger();
        Runnable contender = () -> {
            ready.countDown();
            await(fire);
            CpfStateTransitionResult result = concurrent.transition(new CpfStateTransitionRequest(
                    "job:race", 0L, CpfOperationState.SUCCEEDED,
                    "race-" + Thread.currentThread().getName(), "worker", "race"));
            if (result.status() == CpfStateTransitionResult.Status.APPLIED) applied.incrementAndGet();
        };
        Thread left = new Thread(contender, "left");
        Thread right = new Thread(contender, "right");
        left.start();
        right.start();
        ready.await();
        fire.countDown();
        left.join();
        right.join();
        require(applied.get() == 1, "optimistic CAS must allow exactly one winner");

        InMemoryCpfStateStore boundedStore = new InMemoryCpfStateStore(
                1, 2, Duration.ofMinutes(5), clock);
        CpfStateOperations bounded = new DefaultCpfStateOperations(boundedStore, clock);
        require(bounded.start("bounded:1", "bounded-op-1", "worker", "start").applied(), "capacity first");
        require(bounded.start("bounded:2", "bounded-op-2", "worker", "start").status()
                == CpfStateTransitionResult.Status.RESOURCE_EXHAUSTED, "capacity fail closed");
        CpfStateRuntimeStatus.RuntimeSnapshot runtime = boundedStore.stateRuntimeSnapshot();
        require(runtime.stateCount() == 1 && runtime.resourceExhaustedCount() == 1L
                && runtime.health() == CpfStateRuntimeStatus.Health.DEGRADED, "runtime capacity status");

        AtomicInteger invalidStoreCalls = new AtomicInteger();
        CpfStateStore broken = new CpfStateStore() {
            @Override public Optional<CpfStateSnapshot> find(String key) {
                invalidStoreCalls.incrementAndGet();
                throw new IllegalStateException("secret-token-raw");
            }
            @Override public WriteResult compareAndSet(
                    String key, long expected, String operationId, String commandHash, CpfStateSnapshot next) {
                throw new IllegalStateException("secret-token-raw");
            }
        };
        CpfStateOperations failClosed = new DefaultCpfStateOperations(broken, clock);
        CpfStateTransitionResult unavailable = failClosed.start(
                "job:down", "down-start", "worker", "start");
        require(unavailable.status() == CpfStateTransitionResult.Status.STORE_UNAVAILABLE,
                "provider failure must be typed");
        require(!unavailable.message().contains("secret-token-raw"), "provider secret must not leak");
        require(failClosed.query("job:down").status() == CpfStateQueryResult.Status.STORE_UNAVAILABLE,
                "query must distinguish provider failure");
        require(failClosed.search(CpfStateSearchRequest.firstPage(10)).status()
                == CpfStateSearchResult.Status.UNSUPPORTED,
                "provider without search must report unsupported");
        int callsBeforeInvalid = invalidStoreCalls.get();
        try {
            failClosed.query("../invalid");
            throw new AssertionError("invalid state key must fail before provider access");
        } catch (IllegalArgumentException expected) {
            require(invalidStoreCalls.get() == callsBeforeInvalid, "invalid input must not reach store");
        }

        require(!audit.isEmpty(), "state decisions must be auditable");
        require(audit.stream().noneMatch(event -> event.stateKeyHash().contains("job:42")),
                "audit must hash state keys");
        require(audit.stream().noneMatch(event -> event.reason().contains("raw-secret")),
                "audit reason must be sanitized");

        System.out.println("CPF_STATE_OPERATIONS_HARNESS_PASS");
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("interrupted", interrupted);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static final class MutableClock extends Clock {
        private volatile Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return instant; }
    }
}

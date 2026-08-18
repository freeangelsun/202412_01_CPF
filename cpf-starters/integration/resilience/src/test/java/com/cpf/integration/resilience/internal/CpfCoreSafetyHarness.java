package com.cpf.integration.resilience.internal;

import com.cpf.data.lock.api.CpfLockManager;
import com.cpf.data.lock.api.CpfLockingExecutionGuard;
import com.cpf.integration.resilience.api.CpfResilienceCallContext;
import com.cpf.integration.resilience.api.CpfResilienceOutcome;
import com.cpf.integration.resilience.api.CpfResiliencePolicy;
import com.cpf.integration.resilience.api.CpfResilienceRuntimePolicy;
import com.cpf.platform.operations.observability.api.logging.DynamicLogLevelRule;
import com.cpf.data.lock.api.CpfLockManagers;
import com.cpf.testkit.lock.InMemoryCpfLockStore;
import com.cpf.data.lock.spi.CpfLockAuditSink;
import com.cpf.data.lock.spi.CpfLockStore;
import com.cpf.integration.resilience.spi.CpfResilienceAuditSink;
import com.cpf.integration.resilience.spi.CpfResilienceFailureClassifier;
import com.cpf.integration.resilience.spi.CpfResiliencePolicyStore;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.UnaryOperator;

/** Java 21 substitute runtime harness using actual product API/SPI/default implementations. */
public final class CpfCoreSafetyHarness {
    private CpfCoreSafetyHarness() {}

    public static void main(String[] args) throws Exception {
        lockStateMachineConcurrencyAndUnknown();
        lockGuardRejectsLeaseLossAfterAction();
        retryJitterBudgetDeadlineAndUnknown();
        rateLimitBulkheadAndBackpressure();
        halfOpenFailsClosedWithoutDistributedLock();
        distributedHalfOpenSingleProbe();
        cancellationPreservesInterrupt();
        policyBoundsDeadlineOverflowAndLifecycle();
        closeCancelsInFlightAttempt();
        System.out.println("CPF_CORE_SAFETY_HARNESS_PASS");
    }

    private static void lockStateMachineConcurrencyAndUnknown() throws Exception {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-05T00:00:00Z"));
        List<CpfLockAuditSink.AuditEvent> audits = new ArrayList<>();
        CpfLockManager manager = CpfLockManagers.create(new InMemoryCpfLockStore(), audits::add, clock);
        Duration lease = Duration.ofSeconds(5);
        CpfLockManager.AcquireResult first = manager.acquire("account:1", "node-a", "req-1", lease);
        check(first.status() == CpfLockManager.AcquireStatus.ACQUIRED, "first acquire");
        check(manager.acquire("account:1", "node-a", "req-1", lease).status()
                == CpfLockManager.AcquireStatus.IDEMPOTENT_REPLAY, "idempotent acquire");
        CpfLockManager.LockToken forged = new CpfLockManager.LockToken(
                first.token().key(), first.token().ownerId(), "other-request",
                first.token().fencingToken(), first.token().leaseUntil());
        check(manager.release(forged, "forged").status() == CpfLockManager.ReleaseStatus.STALE_TOKEN,
                "request id is part of token validation");
        check(manager.acquire("account:1", "node-b", "req-2", lease).status()
                == CpfLockManager.AcquireStatus.BUSY, "competing owner rejected");
        clock.advance(Duration.ofSeconds(6));
        check(manager.release(first.token(), "late").status() == CpfLockManager.ReleaseStatus.EXPIRED,
                "expired owner cannot release as current");
        CpfLockManager.AcquireResult second = manager.acquire("account:1", "node-b", "req-2", lease);
        check(second.status() == CpfLockManager.AcquireStatus.ACQUIRED, "expired lease reclaimed");
        check(second.token().fencingToken() > first.token().fencingToken(), "monotonic fencing");
        check(!manager.validateFence("account:1", first.token().fencingToken()), "stale fence rejected");
        check(manager.forceRelease("account:1", "operator", "token=secret", null).status()
                == CpfLockManager.ForceReleaseStatus.APPROVAL_REQUIRED, "approval required");
        CpfLockManager.ForceReleaseApproval unscoped = new CpfLockManager.ForceReleaseApproval(
                "approval-legacy", "approver", clock.instant(), clock.instant().plusSeconds(30));
        check(manager.forceRelease("account:1", "operator", "token=secret", unscoped).status()
                == CpfLockManager.ForceReleaseStatus.APPROVAL_SCOPE_MISMATCH,
                "legacy unscoped approval fails closed");
        CpfLockManager.LockToken originalSecondToken = second.token();
        CpfLockManager.RenewResult renewed = manager.renew(originalSecondToken, lease);
        check(renewed.status() == CpfLockManager.RenewStatus.RENEWED,
                "optimistic renew advances the row version");
        check(renewed.token().version() == originalSecondToken.version() + 1L,
                "row version increments exactly once");
        check(manager.renew(originalSecondToken, lease).status()
                == CpfLockManager.RenewStatus.STALE_TOKEN,
                "a repeated renew with the old row version is rejected");
        second = new CpfLockManager.AcquireResult(second.status(), renewed.token(), second.current(), second.reason());
        CpfLockManager.ForceReleaseCommand command = new CpfLockManager.ForceReleaseCommand(
                "account:1", "operator", "token=secret", second.token().fencingToken(),
                second.token().version());
        check(!command.toString().contains("token=secret"), "force-release command toString must redact reason");
        CpfLockManager.ForceReleaseApproval approval = CpfLockManager.ForceReleaseApproval.approve(
                "approval-1", "approver", command, clock.instant(), clock.instant().plusSeconds(30));
        CpfLockManager.ForceReleaseResult released = manager.forceRelease(
                "account:1", "operator", "token=secret", approval);
        check(released.status() == CpfLockManager.ForceReleaseStatus.RELEASED,
                "scoped approved force release");
        CpfLockManager.ForceReleaseResult replay = manager.forceRelease(
                "account:1", "operator", "token=secret", approval);
        check(replay.status() == CpfLockManager.ForceReleaseStatus.IDEMPOTENT_REPLAY
                        && java.util.Objects.equals(released.auditId(), replay.auditId()),
                "lost force-release response is recovered as an idempotent replay");
        check(audits.size() == 2 && audits.stream().noneMatch(a -> a.reason().contains("secret")),
                "authorization and result audits are masked and not duplicated by replay");

        CpfLockManager.AcquireResult bounded = manager.acquire("account:bounded", "node-a", "req-b", lease);
        CpfLockManager.ForceReleaseCommand longCommand = new CpfLockManager.ForceReleaseCommand(
                "account:bounded", "operator", "bounded", bounded.token().fencingToken(),
                bounded.token().version());
        CpfLockManager.ForceReleaseApproval longApproval = CpfLockManager.ForceReleaseApproval.approve(
                "approval-long", "approver", longCommand, clock.instant(), clock.instant().plusSeconds(3600));
        check(manager.forceRelease("account:bounded", "operator", "bounded", longApproval).status()
                == CpfLockManager.ForceReleaseStatus.APPROVAL_WINDOW_EXCEEDED,
                "critical approval validity is bounded");

        CpfLockManager unavailable = CpfLockManagers.create(new FailingStore(), null, clock);
        check(unavailable.acquire("key", "owner", "request", lease).status()
                == CpfLockManager.AcquireStatus.UNKNOWN, "store outage maps to UNKNOWN");
        check(unavailable.findResult("key").status() == CpfLockManager.QueryStatus.UNKNOWN,
                "monitoring find distinguishes storage outage from no lock");
        check(unavailable.listResult(10).status() == CpfLockManager.QueryStatus.UNKNOWN,
                "monitoring list distinguishes storage outage from an empty store");
        check(manager.listResult(0).status() == CpfLockManager.QueryStatus.INVALID,
                "monitoring list rejects an invalid limit instead of silently clamping it");

        CpfLockManager concurrent = CpfLockManagers.create(new InMemoryCpfLockStore(), null, clock);
        ExecutorService callers = Executors.newFixedThreadPool(16);
        CountDownLatch ready = new CountDownLatch(16);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger winners = new AtomicInteger();
        List<Future<?>> tasks = new ArrayList<>();
        for (int index = 0; index < 16; index++) {
            int id = index;
            tasks.add(callers.submit(() -> {
                ready.countDown();
                start.await();
                CpfLockManager.AcquireResult result = concurrent.acquire(
                        "same-key", "node-" + id, "request-" + id, Duration.ofSeconds(10));
                if (result.status() == CpfLockManager.AcquireStatus.ACQUIRED) winners.incrementAndGet();
                return null;
            }));
        }
        ready.await(5, TimeUnit.SECONDS);
        start.countDown();
        for (Future<?> task : tasks) task.get(5, TimeUnit.SECONDS);
        callers.shutdownNow();
        check(winners.get() == 1, "same-key single winner");
    }

    private static void lockGuardRejectsLeaseLossAfterAction() {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-05T00:00:00Z"));
        CpfLockManager manager = CpfLockManagers.create(new InMemoryCpfLockStore(), null, clock);
        CpfLockingExecutionGuard guard = new CpfLockingExecutionGuard(manager);
        boolean stale = false;
        try {
            guard.executeFenced("critical", "node", "request", Duration.ofMillis(100), token -> {
                check(token.fencingToken() > 0, "fence supplied to consumer");
                clock.advance(Duration.ofMillis(101));
                return "side-effect";
            });
        } catch (CpfLockingExecutionGuard.StaleFenceException expected) {
            stale = true;
        }
        check(stale, "post-action lease loss is detected");
    }

    private static void retryJitterBudgetDeadlineAndUnknown() {
        CpfResiliencePolicy base = policy("retry.zero-jitter", 2, 100, 3);
        AtomicInteger calls = new AtomicInteger();
        try (CpfResilienceEngine engine = engine(base, runtime(base, Duration.ofMillis(100),
                Duration.ofSeconds(1), Duration.ofMillis(1), Duration.ofMillis(10), 0.0d, 10), null)) {
            CpfResilienceOutcome<String> result = engine.execute(context(base.operationId(), "idem"), () -> {
                if (calls.incrementAndGet() == 1) throw new IllegalStateException("retry");
                return "OK";
            });
            check(result.status() == CpfResilienceOutcome.Status.SUCCESS && result.attempts() == 2,
                    "zero jitter retry succeeds");
        }

        CpfResiliencePolicy budgetBase = policy("retry.budget", 3, 100, 3);
        try (CpfResilienceEngine engine = engine(budgetBase, runtime(budgetBase, Duration.ofMillis(100),
                Duration.ofSeconds(1), Duration.ZERO, Duration.ZERO, 0.0d, 0), null)) {
            CpfResilienceOutcome<String> result = engine.execute(context(budgetBase.operationId(), "idem"), () -> {
                throw new IllegalStateException("retry");
            });
            check(result.status() == CpfResilienceOutcome.Status.REJECTED
                    && "RETRY_BUDGET_EXHAUSTED".equals(result.reasonCode()), "retry budget stops storm");
        }

        CpfResiliencePolicy timeoutBase = policy("write.timeout", 2, 100, 3);
        try (CpfResilienceEngine engine = engine(timeoutBase, runtime(timeoutBase, Duration.ofMillis(20),
                Duration.ofMillis(50), Duration.ZERO, Duration.ZERO, 0.0d, 10), null)) {
            CpfResilienceCallContext writeContext = CpfResilienceCallContext.recoveredLineage(
                    timeoutBase.operationId(), "tx", "idem",
                    Map.of(CpfResilienceCallContext.OPERATION_KIND_ATTRIBUTE, "WRITE"),
                    Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
            CpfResilienceOutcome<String> result = engine.execute(writeContext, () -> {
                try { Thread.sleep(200); }
                catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); }
                return "LATE";
            });
            check(result.status() == CpfResilienceOutcome.Status.UNKNOWN_RESULT
                    && "TIMEOUT_UNKNOWN_RESULT".equals(result.reasonCode()),
                    "side-effecting timeout remains UNKNOWN");
        }

        CpfResiliencePolicy preExpired = policy("pre.expired", 1, 100, 3);
        MutableClock wallClock = new MutableClock(Instant.parse("2026-08-05T00:00:10Z"));
        try (CpfResilienceEngine engine = engine(preExpired, runtime(preExpired, Duration.ofSeconds(1),
                Duration.ofSeconds(2), Duration.ZERO, Duration.ZERO, 0.0d, 1), null, wallClock)) {
            CpfResilienceOutcome<String> result = engine.execute(CpfResilienceCallContext.recoveredLineage(
                    preExpired.operationId(), "tx", null, Map.of(),
                    Clock.fixed(wallClock.instant().minusSeconds(3), ZoneOffset.UTC)),
                    () -> "MUST_NOT_RUN");
            check(result.status() == CpfResilienceOutcome.Status.TIMEOUT
                    && "DEADLINE_EXCEEDED_BEFORE_EXECUTION".equals(result.reasonCode()),
                    "upstream elapsed time consumes overall budget");
        }

        CpfResiliencePolicy nullBase = policy("null.result", 1, 100, 3);
        try (CpfResilienceEngine engine = engine(nullBase, runtime(nullBase, Duration.ofSeconds(1),
                Duration.ofSeconds(1), Duration.ZERO, Duration.ZERO, 0.0d, 1), null)) {
            CpfResilienceOutcome<String> result = engine.execute(context(nullBase.operationId(), null), () -> null);
            check(result.status() == CpfResilienceOutcome.Status.FAILED
                    && "NULL_RESULT".equals(result.reasonCode()), "null result is not retried");
        }

        CpfResiliencePolicy auditBase = policy("audit.failure", 1, 100, 3);
        try (CpfResilienceEngine engine = engine(auditBase, runtime(auditBase, Duration.ofSeconds(1),
                Duration.ofSeconds(1), Duration.ZERO, Duration.ZERO, 0.0d, 1), null,
                Clock.fixed(Instant.EPOCH, ZoneOffset.UTC),
                (a, b, c, d, e, f) -> { throw new IllegalStateException("token=secret"); })) {
            CpfResilienceOutcome<String> result = engine.execute(context(auditBase.operationId(), null), () -> "OK");
            check(result.status() == CpfResilienceOutcome.Status.UNKNOWN_RESULT
                    && result.reasonCode().startsWith("AUDIT_PERSISTENCE_FAILED"),
                    "post-side-effect audit failure is explicit UNKNOWN");
        }
    }

    private static void rateLimitBulkheadAndBackpressure() throws Exception {
        CpfResiliencePolicy rateBase = new CpfResiliencePolicy(
                "rate.limit", 1, Duration.ofSeconds(1), 1, Duration.ZERO, 3,
                Duration.ofSeconds(5), 2, 1, Duration.ofMinutes(1), true, true);
        CpfResilienceRuntimePolicy rateRuntime = runtime(
                rateBase, Duration.ofSeconds(1), Duration.ofSeconds(2),
                Duration.ZERO, Duration.ZERO, 0.0d, 1);
        try (CpfResilienceEngine engine = engine(rateBase, rateRuntime, null)) {
            check(engine.execute(context(rateBase.operationId(), null), () -> "ONE").status()
                    == CpfResilienceOutcome.Status.SUCCESS, "first rate permit succeeds");
            CpfResilienceOutcome<String> rejected = engine.execute(
                    context(rateBase.operationId(), null), () -> "TWO");
            check(rejected.status() == CpfResilienceOutcome.Status.REJECTED
                    && "RATE_LIMIT".equals(rejected.reasonCode()), "rate limit rejects excess request");
        }

        CpfResiliencePolicy bulkheadBase = new CpfResiliencePolicy(
                "bulkhead.queue", 1, Duration.ofSeconds(3), 1, Duration.ZERO, 3,
                Duration.ofSeconds(5), 1, 100, Duration.ofMinutes(1), true, true);
        CpfResilienceRuntimePolicy bulkheadRuntime = new CpfResilienceRuntimePolicy(
                bulkheadBase, Duration.ofMillis(100), Duration.ofMillis(100), Duration.ofMillis(100),
                Duration.ofSeconds(3), Duration.ofSeconds(4), Duration.ZERO, Duration.ZERO, 0.0d,
                1, Duration.ofMinutes(1), 1, Duration.ofSeconds(2));
        try (CpfResilienceEngine engine = engine(bulkheadBase, bulkheadRuntime, null)) {
            CountDownLatch firstEntered = new CountDownLatch(1);
            CountDownLatch releaseFirst = new CountDownLatch(1);
            ExecutorService callers = Executors.newFixedThreadPool(3);
            Future<CpfResilienceOutcome<String>> first = callers.submit(() -> engine.execute(
                    context(bulkheadBase.operationId(), "first"), () -> {
                        firstEntered.countDown();
                        try { releaseFirst.await(5, TimeUnit.SECONDS); }
                        catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); }
                        return "FIRST";
                    }));
            check(firstEntered.await(5, TimeUnit.SECONDS), "first bulkhead request entered");
            Future<CpfResilienceOutcome<String>> queued = callers.submit(() -> engine.execute(
                    context(bulkheadBase.operationId(), "queued"), () -> "QUEUED"));
            Thread.sleep(100);
            CpfResilienceOutcome<String> overflow = engine.execute(
                    context(bulkheadBase.operationId(), "overflow"), () -> "MUST_NOT_RUN");
            check(overflow.status() == CpfResilienceOutcome.Status.REJECTED
                    && "BULKHEAD_QUEUE_FULL".equals(overflow.reasonCode()),
                    "bulkhead queue limit provides backpressure");
            releaseFirst.countDown();
            check(first.get(5, TimeUnit.SECONDS).status() == CpfResilienceOutcome.Status.SUCCESS,
                    "active bulkhead request succeeds");
            check(queued.get(5, TimeUnit.SECONDS).status() == CpfResilienceOutcome.Status.SUCCESS,
                    "bounded queued request succeeds after permit release");
            callers.shutdownNow();
        }
    }

    private static void halfOpenFailsClosedWithoutDistributedLock() {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-05T00:00:00Z"));
        CpfResiliencePolicy base = policy("half-open.no-lock", 1, 100, 1);
        CpfResilienceRuntimePolicy runtime = runtime(base, Duration.ofSeconds(1), Duration.ofSeconds(2),
                Duration.ZERO, Duration.ZERO, 0.0d, 1);
        try (CpfResilienceEngine engine = engine(base, runtime, null, clock)) {
            engine.execute(context(base.operationId(), "open", clock), () -> {
                throw new IllegalStateException("open");
            });
            clock.advance(Duration.ofSeconds(6));
            CpfResilienceOutcome<String> result = engine.execute(
                    context(base.operationId(), "probe", clock), () -> "UNSAFE");
            check(result.status() == CpfResilienceOutcome.Status.REJECTED
                            && "DISTRIBUTED_LOCK_REQUIRED_FOR_HALF_OPEN".equals(result.reasonCode()),
                    "half-open fails closed when no cross-instance lock provider exists");
        }
    }

    private static void distributedHalfOpenSingleProbe() throws Exception {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-05T00:00:00Z"));
        CpfLockManager lockManager = CpfLockManagers.create(new InMemoryCpfLockStore(), null, clock);
        CpfResiliencePolicy base = policy("half-open", 1, 100, 1);
        CpfResilienceRuntimePolicy runtime = runtime(base, Duration.ofSeconds(2), Duration.ofSeconds(2),
                Duration.ZERO, Duration.ZERO, 0.0d, 1);
        CpfResilienceEngine first = engine(base, runtime, lockManager, clock);
        CpfResilienceEngine second = engine(base, runtime, lockManager, clock);
        try (first; second) {
            first.execute(context(base.operationId(), "a", clock), () -> { throw new IllegalStateException("open"); });
            second.execute(context(base.operationId(), "b", clock), () -> { throw new IllegalStateException("open"); });
            clock.advance(Duration.ofSeconds(6));
            CountDownLatch entered = new CountDownLatch(1);
            CountDownLatch release = new CountDownLatch(1);
            ExecutorService callers = Executors.newFixedThreadPool(2);
            Future<CpfResilienceOutcome<String>> probe = callers.submit(() -> first.execute(
                    context(base.operationId(), "probe-a", clock), () -> {
                        entered.countDown();
                        try { release.await(5, TimeUnit.SECONDS); }
                        catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); }
                        return "RECOVERED";
                    }));
            check(entered.await(5, TimeUnit.SECONDS), "first half-open probe entered");
            Future<CpfResilienceOutcome<String>> competing = callers.submit(() -> second.execute(
                    context(base.operationId(), "probe-b", clock), () -> "SHOULD_NOT_RUN"));
            CpfResilienceOutcome<String> denied = competing.get(5, TimeUnit.SECONDS);
            release.countDown();
            CpfResilienceOutcome<String> recovered = probe.get(5, TimeUnit.SECONDS);
            callers.shutdownNow();
            check(denied.status() == CpfResilienceOutcome.Status.REJECTED
                    && "HALF_OPEN_PROBE_IN_FLIGHT".equals(denied.reasonCode()),
                    "distributed half-open probe is single");
            check(recovered.status() == CpfResilienceOutcome.Status.SUCCESS, "half-open recovery succeeds");
        }
    }

    private static void cancellationPreservesInterrupt() throws Exception {
        CpfResiliencePolicy base = policy("cancel", 1, 100, 3);
        CpfResilienceRuntimePolicy runtime = runtime(base, Duration.ofSeconds(2), Duration.ofSeconds(2),
                Duration.ZERO, Duration.ZERO, 0.0d, 1);
        try (CpfResilienceEngine engine = engine(base, runtime, null)) {
            CountDownLatch actionEntered = new CountDownLatch(1);
            CountDownLatch actionRelease = new CountDownLatch(1);
            AtomicInteger interrupted = new AtomicInteger();
            Thread caller = new Thread(() -> {
                CpfResilienceOutcome<String> result = engine.execute(context(base.operationId(), null), () -> {
                    actionEntered.countDown();
                    try {
                        actionRelease.await(5, TimeUnit.SECONDS);
                    } catch (InterruptedException actionInterrupted) {
                        Thread.currentThread().interrupt();
                    }
                    return "OK";
                });
                if (result.status() == CpfResilienceOutcome.Status.REJECTED
                        && "CANCELLED".equals(result.reasonCode())
                        && Thread.currentThread().isInterrupted()) interrupted.incrementAndGet();
            });
            caller.start();
            check(actionEntered.await(5, TimeUnit.SECONDS), "attempt action entered");
            caller.interrupt();
            actionRelease.countDown();
            caller.join(5_000);
            check(interrupted.get() == 1, "caller interrupt is preserved");
        }
    }


    private static void policyBoundsDeadlineOverflowAndLifecycle() {
        boolean excessiveDuration = false;
        try {
            new CpfResiliencePolicy("too-long", 1, Duration.ofDays(366), 1,
                    Duration.ZERO, 1, Duration.ofSeconds(1), 1,
                    1, Duration.ofSeconds(1), true, true);
        } catch (IllegalArgumentException expected) {
            excessiveDuration = true;
        }
        check(excessiveDuration, "excessive duration fails closed");

        boolean excessiveNumeric = false;
        try {
            new CpfResiliencePolicy("too-many", 1, Duration.ofSeconds(1), 1_000_001,
                    Duration.ZERO, 1, Duration.ofSeconds(1), 1,
                    1, Duration.ofSeconds(1), true, true);
        } catch (IllegalArgumentException expected) {
            excessiveNumeric = true;
        }
        check(excessiveNumeric, "excessive numeric limit fails closed");

        CpfResiliencePolicy base = policy("lifecycle", 1, 100, 3);
        CpfResilienceRuntimePolicy runtime = runtime(base, Duration.ofSeconds(1), Duration.ofSeconds(2),
                Duration.ZERO, Duration.ZERO, 0.0d, 1);
        CpfResilienceEngine engine = engine(base, runtime, null);
        engine.close();
        engine.close();
        CpfResilienceOutcome<String> closed = engine.execute(context(base.operationId(), "closed"), () -> "NO");
        check(closed.status() == CpfResilienceOutcome.Status.REJECTED
                        && "ENGINE_CLOSED".equals(closed.reasonCode()),
                "closed engine rejects new work deterministically");
    }


    private static void closeCancelsInFlightAttempt() throws Exception {
        CpfResiliencePolicy base = policy("close-inflight", 1, 100, 3);
        CpfResilienceRuntimePolicy runtime = runtime(base, Duration.ofSeconds(10), Duration.ofSeconds(10),
                Duration.ZERO, Duration.ZERO, 0.0d, 1);
        CpfResilienceEngine engine = engine(base, runtime, null);
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch interrupted = new CountDownLatch(1);
        ExecutorService caller = Executors.newSingleThreadExecutor();
        try {
            Future<CpfResilienceOutcome<String>> result = caller.submit(() -> engine.execute(
                    context(base.operationId(), "close"), () -> {
                        entered.countDown();
                        try {
                            new CountDownLatch(1).await();
                            return "MUST_NOT_COMPLETE";
                        } catch (InterruptedException cancelled) {
                            interrupted.countDown();
                            Thread.currentThread().interrupt();
                            throw new IllegalStateException("attempt cancelled by engine close", cancelled);
                        }
                    }));
            check(entered.await(5, TimeUnit.SECONDS), "in-flight attempt entered");
            engine.close();
            CpfResilienceOutcome<String> closed = result.get(5, TimeUnit.SECONDS);
            check(interrupted.await(5, TimeUnit.SECONDS), "close interrupts in-flight action");
            check(closed.status() == CpfResilienceOutcome.Status.REJECTED
                            && "ENGINE_CLOSED_DURING_EXECUTION".equals(closed.reasonCode()),
                    "in-flight close has deterministic rejected outcome");
        } finally {
            engine.close();
            caller.shutdownNow();
        }
    }

    private static CpfResilienceEngine engine(
            CpfResiliencePolicy base, CpfResilienceRuntimePolicy runtime, CpfLockManager lockManager) {
        return engine(base, runtime, lockManager, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
    }

    private static CpfResilienceEngine engine(
            CpfResiliencePolicy base, CpfResilienceRuntimePolicy runtime,
            CpfLockManager lockManager, Clock clock) {
        return engine(base, runtime, lockManager, clock, (a, b, c, d, e, f) -> {});
    }

    private static CpfResilienceEngine engine(
            CpfResiliencePolicy base, CpfResilienceRuntimePolicy runtime,
            CpfLockManager lockManager, Clock clock, CpfResilienceAuditSink audit) {
        return CpfResilienceTestSupport.engine(
                new OnePolicy(base),
                failure -> CpfResilienceFailureClassifier.Classification.RETRYABLE,
                audit,
                (policy, context) -> runtime,
                lockManager,
                clock,
                Executors.newVirtualThreadPerTaskExecutor(),
                () -> 0.5d,
                System::nanoTime);
    }

    private static CpfResiliencePolicy policy(
            String operation, int attempts, int ratePermits, int failureThreshold) {
        return new CpfResiliencePolicy(operation, 1, Duration.ofSeconds(1), attempts,
                Duration.ZERO, failureThreshold, Duration.ofSeconds(5), 2,
                ratePermits, Duration.ofMinutes(1), true, true);
    }

    private static CpfResilienceRuntimePolicy runtime(
            CpfResiliencePolicy base, Duration attempt, Duration overall,
            Duration initialBackoff, Duration maxBackoff, double jitter, int retryBudget) {
        return new CpfResilienceRuntimePolicy(base,
                Duration.ofMillis(100), Duration.ofMillis(100), Duration.ofMillis(100),
                attempt, overall, initialBackoff, maxBackoff, jitter,
                retryBudget, Duration.ofMinutes(1), 2, Duration.ofMillis(20));
    }

    private static CpfResilienceCallContext context(String operation, String idempotencyKey) {
        return context(operation, idempotencyKey, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
    }

    private static CpfResilienceCallContext context(
            String operation, String idempotencyKey, Clock clock) {
        return CpfResilienceCallContext.recoveredLineage(
                operation, "tx-" + operation, idempotencyKey, Map.of(), clock);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private record OnePolicy(CpfResiliencePolicy policy) implements CpfResiliencePolicyStore {
        @Override public Optional<CpfResiliencePolicy> findActive(String operationId) { return Optional.of(policy); }
        @Override public List<CpfResiliencePolicy> search(String filter, int offset, int limit) { return List.of(policy); }
        @Override public String request(CpfResiliencePolicy value, String requester, String reason) { return "request"; }
        @Override public CpfResiliencePolicy approve(String requestId, String approver, String reason) { return policy; }
        @Override public void reject(String requestId, String approver, String reason) {}
    }

    private static final class FailingStore implements CpfLockStore {
        @Override public UpdateResult update(String key, UnaryOperator<StoredLock> transition) {
            throw new IllegalStateException("store down");
        }
        @Override public Optional<StoredLock> find(String key) { throw new IllegalStateException("store down"); }
        @Override public List<StoredLock> list(int limit) { throw new IllegalStateException("store down"); }
        @Override public long nextFence(String key) { throw new IllegalStateException("store down"); }
    }

    private static final class MutableClock extends Clock {
        private final AtomicLong millis;
        private MutableClock(Instant initial) { millis = new AtomicLong(initial.toEpochMilli()); }
        private void advance(Duration duration) { millis.addAndGet(duration.toMillis()); }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return Instant.ofEpochMilli(millis.get()); }
        @Override public long millis() { return millis.get(); }
    }
}

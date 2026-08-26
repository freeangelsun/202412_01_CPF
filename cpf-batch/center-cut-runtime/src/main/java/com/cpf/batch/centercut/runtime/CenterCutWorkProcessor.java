package com.cpf.batch.centercut.runtime;

import com.cpf.batch.context.CpfCenterCutContext;
import com.cpf.batch.context.CpfBatchContext;
import com.cpf.batch.context.CpfBatchContextBundle;
import com.cpf.batch.context.CpfBatchLaunchMode;
import com.cpf.batch.execution.internal.context.CpfBatchRuntimeContexts;
import com.cpf.batch.runtime.LogContext;
import com.cpf.batch.runtime.SensitiveTextSanitizer;
import com.cpf.batch.spi.CenterCutHandler;
import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.core.api.transaction.CpfTransactionIds;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 실제 Worker 프로세스에서 DB Work Item을 Claim/Lease/Fencing하고 공식 Domain Invocation Handler로 처리합니다.
 */
public final class CenterCutWorkProcessor implements AutoCloseable {
    public static final String LEASE_LOST = "BAT_CENTER_CUT_LEASE_LOST";
    private static final DateTimeFormatter BUSINESS_DATE = DateTimeFormatter.BASIC_ISO_DATE;

    private final JdbcCenterCutClaimRepository repository;
    private final Map<String, CenterCutHandler> handlers;
    private final List<CenterCutWorkObserver> observers;
    private final CpfExecutionIdGenerator executionIds;
    private final String runtimeSystemCode;
    private final ScheduledExecutorService leaseHeartbeats;

    public CenterCutWorkProcessor(
            JdbcCenterCutClaimRepository repository,
            List<CenterCutHandler> handlers,
            List<CenterCutWorkObserver> observers,
            CpfExecutionIdGenerator executionIds,
            String runtimeSystemCode) {
        this(repository, handlers, observers, executionIds, runtimeSystemCode,
                Executors.newSingleThreadScheduledExecutor(
                        Thread.ofVirtual().name("cpf-center-cut-db-lease-", 0).factory()));
    }

    CenterCutWorkProcessor(
            JdbcCenterCutClaimRepository repository,
            List<CenterCutHandler> handlers,
            List<CenterCutWorkObserver> observers,
            CpfExecutionIdGenerator executionIds,
            String runtimeSystemCode,
            ScheduledExecutorService leaseHeartbeats) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.observers = observers == null ? List.of() : List.copyOf(observers);
        this.executionIds = Objects.requireNonNull(executionIds, "executionIds");
        this.runtimeSystemCode = requiredSystemCode(runtimeSystemCode);
        this.leaseHeartbeats = Objects.requireNonNull(leaseHeartbeats, "leaseHeartbeats");
        LinkedHashMap<String, CenterCutHandler> indexed = new LinkedHashMap<>();
        for (CenterCutHandler handler : handlers == null ? List.<CenterCutHandler>of() : handlers) {
            String key = requiredKey(handler.handlerKey(), "handlerKey");
            if (indexed.putIfAbsent(key, handler) != null) {
                throw new IllegalStateException("Duplicate Center-Cut handler: " + key);
            }
        }
        this.handlers = Map.copyOf(indexed);
    }

    /** 한 번의 공정한 poll에서 최대 한 Item만 처리해 여러 Worker가 같은 실행을 공유하게 합니다. */
    public Optional<Outcome> processNext(
            String runnerId, String pool, Duration leaseDuration, Duration heartbeatInterval) {
        String runner = requiredKey(runnerId, "runnerId");
        String workerPool = requiredKey(pool, "pool");
        Duration lease = positive(leaseDuration, "leaseDuration");
        Duration heartbeat = positive(heartbeatInterval, "heartbeatInterval");
        if (heartbeat.compareTo(lease.dividedBy(2)) > 0) {
            throw new IllegalArgumentException("heartbeatInterval must not exceed half the lease duration");
        }
        Optional<JdbcCenterCutClaimRepository.Claim> claimed;
        try {
            repository.recoverExpiredToUnknown();
            claimed = repository.claimNext(runner, workerPool, lease);
            healthy();
        } catch (RuntimeException failure) {
            failed(failure);
            throw failure;
        }
        if (claimed.isEmpty()) return Optional.empty();
        return Optional.of(process(claimed.get(), lease, heartbeat));
    }

    private Outcome process(
            JdbcCenterCutClaimRepository.Claim claim, Duration lease, Duration heartbeat) {
        JdbcCenterCutClaimRepository.Work work = repository.load(claim);
        observers.forEach(observer -> observer.claimed(claim, work));
        try {
            CenterCutHandler handler = handlers.get(work.handlerKey());
            if (handler == null) {
                repository.complete(claim, "FAILED", null,
                        "Approved Center-Cut handler is not installed");
                return new Outcome(work.itemId(), work.executionId(), "FAILED",
                        "CENTER_CUT_HANDLER_UNAVAILABLE", claim.fencingToken());
            }
            CpfContextSnapshot snapshot = itemContext(work, claim);
            CpfBatchContextBundle batch = itemBatchContext(work, claim, snapshot);
            try (AutoCloseable _ = CpfContexts.bind(snapshot);
                 AutoCloseable _ = CpfBatchRuntimeContexts.bind(batch);
                 LeaseGuard guard = new LeaseGuard(claim, lease, heartbeat);
                 LogContext _ = LogContext.open(Map.of(
                         "transactionId", work.transactionId(),
                         "segmentId", work.segmentId(),
                         "executionId", work.executionId(),
                         "jobId", work.jobCode()))) {
                CenterCutHandler.Result handled = handler.handle(new CenterCutHandler.Context(
                        work.jobCode(), work.itemId(), work.businessKey(), work.payload(),
                        work.transactionId(), work.segmentId(), claim.fencingToken()));
                guard.assertOwned();
                Completion completion = completion(work, handled);
                repository.complete(claim, completion.status(), handled == null ? null : handled.payload(),
                        completion.message());
                healthy();
                return new Outcome(work.itemId(), work.executionId(), completion.status(),
                        completion.code(), claim.fencingToken());
            }
        } catch (LeaseLostException lost) {
            observers.forEach(observer -> observer.leaseLost(claim));
            return new Outcome(work.itemId(), work.executionId(), "UNKNOWN_RESULT",
                    LEASE_LOST, claim.fencingToken());
        } catch (RuntimeException failure) {
            String detail = SensitiveTextSanitizer.sanitize(failure.getMessage());
            try {
                repository.complete(claim, "UNKNOWN_RESULT", null, detail);
                healthy();
            } catch (RuntimeException stale) {
                failed(stale);
            }
            return new Outcome(work.itemId(), work.executionId(), "UNKNOWN_RESULT",
                    "CENTER_CUT_HANDLER_UNKNOWN_RESULT", claim.fencingToken());
        } catch (Exception failure) {
            throw new IllegalStateException("Center-Cut Worker context close failed", failure);
        } finally {
            observers.forEach(observer -> observer.released(claim));
        }
    }

    private CpfContextSnapshot itemContext(
            JdbcCenterCutClaimRepository.Work work,
            JdbcCenterCutClaimRepository.Claim claim) {
        String transactionId = CpfTransactionIds.requireCanonical(work.transactionId());
        String issuer = CpfTransactionIds.issuerCode(transactionId);
        Instant now = Instant.now();
        CpfContext.CpfTransactionContext transaction = new CpfContext.CpfTransactionContext(
                transactionId, transactionId, null, transactionId, transactionId,
                issuer, runtimeSystemCode, null, null,
                null, null, null, null,
                LocalDate.parse(transactionId.substring(0, 8), BUSINESS_DATE), now,
                CpfContext.CpfTransactionOriginKind.BATCH, issuer, null);
        CpfContext.CpfExecutionContext execution = new CpfContext.CpfExecutionContext(
                work.executionId(), executionIds.newExecutionId(), work.executionId(), null,
                requiredIdentifier(work.segmentId(), "segmentId"),
                requiredIdentifier(work.parentSegmentId(), "parentSegmentId"),
                CpfContext.CpfExecutionType.BATCH, Math.max(1, work.retryCount() + 1), 1,
                now, null, CpfContext.CpfCancellationMode.DEADLINE_ENFORCED);
        CpfContext.CpfOperationContext operation = new CpfContext.CpfOperationContext(
                "BAT_CENTER_CUT_WORK", "BAT Center-Cut Work", work.businessKey(),
                work.executionId() + ":" + work.businessKey(),
                CpfContext.CpfIdempotencyScope.CURRENT_OPERATION,
                CpfContext.CpfIdempotencyMode.REQUIRED,
                null, null, null, 1L);
        CpfContext context = new CpfContext(transaction, execution, operation, null, null);
        return CpfContextSnapshot.capture(context);
    }

    private static CpfBatchContextBundle itemBatchContext(
            JdbcCenterCutClaimRepository.Work work,
            JdbcCenterCutClaimRepository.Claim claim,
            CpfContextSnapshot snapshot) {
        LocalDate businessDate = LocalDate.parse(
                work.transactionId().substring(0, 8), BUSINESS_DATE);
        int attempt = Math.max(1, work.retryCount() + 1);
        CpfBatchContext batch = new CpfBatchContext(
                work.jobCode(), work.jobCode(), 1,
                work.executionId(), work.executionId(), null,
                "center-cut-db-work", Long.toString(work.itemId()), null, null,
                CpfBatchLaunchMode.MANUAL, businessDate, work.retryCount(), attempt,
                Long.toString(work.itemId()), work.businessKey(), null, null,
                claim.runnerId(), "db-worker", Long.toString(work.itemId()), null,
                work.executionId(), null, null, claim.fencingToken(), Instant.now());
        CpfCenterCutContext centerCut = new CpfCenterCutContext(
                work.executionId(), "db-worker", Long.toString(work.itemId()), null,
                work.executionId(), null, null, claim.runnerId(), claim.fencingToken(),
                attempt, work.executionId(), null, Long.toString(work.itemId()));
        return new CpfBatchContextBundle(snapshot, batch, centerCut);
    }

    private static Completion completion(
            JdbcCenterCutClaimRepository.Work work, CenterCutHandler.Result handled) {
        if (handled == null || handled.status() == null) {
            return new Completion("UNKNOWN_RESULT", "CENTER_CUT_UNKNOWN_RESULT",
                    "Center-Cut Handler returned no result");
        }
        String status = handled.status().trim().toUpperCase(Locale.ROOT);
        String message = SensitiveTextSanitizer.sanitize(handled.message());
        return switch (status) {
            case "SUCCESS", "COMPLETED" -> new Completion("SUCCESS", "CENTER_CUT_COMPLETED", message);
            case "UNKNOWN", "UNKNOWN_RESULT" ->
                    new Completion("UNKNOWN_RESULT", "CENTER_CUT_UNKNOWN_RESULT", message);
            default -> {
                if (handled.retryable() && work.retryCount() < work.retryLimit()) {
                    yield new Completion("RETRY", "CENTER_CUT_RETRYABLE", message);
                }
                if (handled.retryable()) {
                    yield new Completion("FAILED", "CENTER_CUT_RETRY_LIMIT_EXHAUSTED",
                            "CENTER_CUT_RETRY_LIMIT_EXHAUSTED: " + Objects.toString(message, ""));
                }
                yield new Completion("FAILED", "CENTER_CUT_FAILED", message);
            }
        };
    }

    private void healthy() { observers.forEach(value -> value.repositoryHealthy()); }
    private void failed(RuntimeException failure) {
        observers.forEach(observer -> observer.repositoryFailure(failure));
    }

    @PreDestroy
    @Override
    public void close() {
        leaseHeartbeats.shutdown();
    }

    public record Outcome(long itemId, String executionId, String status, String code, long fencingToken) { }
    private record Completion(String status, String code, String message) { }

    private final class LeaseGuard implements AutoCloseable {
        private final JdbcCenterCutClaimRepository.Claim claim;
        private final Duration lease;
        private final AtomicBoolean owned = new AtomicBoolean(true);
        private final ScheduledFuture<?> task;

        private LeaseGuard(
                JdbcCenterCutClaimRepository.Claim claim, Duration lease, Duration heartbeat) {
            this.claim = claim;
            this.lease = lease;
            renew();
            assertOwned();
            task = leaseHeartbeats.scheduleAtFixedRate(
                    this::renew, heartbeat.toMillis(), heartbeat.toMillis(), TimeUnit.MILLISECONDS);
        }

        private void renew() {
            if (!owned.get()) return;
            try {
                if (!repository.renew(claim, lease)) owned.set(false);
            } catch (RuntimeException failure) {
                owned.set(false);
                failed(failure);
            }
        }

        private void assertOwned() {
            if (!owned.get()) throw new LeaseLostException();
        }

        @Override public void close() { task.cancel(false); }
    }

    private static final class LeaseLostException extends RuntimeException { }

    private static Duration positive(Duration value, String name) {
        if (value == null || value.isZero() || value.isNegative()) {
            throw new IllegalArgumentException(name + " must be positive");
        }
        return value;
    }

    private static String requiredKey(String value, String name) {
        String normalized = requiredIdentifier(value, name);
        if (!normalized.matches("[A-Za-z0-9._:-]{1,160}")) {
            throw new IllegalArgumentException("Invalid Center-Cut " + name);
        }
        return normalized;
    }

    private static String requiredIdentifier(String value, String name) {
        if (value == null || value.isBlank() || value.length() > 160
                || value.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Invalid Center-Cut " + name);
        }
        return value.trim();
    }

    private static String requiredSystemCode(String value) {
        String normalized = requiredIdentifier(value, "runtimeSystemCode").toUpperCase(Locale.ROOT);
        if (!normalized.matches("[A-Z0-9][A-Z0-9_-]{0,31}")) {
            throw new IllegalArgumentException("Invalid Center-Cut runtimeSystemCode");
        }
        return normalized;
    }
}

package com.cpf.batch.centercut.runner;

import com.cpf.batch.api.BatchJobDefinition;
import com.cpf.batch.centercut.runner.internal.JdbcCenterCutClaimRepository;
import com.cpf.batch.runtime.LogContext;
import com.cpf.batch.runtime.SensitiveTextSanitizer;
import com.cpf.batch.spi.BatchStepHandler;
import com.cpf.batch.spi.CenterCutHandler;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
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
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Center-Cut item 처리를 Spring Batch StepExecution 내부에서만 수행하는 Product Consumer입니다. */
@Component
public final class SpringBatchCenterCutStepHandler implements BatchStepHandler, AutoCloseable {
    static final String REFERENCE_PREFIX = "CENTER_CUT:";
    private static final String EXECUTION_PARAMETER = "centerCutExecutionId";

    private final JdbcCenterCutClaimRepository repository;
    private final Map<String, CenterCutHandler> handlers;
    private final SpringBatchCenterCutRuntimeState runtimeState;
    private final String runnerId;
    private final String pool;
    private final Duration leaseDuration;
    private final long heartbeatMillis;
    private final ScheduledExecutorService leaseHeartbeats;

    public SpringBatchCenterCutStepHandler(
            JdbcCenterCutClaimRepository repository,
            List<CenterCutHandler> handlers,
            SpringBatchCenterCutRuntimeState runtimeState,
            @Value("${cpf.center-cut.runner-id:${CPF_INSTANCE_ID:center-cut-local-01}}") String runnerId,
            @Value("${cpf.center-cut.pool:center-cut}") String pool,
            @Value("${cpf.center-cut.lease-seconds:30}") long leaseSeconds,
            @Value("${cpf.center-cut.heartbeat-ms:5000}") long heartbeatMillis) {
        this(repository, handlers, runtimeState, runnerId, pool,
                Duration.ofSeconds(Math.max(10, leaseSeconds)), heartbeatMillis,
                Executors.newSingleThreadScheduledExecutor(
                        Thread.ofVirtual().name("cpf-center-cut-lease-", 0).factory()));
    }

    SpringBatchCenterCutStepHandler(
            JdbcCenterCutClaimRepository repository,
            List<CenterCutHandler> handlers,
            SpringBatchCenterCutRuntimeState runtimeState,
            String runnerId,
            String pool,
            Duration leaseDuration,
            long heartbeatMillis,
            ScheduledExecutorService leaseHeartbeats) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.runtimeState = Objects.requireNonNull(runtimeState, "runtimeState");
        this.runnerId = requiredText(runnerId, "runnerId");
        this.pool = requiredText(pool, "pool");
        this.leaseDuration = Objects.requireNonNull(leaseDuration, "leaseDuration");
        if (leaseDuration.isZero() || leaseDuration.isNegative()) {
            throw new IllegalArgumentException("leaseDuration must be positive");
        }
        this.heartbeatMillis = Math.max(1L, Math.min(heartbeatMillis, leaseDuration.toMillis() / 2));
        this.leaseHeartbeats = Objects.requireNonNull(leaseHeartbeats, "leaseHeartbeats");
        LinkedHashMap<String, CenterCutHandler> indexed = new LinkedHashMap<>();
        for (CenterCutHandler handler : handlers) {
            String key = requiredHandlerKey(handler.handlerKey());
            if (indexed.putIfAbsent(key, handler) != null) {
                throw new IllegalStateException("Duplicate Center-Cut handler: " + key);
            }
        }
        this.handlers = Map.copyOf(indexed);
    }

    @Override
    public boolean supports(BatchJobDefinition.ExecutorType type, String reference) {
        return type == BatchJobDefinition.ExecutorType.SPRING_BATCH
                && reference != null
                && reference.startsWith(REFERENCE_PREFIX)
                && reference.length() > REFERENCE_PREFIX.length();
    }

    @Override
    public BatchStepResult execute(BatchStepCommand command) {
        if (!supports(command.step().executorType(), command.step().executorReference())) {
            throw new IllegalArgumentException("Center-Cut requires SPRING_BATCH/CENTER_CUT:<handlerKey>");
        }
        String centerCutExecutionId = executionId(command);
        String approvedHandlerKey = requiredHandlerKey(
                command.step().executorReference().substring(REFERENCE_PREFIX.length()));
        long read = 0;
        long written = 0;
        long lastItemId = 0;
        try (SpringBatchCenterCutRuntimeState.Scope scope = runtimeState.begin(
                command.cpfExecutionId(), command.jobExecutionId(), command.fencingToken())) {
            try {
                repository.recoverExpiredToUnknown();
                runtimeState.repositoryHealthy();
                while (runtimeState.accepting()) {
                    Optional<JdbcCenterCutClaimRepository.Claim> claimed = repository.claimForExecution(
                            centerCutExecutionId, runnerId, pool, leaseDuration);
                    runtimeState.repositoryHealthy();
                    if (claimed.isEmpty()) {
                        return BatchStepResult.completed("CENTER_CUT_COMPLETED", read, written,
                                checkpoint(centerCutExecutionId, lastItemId));
                    }
                    JdbcCenterCutClaimRepository.Claim claim = claimed.get();
                    scope.claim(claim.claimToken());
                    lastItemId = claim.itemId();
                    read++;
                    BatchStepResult result = executeClaim(
                            claim, centerCutExecutionId, approvedHandlerKey);
                    scope.releaseClaim();
                    written += result.writeCount();
                    if (result.status() != Status.COMPLETED) {
                        return new BatchStepResult(result.status(), result.code(), result.message(),
                                read, written, result.skipCount(), checkpoint(centerCutExecutionId, lastItemId));
                    }
                }
                return new BatchStepResult(Status.STOPPED, SpringBatchCenterCutRuntimeState.DISABLED,
                        "Center-Cut admission was disabled", read, written, 0,
                        checkpoint(centerCutExecutionId, lastItemId));
            } catch (RuntimeException failure) {
                runtimeState.repositoryFailure(failure);
                throw failure;
            }
        }
    }

    private BatchStepResult executeClaim(
            JdbcCenterCutClaimRepository.Claim claim,
            String centerCutExecutionId,
            String approvedHandlerKey) {
        JdbcCenterCutClaimRepository.Work work = repository.load(claim);
        if (!centerCutExecutionId.equals(work.executionId())) {
            throw new SecurityException("CENTER_CUT_EXECUTION_BINDING_MISMATCH");
        }
        if (!approvedHandlerKey.equals(work.handlerKey())) {
            repository.complete(claim, "FAILED", null, "Approved Center-Cut handler binding mismatch");
            return new BatchStepResult(Status.FAILED, "CENTER_CUT_HANDLER_BINDING_MISMATCH",
                    "Approved Center-Cut handler binding mismatch", 1, 0, 0, Map.of());
        }
        CenterCutHandler handler = handlers.get(approvedHandlerKey);
        if (handler == null) {
            repository.complete(claim, "FAILED", null, "Approved Center-Cut handler is not installed");
            return new BatchStepResult(Status.FAILED, "CENTER_CUT_HANDLER_UNAVAILABLE",
                    "Approved Center-Cut handler is not installed", 1, 0, 0, Map.of());
        }

        try (LeaseGuard lease = startLeaseGuard(claim);
             LogContext ignored = LogContext.open(Map.of(
                     "transactionId", Objects.toString(work.transactionId(), ""),
                     "segmentId", Objects.toString(work.segmentId(), ""),
                     "executionId", centerCutExecutionId,
                     "jobId", work.jobCode()))) {
            CenterCutHandler.Result handled = handler.handle(new CenterCutHandler.Context(
                    work.jobCode(), work.itemId(), work.businessKey(), work.payload(),
                    work.transactionId(), work.segmentId(), claim.fencingToken()));
            lease.assertOwned();
            String itemStatus = itemStatus(handled);
            repository.complete(claim, itemStatus, handled.payload(), handled.message());
            runtimeState.repositoryHealthy();
            return result(itemStatus, handled);
        } catch (LeaseLostException lost) {
            runtimeState.leaseLost();
            return new BatchStepResult(Status.UNKNOWN_RESULT, SpringBatchCenterCutRuntimeState.LEASE_LOST,
                    "Center-Cut item lease was lost", 1, 0, 0, Map.of());
        } catch (Exception failure) {
            String detail = SensitiveTextSanitizer.sanitize(failure.getMessage());
            try {
                repository.complete(claim, "UNKNOWN_RESULT", null, detail);
                runtimeState.repositoryHealthy();
            } catch (RuntimeException stale) {
                runtimeState.repositoryFailure(stale);
            }
            return new BatchStepResult(Status.UNKNOWN_RESULT, "CENTER_CUT_HANDLER_UNKNOWN_RESULT",
                    detail, 1, 0, 0, Map.of());
        }
    }

    private LeaseGuard startLeaseGuard(JdbcCenterCutClaimRepository.Claim claim) {
        LeaseGuard guard = new LeaseGuard(claim);
        guard.renew();
        guard.assertOwned();
        guard.start();
        return guard;
    }

    private static BatchStepResult result(String itemStatus, CenterCutHandler.Result handled) {
        return switch (itemStatus) {
            case "SUCCESS" -> BatchStepResult.completed(handled.message(), 1, 1, Map.of());
            case "RETRY" -> new BatchStepResult(Status.RETRYABLE_FAILURE, "CENTER_CUT_RETRYABLE",
                    handled.message(), 1, 0, 0, Map.of());
            case "UNKNOWN_RESULT" -> new BatchStepResult(Status.UNKNOWN_RESULT, "CENTER_CUT_UNKNOWN_RESULT",
                    handled.message(), 1, 0, 0, Map.of());
            default -> new BatchStepResult(Status.FAILED, "CENTER_CUT_FAILED",
                    handled.message(), 1, 0, 0, Map.of());
        };
    }

    private static String itemStatus(CenterCutHandler.Result handled) {
        if (handled == null || handled.status() == null) return "UNKNOWN_RESULT";
        return switch (handled.status().trim().toUpperCase(Locale.ROOT)) {
            case "SUCCESS", "COMPLETED" -> "SUCCESS";
            case "UNKNOWN", "UNKNOWN_RESULT" -> "UNKNOWN_RESULT";
            default -> handled.retryable() ? "RETRY" : "FAILED";
        };
    }

    private static Map<String, Object> checkpoint(String executionId, long lastItemId) {
        return lastItemId == 0
                ? Map.of("centerCut.executionId", executionId)
                : Map.of("centerCut.executionId", executionId, "centerCut.lastItemId", lastItemId);
    }

    private static String executionId(BatchStepCommand command) {
        Object defined = command.step().parameters().get(EXECUTION_PARAMETER);
        Object supplied = command.jobParameters().get("arg." + EXECUTION_PARAMETER);
        String fromDefinition = Objects.toString(defined, "").trim();
        String fromRequest = Objects.toString(supplied, "").trim();
        if (!fromDefinition.isEmpty() && !fromRequest.isEmpty() && !fromDefinition.equals(fromRequest)) {
            throw new SecurityException("CENTER_CUT_EXECUTION_PARAMETER_MISMATCH");
        }
        String value = !fromRequest.isEmpty() ? fromRequest : fromDefinition;
        if (!value.matches("[A-Za-z0-9._:-]{1,120}")) {
            throw new IllegalArgumentException("centerCutExecutionId is required");
        }
        return value;
    }

    private static String requiredHandlerKey(String value) {
        String key = requiredText(value, "handlerKey");
        if (!key.matches("[A-Za-z0-9._:-]{1,120}")) {
            throw new IllegalArgumentException("Invalid Center-Cut handler key");
        }
        return key;
    }

    private static String requiredText(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " is required");
        return value.trim();
    }

    @PreDestroy
    @Override
    public void close() {
        leaseHeartbeats.shutdown();
    }

    private final class LeaseGuard implements AutoCloseable {
        private final JdbcCenterCutClaimRepository.Claim claim;
        private final AtomicBoolean lost = new AtomicBoolean();
        private final AtomicReference<RuntimeException> renewalFailure = new AtomicReference<>();
        private ScheduledFuture<?> heartbeat;

        private LeaseGuard(JdbcCenterCutClaimRepository.Claim claim) {
            this.claim = claim;
        }

        private void start() {
            heartbeat = leaseHeartbeats.scheduleAtFixedRate(
                    this::renew, heartbeatMillis, heartbeatMillis, TimeUnit.MILLISECONDS);
        }

        private void renew() {
            if (lost.get()) return;
            try {
                if (!repository.renew(claim, leaseDuration)) lost.set(true);
            } catch (RuntimeException failure) {
                renewalFailure.compareAndSet(null, failure);
                lost.set(true);
            }
        }

        private void assertOwned() {
            if (lost.get()) throw new LeaseLostException(renewalFailure.get());
        }

        @Override
        public void close() {
            if (heartbeat != null) heartbeat.cancel(false);
        }
    }

    private static final class LeaseLostException extends RuntimeException {
        private LeaseLostException(Throwable cause) {
            super("Center-Cut item lease was lost", cause);
        }
    }
}

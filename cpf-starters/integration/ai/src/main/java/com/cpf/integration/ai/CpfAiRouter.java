package com.cpf.integration.ai;

import com.cpf.integration.ai.api.CpfAiOperations;
import com.cpf.integration.ai.api.CpfAiPolicy;
import com.cpf.integration.ai.api.CpfAiProvider;
import com.cpf.integration.ai.api.CpfAiRequest;
import com.cpf.integration.ai.api.CpfAiResponse;
import com.cpf.integration.ai.api.CpfAiRisk;
import com.cpf.integration.ai.api.CpfAiUnknownResultException;
import com.cpf.integration.ai.api.CpfAiTelemetry;
import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.foundation.execution.CpfContextExecutionFactory;
import java.time.Duration;
import java.time.Clock;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Provider-neutral AI routing runtime입니다.
 *
 * <p>Core Context에는 AI 전용 Component를 삽입하지 않습니다. 호출별 child execution만 생성해
 * transactionId/root lineage를 보존하고, AI 전용 메타데이터는 이 Owner 내부에서만 사용합니다.</p>
 */
public final class CpfAiRouter implements CpfAiOperations, AutoCloseable {
    private final List<CpfAiProvider> providers;
    private final CpfAiPolicy policy;
    private final CpfAiProperties properties;
    private final CpfContextExecutionFactory contextFactory;
    private final CpfAiResourceLimiter resourceLimiter;
    private final CpfAiTelemetry telemetry;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final ConcurrentMap<String, Circuit> circuits = new ConcurrentHashMap<>();

    public CpfAiRouter(
            List<CpfAiProvider> providers,
            CpfAiPolicy policy,
            CpfAiProperties properties,
            CpfContextExecutionFactory contextFactory) {
        this(providers, policy, properties, contextFactory,
                new CpfAiResourceLimiter(properties, Clock.systemUTC()), CpfAiTelemetry.NOOP);
    }

    CpfAiRouter(
            List<CpfAiProvider> providers,
            CpfAiPolicy policy,
            CpfAiProperties properties,
            CpfContextExecutionFactory contextFactory,
            CpfAiResourceLimiter resourceLimiter,
            CpfAiTelemetry telemetry) {
        this.providers = order(providers, properties.getProviderOrder());
        this.policy = Objects.requireNonNull(policy, "policy");
        this.properties = Objects.requireNonNull(properties, "properties");
        this.contextFactory = Objects.requireNonNull(contextFactory, "contextFactory");
        this.resourceLimiter = Objects.requireNonNull(resourceLimiter, "resourceLimiter");
        this.telemetry = Objects.requireNonNull(telemetry, "telemetry");
        if (this.providers.isEmpty()) throw new IllegalStateException("AI enabled but no CpfAiProvider is available");
    }

    @Override
    public CpfAiResponse execute(CpfAiRequest original) {
        CpfAiRequest request = policy.authorizeAndMask(Objects.requireNonNull(original, "request"));
        try {
            resourceLimiter.check(request);
        } catch (CpfAiLimitExceededException limited) {
            telemetry.limited(request, limited.code());
            policy.audit(request, null, limited);
            throw limited;
        }
        telemetry.accepted(request);
        long startedNanos = System.nanoTime();
        CpfContextSnapshot caller = CpfContexts.requireSnapshot();
        String transactionId = caller.context().transactionId();
        if (request.risk() == CpfAiRisk.HIGH && !request.humanApproved()) {
            throw new SecurityException("HIGH risk AI request requires human approval");
        }

        Throwable last = null;
        int totalAttempts = 0;
        for (CpfAiProvider provider : providers) {
            if (!provider.supports(request.model())) continue;
            Circuit circuit = circuits.computeIfAbsent(provider.providerId(), ignored -> new Circuit());
            if (circuit.isOpen(properties.getCircuitOpenDuration())) continue;

            for (int n = 1; n <= properties.getRetryAttemptsPerProvider()
                    && totalAttempts < properties.getMaxAttempts(); n++) {
                totalAttempts++;
                Future<CpfAiResponse> future = null;
                try {
                    int attempt = totalAttempts;
                    CpfContextSnapshot attemptSnapshot = contextFactory.childSnapshot(
                            caller,
                            new CpfContextExecutionFactory.ChildSpec(
                                    "ai." + provider.providerId(),
                                    CpfContext.CpfExecutionType.INTEGRATION,
                                    attempt,
                                    caller.context().execution().deadline(),
                                    caller.context().operation()));
                    // Owner-local metadata: useful for audit/metrics but never placed in Core Context.
                    new CpfAiContext(
                            transactionId + ":" + attempt,
                            request.model(), provider.providerId(), null, null, attempt);
                    future = executor.submit(() -> CpfContexts.call(attemptSnapshot, () -> provider.execute(request)));
                    long timeoutMs = Math.max(1, Math.min(
                            request.timeout().toMillis(), properties.getTimeout().toMillis()));
                    CpfAiResponse response = future.get(timeoutMs, TimeUnit.MILLISECONDS);
                    circuit.success();
                    policy.audit(request, response, null);
                    telemetry.completed(request, response, System.nanoTime() - startedNanos);
                    return response;
                } catch (TimeoutException timeout) {
                    if (future != null) future.cancel(true);
                    last = timeout;
                    circuit.failure(properties.getCircuitFailureThreshold());
                    if (!provider.safeToFallbackAfterTimeout()) {
                        policy.audit(request, null, timeout);
                        telemetry.failed(request, timeout, System.nanoTime() - startedNanos);
                        throw new CpfAiUnknownResultException(transactionId, timeout);
                    }
                    break;
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    last = interrupted;
                    circuit.failure(properties.getCircuitFailureThreshold());
                    break;
                } catch (Exception failure) {
                    last = failure instanceof ExecutionException && failure.getCause() != null
                            ? failure.getCause() : failure;
                    circuit.failure(properties.getCircuitFailureThreshold());
                    if (n < properties.getRetryAttemptsPerProvider() && totalAttempts < properties.getMaxAttempts()) {
                        sleep(properties.getRetryBackoff());
                    }
                }
            }
        }
        policy.audit(request, null, last);
        telemetry.failed(request, last, System.nanoTime() - startedNanos);
        throw new CpfAiUnknownResultException(transactionId, last);
    }

    private static void sleep(Duration duration) {
        if (duration == null || duration.isZero()) return;
        try { Thread.sleep(duration.toMillis()); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    private static List<CpfAiProvider> order(List<CpfAiProvider> input, List<String> preferredOrder) {
        List<CpfAiProvider> source = input == null ? List.of() : List.copyOf(input);
        if (preferredOrder == null || preferredOrder.isEmpty()) return source;
        Map<String, CpfAiProvider> byId = new LinkedHashMap<>();
        for (CpfAiProvider provider : source) {
            if (byId.put(provider.providerId(), provider) != null) {
                throw new IllegalArgumentException("Duplicate AI providerId: " + provider.providerId());
            }
        }
        List<CpfAiProvider> ordered = new ArrayList<>();
        for (String id : preferredOrder) {
            CpfAiProvider provider = byId.remove(id);
            if (provider != null) ordered.add(provider);
        }
        ordered.addAll(byId.values());
        return List.copyOf(ordered);
    }

    @Override public void close() { executor.close(); }

    private static final class Circuit {
        private final AtomicInteger failures = new AtomicInteger();
        private volatile long openedAtNanos = -1L;
        void success() { failures.set(0); openedAtNanos = -1L; }
        void failure(int threshold) { if (failures.incrementAndGet() >= threshold) openedAtNanos = System.nanoTime(); }
        boolean isOpen(Duration duration) {
            long opened = openedAtNanos;
            if (opened < 0) return false;
            if (System.nanoTime() - opened >= duration.toNanos()) { success(); return false; }
            return true;
        }
    }
}

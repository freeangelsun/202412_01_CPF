package com.cpf.batch.centercut.runner;

import com.cpf.batch.api.ActualState;
import com.cpf.batch.runtime.BatchRuntimePolicy;
import com.cpf.batch.runtime.RuntimeStateProvider;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Spring Batch Step 경계에서만 Center-Cut 실행 상태와 admission을 관찰합니다. */
@Component
public final class SpringBatchCenterCutRuntimeState implements RuntimeStateProvider {
    static final String DISABLED = "BAT_CENTER_CUT_DISABLED";
    static final String CAPACITY_EXHAUSTED = "BAT_CENTER_CUT_CAPACITY_EXHAUSTED";
    static final String LEASE_LOST = "BAT_CENTER_CUT_LEASE_LOST";

    private final BatchRuntimePolicy runtimePolicy;
    private final int configuredMaxConcurrency;
    private final AtomicBoolean manualDrain = new AtomicBoolean();
    private final AtomicInteger active = new AtomicInteger();
    private final AtomicLong sequence = new AtomicLong();
    private final ConcurrentMap<Long, Invocation> invocations = new ConcurrentHashMap<>();
    private final AtomicReference<String> repositoryError = new AtomicReference<>();

    public SpringBatchCenterCutRuntimeState(
            BatchRuntimePolicy runtimePolicy,
            @Value("${cpf.center-cut.max-concurrency:${CPF_CENTER_CUT_MAX_CONCURRENCY:1}}")
            int maxConcurrency) {
        this.runtimePolicy = runtimePolicy;
        if (maxConcurrency < 1 || maxConcurrency > BatchRuntimePolicy.MAX_CONCURRENCY) {
            throw new IllegalArgumentException("Center-Cut max concurrency is out of range");
        }
        this.configuredMaxConcurrency = maxConcurrency;
    }

    Scope begin(String cpfExecutionId, long jobExecutionId, long fencingToken) {
        if (!accepting()) {
            throw new IllegalStateException(DISABLED);
        }
        int current = active.incrementAndGet();
        if (current > configuredMaxConcurrency) {
            active.decrementAndGet();
            throw new IllegalStateException(CAPACITY_EXHAUSTED);
        }
        long id = sequence.incrementAndGet();
        invocations.put(id, new Invocation(cpfExecutionId, jobExecutionId, fencingToken));
        return new Scope(id);
    }

    boolean accepting() {
        return !manualDrain.get() && runtimePolicy.current().centerCutEnabled();
    }

    void repositoryHealthy() {
        repositoryError.set(null);
    }

    void repositoryFailure(RuntimeException failure) {
        repositoryError.set("BAT_CENTER_CUT_REPOSITORY_"
                + failure.getClass().getSimpleName().toUpperCase(java.util.Locale.ROOT));
    }

    void leaseLost() {
        repositoryError.set(LEASE_LOST);
    }

    public void drain() {
        manualDrain.set(true);
    }

    public void resume() {
        manualDrain.set(false);
    }

    @Override
    public ActualState actualState() {
        if (repositoryError.get() != null) return ActualState.DEGRADED;
        if (draining()) return ActualState.DRAINING;
        return active.get() == 0 ? ActualState.READY : ActualState.BUSY;
    }

    @Override
    public boolean ready() {
        return accepting() && repositoryError.get() == null;
    }

    @Override
    public List<String> currentExecutions() {
        return invocations.values().stream()
                .map(Invocation::cpfExecutionId)
                .distinct()
                .sorted()
                .toList();
    }

    @Override
    public List<String> activeLeases() {
        return invocations.values().stream()
                .map(Invocation::claimToken)
                .filter(token -> token != null && !token.isBlank())
                .sorted()
                .toList();
    }

    @Override
    public int availableCapacity() {
        return ready() ? Math.max(0, configuredMaxConcurrency - active.get()) : 0;
    }

    @Override
    public boolean draining() {
        return !accepting();
    }

    @Override
    public Map<String, String> dependencyHealth() {
        String error = repositoryError.get();
        return Map.of("springBatchCenterCut", error == null ? (draining() ? "PAUSED" : "UP") : "DOWN");
    }

    @Override
    public String lastErrorCode() {
        return repositoryError.get();
    }

    @Override
    public Map<String, Number> metrics() {
        LinkedHashMap<String, Number> values = new LinkedHashMap<>();
        values.put("centerCut.configuredMaxConcurrency", configuredMaxConcurrency);
        values.put("centerCut.activeStepInvocations", active.get());
        values.put("centerCut.activeItemLeases", activeLeases().size());
        return Map.copyOf(values);
    }

    @Override
    public long fencingToken() {
        return invocations.values().stream()
                .max(Comparator.comparingLong(Invocation::fencingToken))
                .map(Invocation::fencingToken)
                .orElse(0L);
    }

    final class Scope implements AutoCloseable {
        private final long id;
        private final AtomicBoolean closed = new AtomicBoolean();

        private Scope(long id) {
            this.id = id;
        }

        void claim(String claimToken) {
            Invocation invocation = invocations.get(id);
            if (invocation != null) invocation.claimTokenRef().set(claimToken);
        }

        void releaseClaim() {
            claim(null);
        }

        @Override
        public void close() {
            if (closed.compareAndSet(false, true)) {
                invocations.remove(id);
                active.decrementAndGet();
            }
        }
    }

    private record Invocation(
            String cpfExecutionId,
            long jobExecutionId,
            long fencingToken,
            AtomicReference<String> claimTokenRef) {
        private Invocation(String cpfExecutionId, long jobExecutionId, long fencingToken) {
            this(cpfExecutionId, jobExecutionId, fencingToken, new AtomicReference<>());
        }

        private String claimToken() {
            return claimTokenRef.get();
        }
    }
}

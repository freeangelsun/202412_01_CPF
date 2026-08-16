package com.cpf.integration.resilience.internal;

import com.cpf.platform.operations.observability.api.CpfTelemetry;
import com.cpf.integration.resilience.api.CpfResilienceCallContext;
import com.cpf.integration.resilience.api.CpfResilienceOutcome;
import com.cpf.integration.resilience.api.CpfResiliencePolicy;
import com.cpf.integration.resilience.spi.CpfResilienceFailureClassifier;
import com.cpf.integration.resilience.spi.CpfResiliencePolicyStore;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.DoubleSupplier;

/** Executable consumer gate for remote/local call and retry-attempt trace correlation. */
public final class CpfResilienceTracingHarness {
    private CpfResilienceTracingHarness() { }

    public static void main(String[] args) {
        tracesRemoteRetriesAndSensitiveCorrelation();
        isolatesTelemetryProviderFailure();
        tracesLocalOverrideAndReconcile();
        System.out.println("CPF_RESILIENCE_TRACING_HARNESS_PASS");
    }

    private static void tracesRemoteRetriesAndSensitiveCorrelation() {
        CpfResiliencePolicy policy = policy("payment.remote", 2);
        RecordingTelemetry telemetry = new RecordingTelemetry(false, false, false);
        AtomicInteger calls = new AtomicInteger();
        try (CpfResilienceEngine engine = engine(policy, telemetry)) {
            CpfResilienceCallContext context = new CpfResilienceCallContext(
                    policy.operationId(), "user@example.com", "idem-1", Instant.EPOCH,
                    Map.of("cpf.module", "payment", "cpf.channel", "MOBILE"));
            CpfResilienceOutcome<String> result = engine.execute(context, () -> {
                if (calls.incrementAndGet() == 1) throw new IllegalStateException("retry");
                return "OK";
            });
            check(result.status() == CpfResilienceOutcome.Status.SUCCESS && result.attempts() == 2,
                    "retry outcome");
        }
        check(telemetry.spans.size() == 3, "root plus two attempt spans");
        Span root = telemetry.spans.get(0);
        check("REMOTE".equals(root.kind) && root.name.startsWith("remote.execute.payment.remote"),
                "remote root span");
        check(!root.attributes.toString().contains("user@example.com"),
                "sensitive transaction identifier excluded");
        check(root.attributes.get("cpf.transaction_id").startsWith("sha256:"),
                "sensitive transaction identifier hashed");
        Span first = telemetry.spans.get(1);
        Span second = telemetry.spans.get(2);
        check("1".equals(first.attributes.get("cpf.attempt"))
                        && "2".equals(second.attributes.get("cpf.attempt")),
                "attempt correlation");
        check(first.attributes.get("cpf.trace_id").equals(second.attributes.get("cpf.trace_id")),
                "retry trace correlation");
        check(first.errors == 1 && second.errors == 0 && root.errors == 0,
                "attempt error isolated from successful root");
        check(telemetry.spans.stream().allMatch(span -> span.closed == 1), "all spans closed once");
    }

    private static void isolatesTelemetryProviderFailure() {
        CpfResiliencePolicy policy = policy("telemetry.failure", 1);
        for (RecordingTelemetry telemetry : List.of(
                new RecordingTelemetry(true, false, false),
                new RecordingTelemetry(false, true, true))) {
            try (CpfResilienceEngine engine = engine(policy, telemetry)) {
                CpfResilienceOutcome<String> result = engine.execute(new CpfResilienceCallContext(
                        policy.operationId(), "tx-safe", null, Instant.EPOCH, Map.of()), () -> "OK");
                check(result.status() == CpfResilienceOutcome.Status.SUCCESS,
                        "telemetry failure must not alter business outcome");
            }
        }
    }

    private static void tracesLocalOverrideAndReconcile() {
        CpfResiliencePolicy policy = policy("cache.local", 1);
        RecordingTelemetry telemetry = new RecordingTelemetry(false, false, false);
        try (CpfResilienceEngine engine = engine(policy, telemetry)) {
            CpfResilienceCallContext context = new CpfResilienceCallContext(
                    policy.operationId(), "tx-local", null, Instant.EPOCH,
                    Map.of(CpfResilienceCallContext.TRACE_SPAN_KIND_ATTRIBUTE, "LOCAL",
                            CpfResilienceCallContext.TRACE_SEGMENT_ATTRIBUTE, "CACHE"));
            check(engine.execute(context, () -> "HIT").status() == CpfResilienceOutcome.Status.SUCCESS,
                    "local execution");
            check(engine.reconcile(context, () -> "PROBED").status() == CpfResilienceOutcome.Status.SUCCESS,
                    "reconcile execution");
        }
        check(telemetry.spans.get(0).name.startsWith("local.execute.cache.local"),
                "local override root");
        check(telemetry.spans.stream().anyMatch(span -> span.name.startsWith("local.reconcile.cache.local")),
                "reconcile root span");
        check(telemetry.spans.stream().filter(span -> span.name.contains(".attempt"))
                        .allMatch(span -> "CACHE".equals(span.attributes.get("cpf.segment_id"))),
                "segment correlation");
    }

    private static CpfResilienceEngine engine(CpfResiliencePolicy policy, CpfTelemetry telemetry) {
        ExecutorService executor = Executors.newCachedThreadPool();
        DoubleSupplier random = () -> 0.5d;
        return CpfResilienceTestSupport.engine(
                new SinglePolicyStore(policy),
                failure -> CpfResilienceFailureClassifier.Classification.RETRYABLE,
                (a, b, c, d, e, f) -> { },
                com.cpf.integration.resilience.spi.CpfResilienceRuntimePolicyResolver.legacyCompatible(),
                null,
                telemetry,
                Clock.fixed(Instant.EPOCH, ZoneOffset.UTC),
                executor,
                random,
                System::nanoTime);
    }

    private static CpfResiliencePolicy policy(String operation, int attempts) {
        return new CpfResiliencePolicy(operation, 1L, Duration.ofSeconds(1), attempts,
                Duration.ZERO, 3, Duration.ofSeconds(5), 2, 100,
                Duration.ofMinutes(1), true, true);
    }

    private static void check(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
    }

    private static final class SinglePolicyStore implements CpfResiliencePolicyStore {
        private final CpfResiliencePolicy policy;
        SinglePolicyStore(CpfResiliencePolicy policy) { this.policy = policy; }
        @Override public Optional<CpfResiliencePolicy> findActive(String operationId) {
            return policy.operationId().equals(operationId) ? Optional.of(policy) : Optional.empty();
        }
        @Override public List<CpfResiliencePolicy> search(String filter, int offset, int limit) {
            return List.of(policy);
        }
        @Override public String request(CpfResiliencePolicy requested, String requester, String reason) {
            throw new UnsupportedOperationException();
        }
        @Override public CpfResiliencePolicy approve(String requestId, String approver, String reason) {
            throw new UnsupportedOperationException();
        }
        @Override public void reject(String requestId, String approver, String reason) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class RecordingTelemetry implements CpfTelemetry {
        private final boolean failStart;
        private final boolean failError;
        private final boolean failClose;
        private final List<Span> spans = new ArrayList<>();
        RecordingTelemetry(boolean failStart, boolean failError, boolean failClose) {
            this.failStart = failStart;
            this.failError = failError;
            this.failClose = failClose;
        }
        @Override public CpfTelemetrySpan startSpan(
                String name, String kind, Map<String, String> attributes) {
            if (failStart) throw new IllegalStateException("telemetry start unavailable");
            Span span = new Span(name, kind, attributes, failError, failClose);
            spans.add(span);
            return span;
        }
        @Override public Map<String, Object> status() { return Map.of("state", "TEST"); }
    }

    private static final class Span implements CpfTelemetry.CpfTelemetrySpan {
        private final String name;
        private final String kind;
        private final Map<String, String> attributes;
        private final boolean failError;
        private final boolean failClose;
        private int errors;
        private int closed;
        Span(String name, String kind, Map<String, String> attributes,
                boolean failError, boolean failClose) {
            this.name = name;
            this.kind = kind;
            this.attributes = Map.copyOf(attributes);
            this.failError = failError;
            this.failClose = failClose;
        }
        @Override public void error(Throwable throwable) {
            errors++;
            if (failError) throw new IllegalStateException("telemetry error unavailable");
        }
        @Override public void close() {
            closed++;
            if (failClose) throw new IllegalStateException("telemetry close unavailable");
        }
    }
}

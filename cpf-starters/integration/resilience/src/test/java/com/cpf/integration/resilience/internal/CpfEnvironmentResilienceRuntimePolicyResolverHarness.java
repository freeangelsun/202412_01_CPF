package com.cpf.integration.resilience.internal;

import com.cpf.integration.resilience.api.CpfResilienceCallContext;
import com.cpf.integration.resilience.api.CpfResiliencePolicy;
import com.cpf.integration.resilience.api.CpfResilienceRuntimePolicy;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

/** Verifies deterministic global/operation override resolution and fail-closed validation. */
public final class CpfEnvironmentResilienceRuntimePolicyResolverHarness {
    private CpfEnvironmentResilienceRuntimePolicyResolverHarness() {}

    public static void main(String[] args) {
        MapEnvironment environment = new MapEnvironment(Map.of(
                "cpf.integration.resilience.runtime.connect-timeout", "250ms",
                "cpf.integration.resilience.runtime.tls-timeout", "500ms",
                "cpf.integration.resilience.runtime.response-header-timeout", "900ms",
                "cpf.integration.resilience.runtime.attempt-timeout", "2s",
                "cpf.integration.resilience.runtime.overall-timeout", "5s",
                "cpf.integration.resilience.runtime.retry.jitter-ratio", "0.25",
                "cpf.integration.resilience.runtime.operations.payment-post.write-timeout", "750ms",
                "cpf.integration.resilience.runtime.operations.payment-post.retry.budget-capacity", "7",
                "cpf.integration.resilience.runtime.operations.payment-post.bulkhead.queue-limit", "3"));
        CpfEnvironmentResilienceRuntimePolicyResolver resolver =
                new CpfEnvironmentResilienceRuntimePolicyResolver(environment);
        CpfResiliencePolicy base = new CpfResiliencePolicy(
                "PAYMENT.POST", 1, Duration.ofSeconds(1), 2, Duration.ofMillis(50),
                3, Duration.ofSeconds(5), 2, 100, Duration.ofMinutes(1), true, true);
        CpfResilienceCallContext context = CpfResilienceCallContext.now(
                base.operationId(), "tx", "idem", Map.of(),
                Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
        CpfResilienceRuntimePolicy resolved = resolver.resolve(base, context);
        check(resolved.connectTimeout().equals(Duration.ofMillis(250)), "global connect duration");
        check(resolved.tlsTimeout().equals(Duration.ofMillis(500)), "global TLS duration");
        check(resolved.writeTimeout().equals(Duration.ofMillis(750)), "operation duration override");
        check(resolved.responseHeaderTimeout().equals(Duration.ofMillis(900)), "response header duration");
        check(resolved.attemptTimeout().equals(Duration.ofSeconds(2)), "attempt timeout");
        check(resolved.overallTimeout().equals(Duration.ofSeconds(5)), "overall timeout");
        check(resolved.retryBudgetCapacity() == 7, "operation retry budget override");
        check(resolved.bulkheadQueueLimit() == 3, "operation queue limit override");
        check(resolved.jitterRatio() == 0.25d, "global jitter");

        environment.put("cpf.integration.resilience.runtime.overall-timeout", "1s");
        boolean invalid = false;
        try { resolver.resolve(base, context); }
        catch (IllegalArgumentException expected) { invalid = true; }
        check(invalid, "overall timeout shorter than attempt fails closed");
        System.out.println("CPF_ENVIRONMENT_RESILIENCE_POLICY_HARNESS_PASS");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static final class MapEnvironment implements Environment {
        private final Map<String, String> values = new HashMap<>();
        private MapEnvironment(Map<String, String> source) { values.putAll(source); }
        void put(String key, String value) { values.put(key, value); }
        @Override public boolean acceptsProfiles(Profiles profiles) { return profiles.matches(profile -> false); }
        @Override public String getProperty(String key) { return values.get(key); }
    }
}

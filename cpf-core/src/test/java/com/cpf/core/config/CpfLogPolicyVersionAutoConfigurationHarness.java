package com.cpf.core.config;

import com.cpf.core.api.logging.policy.CpfLogPolicyVersionOperations;
import com.cpf.core.api.logging.policy.CpfLogPolicyVersionSnapshot;
import com.cpf.core.api.logging.policy.LogPolicyDecision;
import com.cpf.core.api.logging.policy.LogPolicyTargetType;
import com.cpf.core.internal.logging.InMemoryCpfLogPolicyVersionStore;
import com.cpf.core.spi.logging.CpfLogPolicyVersionApplier;
import com.cpf.core.spi.logging.CpfLogPolicyVersionAuditSink;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;

/** Isolated contract gate for fail-closed log-policy version control-plane wiring. */
public final class CpfLogPolicyVersionAutoConfigurationHarness {
    private CpfLogPolicyVersionAutoConfigurationHarness() { }

    public static void main(String[] args) throws Exception {
        Method storeMethod = CpfLogPolicyVersionAutoConfiguration.class.getDeclaredMethod(
                "cpfLogPolicyVersionStore", Environment.class, ObjectProvider.class);
        ConditionalOnProperty condition = storeMethod.getAnnotation(ConditionalOnProperty.class);
        require(condition != null, "store bean must have an explicit property condition");
        require("cpf.logging.policy-version".equals(condition.prefix()), "property prefix mismatch");
        require(condition.name().length == 1 && "mode".equals(condition.name()[0]), "mode property mismatch");
        require("in-memory".equals(condition.havingValue()), "only explicit in-memory mode may activate");
        require(!condition.matchIfMissing(), "control plane must remain disabled when mode is absent");

        Clock clock = Clock.fixed(Instant.parse("2026-08-05T00:00:00Z"), ZoneOffset.UTC);
        Map<String, Object> values = new HashMap<>();
        values.put("cpf.logging.policy-version.in-memory.maximum-targets", 7);
        values.put("cpf.logging.policy-version.in-memory.maximum-history-per-target", 5);
        values.put("cpf.logging.policy-version.in-memory.maximum-command-records", 19);
        values.put("cpf.logging.policy-version.in-memory.command-ttl", Duration.ofMinutes(9));
        Environment environment = proxy(Environment.class, (method, arguments) -> {
            if ("getProperty".equals(method.getName()) && arguments != null && arguments.length == 3) {
                return values.getOrDefault(arguments[0], arguments[2]);
            }
            return null;
        });
        ObjectProvider<Clock> clocks = provider(clock);
        CpfLogPolicyVersionAutoConfiguration configuration = new CpfLogPolicyVersionAutoConfiguration();
        InMemoryCpfLogPolicyVersionStore store = configuration.cpfLogPolicyVersionStore(environment, clocks);
        var runtime = store.runtimeStatus();
        require(runtime.maximumTargets() == 7, "maximum-targets must be consumed");
        require(runtime.maximumHistoryPerTarget() == 5, "maximum-history must be consumed");
        require(runtime.maximumCommandRecords() == 19, "maximum-command-records must be consumed");

        CpfLogPolicyVersionAuditSink audit = event -> { };
        CpfLogPolicyVersionApplier applier = new CpfLogPolicyVersionApplier() {
            @Override public CpfLogPolicyVersionSnapshot baseline(
                    LogPolicyTargetType type, String targetId, Instant observedAt) {
                return new CpfLogPolicyVersionSnapshot(type, targetId, 1L,
                        CpfLogPolicyVersionSnapshot.Status.ACTIVE,
                        LogPolicyDecision.cpfDefault(type, targetId), observedAt,
                        "CPF_RUNTIME", "baseline");
            }
            @Override public void apply(CpfLogPolicyVersionSnapshot snapshot) { }
        };
        CpfLogPolicyVersionOperations operations = configuration.cpfLogPolicyVersionOperations(
                store, audit, applier, clocks);
        require(operations.current(LogPolicyTargetType.MODULE, "PAY").version() == 1L,
                "operations bean must expose the actual manager and baseline consumer");
        System.out.println("CPF_LOG_POLICY_VERSION_AUTOCONFIG_HARNESS_PASS");
    }

    @SuppressWarnings("unchecked")
    private static <T> ObjectProvider<T> provider(T value) {
        return (ObjectProvider<T>) Proxy.newProxyInstance(ObjectProvider.class.getClassLoader(),
                new Class<?>[]{ObjectProvider.class}, (proxy, method, args) -> {
                    if ("getIfUnique".equals(method.getName()) || "getIfAvailable".equals(method.getName())
                            || "getObject".equals(method.getName())) return value;
                    if ("iterator".equals(method.getName())) return java.util.List.of(value).iterator();
                    return null;
                });
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> contract, Invocation invocation) {
        return (T) Proxy.newProxyInstance(contract.getClassLoader(), new Class<?>[]{contract},
                (proxy, method, args) -> invocation.invoke(method, args));
    }

    @FunctionalInterface
    private interface Invocation {
        Object invoke(Method method, Object[] arguments) throws Throwable;
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}

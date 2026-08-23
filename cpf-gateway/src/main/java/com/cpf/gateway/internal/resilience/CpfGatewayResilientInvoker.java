package com.cpf.gateway.internal.resilience;

import com.cpf.core.api.context.CpfContexts;
import com.cpf.integration.resilience.api.CpfResilienceCallContext;

import com.cpf.integration.resilience.api.CpfResilienceExecutor;
import com.cpf.integration.resilience.api.CpfResilienceOutcome;
import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/** Gateway adapter that applies the same operation policy used by HTTP and TCP clients. */
public final class CpfGatewayResilientInvoker {
    private final CpfResilienceExecutor executor;
    private final Clock clock;

    public CpfGatewayResilientInvoker(CpfResilienceExecutor executor) {
        this(executor, Clock.systemUTC());
    }

    CpfGatewayResilientInvoker(CpfResilienceExecutor executor, Clock clock) {
        this.executor = Objects.requireNonNull(executor, "executor");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /**
     * Compatibility entry point. Unknown operation kinds fail closed for timeout retry until the
     * caller explicitly declares READ or WRITE semantics.
     */
    public <T> CpfResilienceOutcome<T> invoke(
            String routeId,
            String transactionId,
            String idempotencyKey,
            Supplier<T> downstream) {
        return invoke(routeId, transactionId, idempotencyKey,
                CpfResilienceCallContext.OperationKind.UNKNOWN, false, downstream);
    }

    public <T> CpfResilienceOutcome<T> invokeRead(
            String routeId, String transactionId, Supplier<T> downstream) {
        return invoke(routeId, transactionId, null, CpfResilienceCallContext.OperationKind.READ, true, downstream);
    }

    public <T> CpfResilienceOutcome<T> invokeWrite(
            String routeId,
            String transactionId,
            String idempotencyKey,
            boolean timeoutRetryAllowed,
            Supplier<T> downstream) {
        if (timeoutRetryAllowed && (idempotencyKey == null || idempotencyKey.isBlank())) {
            throw new IllegalArgumentException(
                    "idempotencyKey is required when timeout retry is enabled for a gateway write");
        }
        return invoke(routeId, transactionId, idempotencyKey,
                CpfResilienceCallContext.OperationKind.WRITE, timeoutRetryAllowed, downstream);
    }

    public <T> CpfResilienceOutcome<T> invoke(
            String routeId,
            String transactionId,
            String idempotencyKey,
            CpfResilienceCallContext.OperationKind operationKind,
            boolean timeoutRetryAllowed,
            Supplier<T> downstream) {
        String normalizedRoute = required(routeId, "routeId");
        String suppliedTransactionId = required(transactionId, "transactionId");
        String currentTransactionId = CpfContexts.transactionId();
        if (!currentTransactionId.equals(suppliedTransactionId)) {
            throw new SecurityException(
                    "transactionId must match the current managed CPF transaction");
        }
        Objects.requireNonNull(operationKind, "operationKind");
        Objects.requireNonNull(downstream, "downstream");

        Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put("consumer", "gateway");
        attributes.put("route", normalizedRoute);
        attributes.put(CpfResilienceCallContext.OPERATION_KIND_ATTRIBUTE, operationKind.name());
        attributes.put(CpfResilienceCallContext.TIMEOUT_RETRY_ATTRIBUTE,
                Boolean.toString(timeoutRetryAllowed));
        attributes.put(CpfResilienceCallContext.TRACE_SPAN_KIND_ATTRIBUTE, "CLIENT");
        attributes.put(CpfResilienceCallContext.TRACE_SEGMENT_ATTRIBUTE,
                "gateway." + normalizedRoute);

        CpfResilienceCallContext context = CpfResilienceCallContext.current(
                "gateway." + normalizedRoute,
                idempotencyKey,
                Map.copyOf(attributes),
                clock);
        return executor.execute(context, downstream);
    }

    private static String required(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
        return value.trim();
    }
}

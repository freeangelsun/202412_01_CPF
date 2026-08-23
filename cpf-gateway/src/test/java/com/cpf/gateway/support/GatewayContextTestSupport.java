package com.cpf.gateway.support;

import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import java.time.Instant;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

/** Managed CPF Context fixture for tests that invoke below the real Gateway ingress boundary. */
public final class GatewayContextTestSupport {
    private static final Instant NOW = Instant.parse("2026-08-22T00:00:00Z");
    private static final AtomicInteger SEQUENCE = new AtomicInteger();

    private GatewayContextTestSupport() {}

    public static AutoCloseable bind(String transactionId, String callerChannel) {
        int sequence = SEQUENCE.incrementAndGet();
        String executionId = "GWY-TEST-EX-" + sequence;
        String segmentId = "GWY-TEST-SG-" + sequence;
        CpfContext.CpfTransactionContext transaction = new CpfContext.CpfTransactionContext(
                transactionId,
                transactionId,
                null,
                "GWY-TEST-CORR-" + sequence,
                null,
                "EDGE",
                "GWY",
                "EDGE",
                "DOMAIN",
                callerChannel,
                "GATEWAY",
                callerChannel,
                "DOMAIN",
                LocalDate.of(2026, 8, 22),
                NOW,
                CpfContext.CpfTransactionOriginKind.HTTP,
                "GWY",
                transactionId);
        CpfContext.CpfExecutionContext execution = new CpfContext.CpfExecutionContext(
                "GWY-TEST",
                executionId,
                executionId,
                null,
                segmentId,
                null,
                CpfContext.CpfExecutionType.API,
                1,
                0,
                NOW,
                null,
                CpfContext.CpfCancellationMode.DEADLINE_ENFORCED);
        return CpfContexts.bind(CpfContextSnapshot.capture(
                new CpfContext(transaction, execution, null, null, null)));
    }

    public static void assertClear() {
        if (CpfContexts.snapshot() != null) {
            throw new AssertionError("Gateway test leaked a managed CPF Context");
        }
    }
}

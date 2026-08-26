package com.cpf.starter.internal.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.foundation.context.CpfContextProjection;
import com.cpf.foundation.context.CpfContextProjectionRegistry;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class CpfStarterContextProjectionRuntimeTest {
    @Test
    void projectsNestedScopesRestoresParentAndClearsFinalScope() throws Exception {
        CpfContextProjectionRegistry registry = new CpfContextProjectionRegistry();
        CpfStarterContextRuntime runtime = new CpfStarterContextRuntime(registry);
        List<String> projectedTransactions = new ArrayList<>();
        AtomicInteger clears = new AtomicInteger();
        AutoCloseable registration = registry.register(new CpfContextProjection() {
            @Override public void project(CpfContextSnapshot snapshot) {
                projectedTransactions.add(snapshot.transaction().transactionId());
            }
            @Override public void clear() { clears.incrementAndGet(); }
        });

        try (AutoCloseable root = runtime.bind(context("TX-ROOT", "EX-ROOT", "SG-ROOT"))) {
            try (AutoCloseable _ = runtime.bind(context("TX-CHILD", "EX-CHILD", "SG-CHILD"))) {
                assertEquals("TX-CHILD", runtime.current().transactionId());
            }
            assertEquals("TX-ROOT", runtime.current().transactionId());
        } finally {
            registration.close();
        }

        assertEquals(List.of("TX-ROOT", "TX-CHILD", "TX-ROOT"), projectedTransactions);
        assertEquals(1, clears.get());
    }

    @Test
    void outOfOrderCloseFailsClosedAndClearsProjection() throws Exception {
        CpfContextProjectionRegistry registry = new CpfContextProjectionRegistry();
        CpfStarterContextRuntime runtime = new CpfStarterContextRuntime(registry);
        AtomicInteger clears = new AtomicInteger();
        AutoCloseable registration = registry.register(new CpfContextProjection() {
            @Override public void project(CpfContextSnapshot snapshot) { }
            @Override public void clear() { clears.incrementAndGet(); }
        });
        AutoCloseable root = runtime.bind(context("TX-ROOT", "EX-ROOT", "SG-ROOT"));
        AutoCloseable child = runtime.bind(context("TX-CHILD", "EX-CHILD", "SG-CHILD"));

        try {
            assertThrows(IllegalStateException.class, root::close);
            assertNull(runtime.current());
            assertEquals(1, clears.get());
        } finally {
            assertThrows(IllegalStateException.class, child::close);
            registration.close();
        }
    }

    private static CpfContext context(String transactionId, String executionId, String segmentId) {
        Instant now = Instant.parse("2026-08-23T00:00:00Z");
        return new CpfContext(
                new CpfContext.CpfTransactionContext(transactionId, transactionId, null, null,
                        LocalDate.of(2026, 8, 23), now, CpfContext.CpfTransactionOriginKind.INTERNAL, "TST", null),
                new CpfContext.CpfExecutionContext("test.operation", executionId, executionId, null,
                        segmentId, null, CpfContext.CpfExecutionType.INTERNAL, 1, 0, now, now.plusSeconds(30),
                        CpfContext.CpfCancellationMode.DEADLINE_ENFORCED),
                null, null, null);
    }
}

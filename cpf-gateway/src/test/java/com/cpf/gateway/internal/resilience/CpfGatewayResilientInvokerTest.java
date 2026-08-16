package com.cpf.gateway.internal.resilience;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cpf.integration.resilience.api.CpfResilienceCallContext;
import com.cpf.integration.resilience.api.CpfResilienceExecutor;
import com.cpf.integration.resilience.api.CpfResilienceOutcome;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class CpfGatewayResilientInvokerTest {
    private static final Instant NOW = Instant.parse("2026-08-05T02:00:00Z");

    @Test
    void compatibilityCallMarksUnknownAndDisablesTimeoutRetry() {
        RecordingExecutor executor = new RecordingExecutor();
        CpfGatewayResilientInvoker invoker = new CpfGatewayResilientInvoker(
                executor, Clock.fixed(NOW, ZoneOffset.UTC));

        invoker.invoke("member", "tx-1", null, () -> "ok");

        assertThat(executor.context.requestedAt()).isEqualTo(NOW);
        assertThat(executor.context.operationKind())
                .isEqualTo(CpfResilienceCallContext.OperationKind.UNKNOWN);
        assertThat(executor.context.timeoutRetryAllowed()).isFalse();
        assertThat(executor.context.attributes())
                .containsEntry(CpfResilienceCallContext.TRACE_SPAN_KIND_ATTRIBUTE, "CLIENT")
                .containsEntry(CpfResilienceCallContext.TRACE_SEGMENT_ATTRIBUTE, "gateway.member");
    }

    @Test
    void readAndWriteDeclareDifferentRetrySemantics() {
        RecordingExecutor executor = new RecordingExecutor();
        CpfGatewayResilientInvoker invoker = new CpfGatewayResilientInvoker(
                executor, Clock.fixed(NOW, ZoneOffset.UTC));

        invoker.invokeRead("catalog", "tx-read", () -> "read");
        assertThat(executor.context.operationKind())
                .isEqualTo(CpfResilienceCallContext.OperationKind.READ);
        assertThat(executor.context.timeoutRetryAllowed()).isTrue();

        invoker.invokeWrite("payment", "tx-write", "idem-1", false, () -> "write");
        assertThat(executor.context.operationKind())
                .isEqualTo(CpfResilienceCallContext.OperationKind.WRITE);
        assertThat(executor.context.timeoutRetryAllowed()).isFalse();
    }

    @Test
    void retryableWriteRequiresIdempotencyBeforeExecutorCall() {
        RecordingExecutor executor = new RecordingExecutor();
        CpfGatewayResilientInvoker invoker = new CpfGatewayResilientInvoker(
                executor, Clock.fixed(NOW, ZoneOffset.UTC));

        assertThatThrownBy(() -> invoker.invokeWrite(
                "payment", "tx-write", null, true, () -> "write"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("idempotencyKey");
        assertThat(executor.calls).isZero();
    }

    private static final class RecordingExecutor implements CpfResilienceExecutor {
        private CpfResilienceCallContext context;
        private int calls;

        @Override
        public <T> CpfResilienceOutcome<T> execute(
                CpfResilienceCallContext context, Supplier<T> action) {
            this.context = context;
            calls++;
            return new CpfResilienceOutcome<>(
                    CpfResilienceOutcome.Status.SUCCESS, action.get(), null, 1, 1L, NOW);
        }

        @Override
        public <T> CpfResilienceOutcome<T> reconcile(
                CpfResilienceCallContext context, Supplier<T> probe) {
            throw new UnsupportedOperationException();
        }
    }
}

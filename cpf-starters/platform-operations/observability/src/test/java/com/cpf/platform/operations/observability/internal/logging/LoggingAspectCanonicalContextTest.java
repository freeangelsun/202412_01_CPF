package com.cpf.platform.operations.observability.internal.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.foundation.context.header.CpfHeaderNames;
import com.cpf.foundation.execution.api.CpfOnlineTransaction;
import com.cpf.platform.operations.observability.api.logging.CpfTransactionSegmentPort;
import com.cpf.platform.operations.observability.internal.logging.header.CpfHeaderPropagator;
import com.cpf.platform.operations.observability.internal.logging.policy.LogPolicyResolver;
import com.cpf.platform.operations.observability.spi.logging.TransactionLogRecord;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.mock.env.MockEnvironment;

/** Canonical Core Context가 summary와 persisted Segment consumer에 그대로 투영되는지 검증합니다. */
class LoggingAspectCanonicalContextTest {
    private static final Instant NOW = Instant.parse("2026-08-24T10:00:00Z");
    private static final String OPERATION = "MBR_SAMPLE_TX_CREATE";

    @AfterEach
    void clearLegacyContext() {
        TransactionContext.clear();
    }

    @Test
    void projectsCanonicalIdentityAndCompletesPersistedSegment() throws Throwable {
        RecordingSegmentPort segments = new RecordingSegmentPort();
        AtomicReference<TransactionLogEvent> event = new AtomicReference<>();
        LoggingAspect aspect = aspect(segments, event);
        ProceedingJoinPoint joinPoint = joinPoint(false);

        try (AutoCloseable _ = CpfContexts.bind(CpfContextSnapshot.capture(context()))) {
            assertEquals("tx-canonical-001", TransactionContext.currentTransactionId());
            assertEquals("trace-canonical-001", TransactionContext.currentTraceId());
            assertEquals("SG-CANONICAL-001", TransactionContext.currentSpanId());
            assertEquals(OPERATION, TransactionContext.observedOperationId());
            assertNull(TransactionContext.targetOperationId());
            assertEquals(OPERATION, CpfHeaderPropagator.resolvedHeaders().get(CpfHeaderNames.TARGET_OPERATION_ID));
            assertFalse(CpfHeaderPropagator.outboundHeaders().containsKey(CpfHeaderNames.TARGET_OPERATION_ID));

            assertEquals("ok", aspect.logTransaction(joinPoint));
        }

        assertEquals(CpfTransactionSegmentPort.Role.MAIN, segments.role);
        assertEquals(CpfTransactionSegmentPort.Direction.INBOUND, segments.direction);
        assertEquals("MBR", segments.moduleCode);
        assertEquals("BAT", segments.sourceModuleCode);
        assertEquals("MBR", segments.targetModuleCode);
        assertTrue(segments.scope.success);
        assertNull(segments.scope.failureCode);

        assertNotNull(event.get());
        TransactionLogRecord record = event.get().getRecord();
        assertEquals("tx-canonical-001", record.getTransactionId());
        assertEquals("trace-canonical-001", record.getTraceId());
        assertEquals("SG-CANONICAL-001", record.getSpanId());
        assertEquals("BAT", record.getOriginalSystemCode());
        assertEquals("BAT", record.getCallerSystemCode());
        assertEquals("MBR", record.getSystemCode());
        assertEquals("MBR", record.getTargetSystemCode());
        assertEquals(OPERATION, record.getTargetOperationId());
    }

    @Test
    void terminatesPersistedSegmentAsFailureWhenBusinessOperationFails() throws Throwable {
        RecordingSegmentPort segments = new RecordingSegmentPort();
        AtomicReference<TransactionLogEvent> event = new AtomicReference<>();
        LoggingAspect aspect = aspect(segments, event);
        ProceedingJoinPoint joinPoint = joinPoint(true);

        try (AutoCloseable _ = CpfContexts.bind(CpfContextSnapshot.capture(context()))) {
            IllegalStateException failure = assertThrows(IllegalStateException.class,
                    () -> aspect.logTransaction(joinPoint));
            assertEquals("business-failed", failure.getMessage());
        }

        assertFalse(segments.scope.success);
        assertEquals("IllegalStateException", segments.scope.failureCode);
        assertEquals("business-failed", segments.scope.failureMessage);
        assertNotNull(event.get());
        assertEquals("FAILURE", event.get().getRecord().getLogType());
    }

    private static LoggingAspect aspect(
            RecordingSegmentPort segments,
            AtomicReference<TransactionLogEvent> event) {
        DefaultListableBeanFactory providers = new DefaultListableBeanFactory();
        providers.registerSingleton("clock", Clock.fixed(NOW, ZoneOffset.UTC));
        return new LoggingAspect(
                published -> event.set((TransactionLogEvent) published),
                new MockEnvironment().withProperty("spring.application.name", "cpf-member-online"),
                new DynamicTransactionLogLevelService(),
                providers.getBeanProvider(CpfTraceSamplingPolicy.class),
                providers.getBeanProvider(com.cpf.core.api.error.CpfResponseCodeResolver.class),
                providers.getBeanProvider(LogPolicyResolver.class),
                segments,
                providers.getBeanProvider(Clock.class));
    }

    private static ProceedingJoinPoint joinPoint(boolean fail) throws Throwable {
        Method method = SampleOperation.class.getDeclaredMethod("execute");
        MethodSignature signature = mock(MethodSignature.class);
        when(signature.getMethod()).thenReturn(method);
        when(signature.toShortString()).thenReturn("SampleOperation.execute()");
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getTarget()).thenReturn(new SampleOperation());
        when(joinPoint.getArgs()).thenReturn(new Object[0]);
        if (fail) when(joinPoint.proceed()).thenThrow(new IllegalStateException("business-failed"));
        else when(joinPoint.proceed()).thenReturn("ok");
        return joinPoint;
    }

    private static CpfContext context() {
        return new CpfContext(
                new CpfContext.CpfTransactionContext(
                        "tx-canonical-001", "tx-canonical-001", null, "corr-001", "trace-canonical-001",
                        "BAT", "MBR", "BAT", "MBR",
                        "BAT", "MBR", "BAT", "MBR",
                        LocalDate.of(2026, 8, 24), NOW, CpfContext.CpfTransactionOriginKind.HTTP, "BAT", null),
                new CpfContext.CpfExecutionContext(
                        OPERATION, "EX-CANONICAL-001", "EX-CANONICAL-001", null,
                        "SG-CANONICAL-001", null, CpfContext.CpfExecutionType.API, 1, 0, NOW, null,
                        CpfContext.CpfCancellationMode.DEADLINE_ENFORCED),
                new CpfContext.CpfOperationContext(
                        OPERATION, "member sample create", null, "idem-001",
                        CpfContext.CpfIdempotencyScope.CURRENT_OPERATION, CpfContext.CpfIdempotencyMode.OPTIONAL,
                        null, null, null, 1L),
                null,
                null);
    }

    static class SampleOperation {
        @CpfOnlineTransaction(
                operationId = OPERATION,
                name = "member sample create",
                description = "canonical context projection")
        String execute() {
            return "ok";
        }
    }

    private static final class RecordingSegmentPort implements CpfTransactionSegmentPort {
        private final RecordingScope scope = new RecordingScope();
        private Role role;
        private Direction direction;
        private String moduleCode;
        private String sourceModuleCode;
        private String targetModuleCode;

        @Override
        public SegmentScope start(
                Role role,
                Direction direction,
                String moduleCode,
                String sourceModuleCode,
                String targetModuleCode,
                String apiPath,
                String transactionName) {
            this.role = role;
            this.direction = direction;
            this.moduleCode = moduleCode;
            this.sourceModuleCode = sourceModuleCode;
            this.targetModuleCode = targetModuleCode;
            return scope;
        }
    }

    private static final class RecordingScope implements CpfTransactionSegmentPort.SegmentScope {
        private boolean success;
        private String failureCode;
        private String failureMessage;
        @Override public String transactionSegmentId() { return "SEG-PERSISTED-001"; }
        @Override public String transactionId() { return "tx-canonical-001"; }
        @Override public void update(CpfTransactionSegmentPort.SegmentAttributes attributes) { }
        @Override public void success() { success = true; }
        @Override public void fail(String code, String message) { failureCode = code; failureMessage = message; }
    }
}

package com.cpf.batch.centercut.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cpf.batch.execution.internal.context.CpfBatchRuntimeContexts;
import com.cpf.batch.spi.CenterCutHandler;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import com.cpf.testkit.context.CpfTestContextRuntime;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CenterCutWorkProcessorTest {
    private static final String TX = "20260824144200000BATCONTROL0000001";
    private static CpfTestContextRuntime contexts;

    @BeforeAll static void install() { contexts = CpfTestContextRuntime.install(); }
    @AfterAll static void closeRuntime() { contexts.close(); }
    @AfterEach void clear() {
        assertThat(CpfContexts.current()).isNull();
        assertThat(CpfBatchRuntimeContexts.current()).isNull();
    }

    @Test
    void actualWorkerBindsDbLineageAndOwnerContextBeforeOfficialHandler() {
        Fixture fixture = fixture(work(0, 3));
        AtomicReference<String> observed = new AtomicReference<>();
        CenterCutHandler handler = handler(context -> {
            var core = CpfContexts.requireCurrent();
            var batch = CpfBatchRuntimeContexts.current();
            observed.set(core.transactionId() + "|" + core.execution().segmentId() + "|"
                    + core.execution().parentSegmentId() + "|" + batch.centerCut().workerId());
            return new CenterCutHandler.Result("SUCCESS", "{\"persisted\":true}", "done", false, false);
        });

        try (CenterCutWorkProcessor processor = fixture.processor(handler)) {
            CenterCutWorkProcessor.Outcome outcome = processor.processNext(
                    "worker-1", "center-cut", Duration.ofSeconds(10), Duration.ofSeconds(1)).orElseThrow();
            assertThat(outcome.status()).isEqualTo("SUCCESS");
        }

        assertThat(observed).hasValue(TX + "|segment-1|parent-1|worker-1");
        verify(fixture.repository).complete(
                fixture.claim, "SUCCESS", "{\"persisted\":true}", "done");
    }

    @Test
    void retryIsBoundedByImmutableJobLimit() {
        Fixture retry = fixture(work(0, 1));
        try (CenterCutWorkProcessor processor = retry.processor(retryHandler())) {
            assertThat(processor.processNext("worker-1", "center-cut",
                    Duration.ofSeconds(10), Duration.ofSeconds(1)).orElseThrow().status())
                    .isEqualTo("RETRY");
        }
        verify(retry.repository).complete(retry.claim, "RETRY", null, "temporary");

        Fixture exhausted = fixture(work(1, 1));
        try (CenterCutWorkProcessor processor = exhausted.processor(retryHandler())) {
            CenterCutWorkProcessor.Outcome outcome = processor.processNext(
                    "worker-2", "center-cut", Duration.ofSeconds(10), Duration.ofSeconds(1)).orElseThrow();
            assertThat(outcome.status()).isEqualTo("FAILED");
            assertThat(outcome.code()).isEqualTo("CENTER_CUT_RETRY_LIMIT_EXHAUSTED");
        }
        verify(exhausted.repository).complete(eq(exhausted.claim), eq("FAILED"), eq(null),
                eq("CENTER_CUT_RETRY_LIMIT_EXHAUSTED: temporary"));
    }

    @Test
    void unknownIsPersistedForExplicitReconciliationAndNeverAutoRetried() {
        Fixture fixture = fixture(work(0, 3));
        CenterCutHandler unknown = handler(context ->
                new CenterCutHandler.Result("UNKNOWN_RESULT", null, "probe required", false, true));
        try (CenterCutWorkProcessor processor = fixture.processor(unknown)) {
            assertThat(processor.processNext("worker-1", "center-cut",
                    Duration.ofSeconds(10), Duration.ofSeconds(1)).orElseThrow().status())
                    .isEqualTo("UNKNOWN_RESULT");
        }
        verify(fixture.repository).complete(fixture.claim, "UNKNOWN_RESULT", null, "probe required");
        verify(fixture.repository, never()).complete(fixture.claim, "RETRY", null, "probe required");
    }

    private static CenterCutHandler retryHandler() {
        return handler(context -> new CenterCutHandler.Result("RETRY", null, "temporary", true, false));
    }

    private static CenterCutHandler handler(
            java.util.function.Function<CenterCutHandler.Context,CenterCutHandler.Result> action) {
        return new CenterCutHandler() {
            @Override public String handlerKey() { return "cpfDomainInvocationCenterCutHandler"; }
            @Override public Result handle(Context context) { return action.apply(context); }
        };
    }

    private static Fixture fixture(JdbcCenterCutClaimRepository.Work work) {
        JdbcCenterCutClaimRepository repository = mock(JdbcCenterCutClaimRepository.class);
        JdbcCenterCutClaimRepository.Claim claim = new JdbcCenterCutClaimRepository.Claim(
                11L, "worker-1", "claim-1", 7L, Instant.now().plusSeconds(30), "execution-1");
        when(repository.claimNext(any(), any(), any())).thenReturn(Optional.of(claim));
        when(repository.load(claim)).thenReturn(work);
        when(repository.renew(eq(claim), any())).thenReturn(true);
        return new Fixture(repository, claim);
    }

    private static JdbcCenterCutClaimRepository.Work work(int retryCount,int retryLimit) {
        return new JdbcCenterCutClaimRepository.Work(
                11L, "execution-1", "business-1",
                "{\"systemCode\":\"MBR\",\"operationId\":\"MBR_SAMPLE_TX_CREATE\",\"request\":{}}",
                "CPF_BAT_CENTER_CUT_JOB", TX, "segment-1", "parent-1",
                "cpfDomainInvocationCenterCutHandler", "CPF_BAT_CENTER_CUT_JOB",
                retryCount, retryLimit);
    }

    private record Fixture(
            JdbcCenterCutClaimRepository repository,
            JdbcCenterCutClaimRepository.Claim claim) {
        CenterCutWorkProcessor processor(CenterCutHandler handler) {
            CpfExecutionIdGenerator ids = new CpfExecutionIdGenerator() {
                @Override public String newExecutionId() { return "worker-execution-1"; }
                @Override public String newSegmentId() { return "unused-segment"; }
            };
            return new CenterCutWorkProcessor(repository, List.of(handler), List.of(), ids, "BAT");
        }
    }
}

package com.cpf.batch.centercut.runner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cpf.batch.api.BatchJobDefinition;
import com.cpf.batch.api.BatchStepDefinition;
import com.cpf.batch.centercut.runner.internal.JdbcCenterCutClaimRepository;
import com.cpf.batch.runtime.BatchRuntimePolicy;
import com.cpf.batch.spi.BatchStepHandler;
import com.cpf.batch.spi.CenterCutHandler;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;

class SpringBatchCenterCutStepHandlerTest {
    @Test
    void processesApprovedItemInsideSpringBatchStepAndAggregatesCounts() {
        JdbcCenterCutClaimRepository repository = mock(JdbcCenterCutClaimRepository.class);
        CenterCutHandler business = handler("handler-a",
                new CenterCutHandler.Result("COMPLETED", "result", "done", false, false));
        JdbcCenterCutClaimRepository.Claim claim = claim();
        when(repository.claimForExecution(eq("cc-exec"), eq("runner-a"), eq("pool-a"), any()))
                .thenReturn(Optional.of(claim), Optional.empty());
        when(repository.renew(eq(claim), any())).thenReturn(true);
        when(repository.load(claim)).thenReturn(work("handler-a"));

        try (SpringBatchCenterCutStepHandler consumer = consumer(repository, List.of(business))) {
            BatchStepHandler.BatchStepResult result = consumer.execute(command("handler-a", "cc-exec"));

            assertThat(result.status()).isEqualTo(BatchStepHandler.Status.COMPLETED);
            assertThat(result.readCount()).isEqualTo(1);
            assertThat(result.writeCount()).isEqualTo(1);
            assertThat(result.checkpoint()).containsEntry("centerCut.executionId", "cc-exec")
                    .containsEntry("centerCut.lastItemId", 51L);
            verify(repository).complete(claim, "SUCCESS", "result", "done");
        }
    }

    @Test
    void rejectsApprovalBindingMismatchWithoutInvokingBusinessHandler() {
        JdbcCenterCutClaimRepository repository = mock(JdbcCenterCutClaimRepository.class);
        CenterCutHandler business = handler("handler-a",
                new CenterCutHandler.Result("COMPLETED", "result", "done", false, false));
        JdbcCenterCutClaimRepository.Claim claim = claim();
        when(repository.claimForExecution(eq("cc-exec"), eq("runner-a"), eq("pool-a"), any()))
                .thenReturn(Optional.of(claim));
        when(repository.load(claim)).thenReturn(work("handler-b"));

        try (SpringBatchCenterCutStepHandler consumer = consumer(repository, List.of(business))) {
            BatchStepHandler.BatchStepResult result = consumer.execute(command("handler-a", "cc-exec"));

            assertThat(result.status()).isEqualTo(BatchStepHandler.Status.FAILED);
            verify(repository).complete(claim, "FAILED", null,
                    "Approved Center-Cut handler binding mismatch");
            verify(business, never()).handle(any());
        }
    }

    @Test
    void leaseLossBecomesUnknownWithoutStaleCompletion() {
        JdbcCenterCutClaimRepository repository = mock(JdbcCenterCutClaimRepository.class);
        CenterCutHandler business = handler("handler-a",
                new CenterCutHandler.Result("COMPLETED", "result", "done", false, false));
        JdbcCenterCutClaimRepository.Claim claim = claim();
        when(repository.claimForExecution(eq("cc-exec"), eq("runner-a"), eq("pool-a"), any()))
                .thenReturn(Optional.of(claim));
        when(repository.load(claim)).thenReturn(work("handler-a"));
        when(repository.renew(eq(claim), any())).thenReturn(false);

        try (SpringBatchCenterCutStepHandler consumer = consumer(repository, List.of(business))) {
            BatchStepHandler.BatchStepResult result = consumer.execute(command("handler-a", "cc-exec"));

            assertThat(result.status()).isEqualTo(BatchStepHandler.Status.UNKNOWN_RESULT);
            assertThat(result.code()).isEqualTo(SpringBatchCenterCutRuntimeState.LEASE_LOST);
            verify(business, never()).handle(any());
            verify(repository, never()).complete(eq(claim), any(), any(), any());
        }
    }

    @Test
    void requiresImmutableCenterCutExecutionParameterBinding() {
        JdbcCenterCutClaimRepository repository = mock(JdbcCenterCutClaimRepository.class);
        try (SpringBatchCenterCutStepHandler consumer = consumer(repository, List.of())) {
            assertThatThrownBy(() -> consumer.execute(commandWithDefinitionAndRequest(
                    "handler-a", "definition-exec", "request-exec")))
                    .isInstanceOf(SecurityException.class)
                    .hasMessage("CENTER_CUT_EXECUTION_PARAMETER_MISMATCH");
            verify(repository, never()).claimForExecution(any(), any(), any(), any());
        }
    }

    private static SpringBatchCenterCutStepHandler consumer(
            JdbcCenterCutClaimRepository repository, List<CenterCutHandler> handlers) {
        return new SpringBatchCenterCutStepHandler(
                repository,
                handlers,
                new SpringBatchCenterCutRuntimeState(new BatchRuntimePolicy(), 2),
                "runner-a",
                "pool-a",
                Duration.ofSeconds(30),
                5_000,
                Executors.newSingleThreadScheduledExecutor());
    }

    private static CenterCutHandler handler(String key, CenterCutHandler.Result result) {
        CenterCutHandler handler = mock(CenterCutHandler.class);
        when(handler.handlerKey()).thenReturn(key);
        try {
            when(handler.handle(any())).thenReturn(result);
        } catch (Exception impossible) {
            throw new AssertionError(impossible);
        }
        return handler;
    }

    private static JdbcCenterCutClaimRepository.Claim claim() {
        return new JdbcCenterCutClaimRepository.Claim(
                51L, "runner-a", "claim-a", 9L, Instant.now().plusSeconds(30), "cc-exec");
    }

    private static JdbcCenterCutClaimRepository.Work work(String handlerKey) {
        return new JdbcCenterCutClaimRepository.Work(
                51L, "cc-exec", "business-a", "{}", "center-job-a",
                "transaction-a", "segment-a", handlerKey, "batch-job-a");
    }

    private static BatchStepHandler.BatchStepCommand command(String handlerKey, String executionId) {
        return commandWithDefinitionAndRequest(handlerKey, null, executionId);
    }

    private static BatchStepHandler.BatchStepCommand commandWithDefinitionAndRequest(
            String handlerKey, String definedExecutionId, String requestedExecutionId) {
        Map<String, Object> definitionParameters = definedExecutionId == null
                ? Map.of()
                : Map.of("centerCutExecutionId", definedExecutionId);
        BatchStepDefinition step = new BatchStepDefinition(
                "center-cut-items",
                BatchJobDefinition.ExecutorType.SPRING_BATCH,
                "CENTER_CUT:" + handlerKey,
                definitionParameters,
                1,
                "",
                "",
                true);
        return new BatchStepHandler.BatchStepCommand(
                "cpf-exec", 101L, 201L, 7L, step,
                Map.of("arg.centerCutExecutionId", requestedExecutionId), Map.of());
    }
}

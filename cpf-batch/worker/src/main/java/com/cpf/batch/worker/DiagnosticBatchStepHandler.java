package com.cpf.batch.worker;

import com.cpf.batch.api.BatchJobDefinition;
import com.cpf.batch.spi.BatchStepHandler;
import com.cpf.foundation.runtime.CpfInstanceIdentity;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Local 전용 분산 실행 진단도 실제 Worker StepExecution 경계 안에서 수행합니다. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnProperty(name = "cpf.batch.diagnostic.enabled", havingValue = "true")
final class DiagnosticBatchStepHandler implements BatchStepHandler {
    static final String REFERENCE = "SERVICE:CPF_BAT_DIAGNOSTIC";
    private final SpringBatchWorkerRuntimeState runtime;
    private final WorkerExecutionTracker executions;

    DiagnosticBatchStepHandler(
            SpringBatchWorkerRuntimeState runtime,
            WorkerExecutionTracker executions) {
        this.runtime = runtime;
        this.executions = executions;
    }

    @Override
    public boolean supports(BatchJobDefinition.ExecutorType type, String reference) {
        return type == BatchJobDefinition.ExecutorType.SERVICE_CALL && REFERENCE.equals(reference);
    }

    @Override
    public BatchStepResult execute(BatchStepCommand command) throws Exception {
        long sleepMs = parseSleep(command.step().parameters().get("sleepMs"));
        try (WorkerExecutionTracker.Scope _ = executions.begin(
                command.cpfExecutionId(), command.jobExecutionId(), command.fencingToken())) {
            if (sleepMs > 0) {
                Thread.sleep(sleepMs);
            }
            return BatchStepResult.completed(
                    "CPF_BAT_DIAGNOSTIC_COMPLETED",
                    1,
                    1,
                    Map.of(
                            "diagnostic.workerId", runtime.workerId(),
                            "diagnostic.instanceId", CpfInstanceIdentity.instanceId()));
        }
    }

    private static long parseSleep(Object value) {
        try {
            long parsed = Long.parseLong(String.valueOf(value));
            if (parsed < 0 || parsed > 60_000L) {
                throw new IllegalArgumentException("diagnostic sleepMs must be between 0 and 60000");
            }
            return parsed;
        } catch (NumberFormatException failure) {
            throw new IllegalArgumentException("diagnostic sleepMs must be an integer", failure);
        }
    }
}

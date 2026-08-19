package com.cpf.education.batch.conditionalflow.service;

import com.cpf.foundation.annotation.CpfService;

import com.cpf.batch.spi.BatchStepHandler.BatchStepCommand;
import com.cpf.batch.spi.BatchStepHandler.BatchStepResult;
import java.util.Map;

/** 배치-10 Multi-Step·조건 Flow: Step A 완료 후 조건분기하고 Step B 실패 시 재진입 기준을 checkpoint에 남깁니다. */
@CpfService
public class CustomerConditionalFlowJobService {
    public BatchStepResult stepA(BatchStepCommand command) {
        boolean valid = Boolean.parseBoolean(String.valueOf(command.jobParameters().getOrDefault("valid", true)));
        if (!valid) {
            return new BatchStepResult(
                    BatchStepResult.Status.FAILED,
                    "VALIDATION_FAILED",
                    "Step A 실패 Route에서 Job을 종료합니다.",
                    1, 0, 0,
                    Map.of("stepA", "FAILED", "nextRoute", "END"));
        }
        return BatchStepResult.completed(
                "Step A committed",
                1, 1,
                Map.of("stepA", "COMPLETED", "nextRoute", "STEP_B"));
    }

    /** stepB 단계는 CPF Public 계약을 사용해 이 EDU 시나리오의 업무 흐름을 수행합니다. */
    public BatchStepResult stepB(BatchStepCommand command) {
        boolean fail = Boolean.parseBoolean(String.valueOf(command.jobParameters().getOrDefault("failStepB", false)));
        if (fail) {
            return new BatchStepResult(
                    BatchStepResult.Status.RETRYABLE_FAILURE,
                    "STEP_B_RETRY",
                    "Step A 완료는 유지하고 Step B checkpoint부터 재진입합니다.",
                    1, 0, 0,
                    Map.of("stepA", "COMPLETED", "stepB", "RETRY", "restartFrom", "STEP_B"));
        }
        return BatchStepResult.completed(
                "conditional flow completed",
                1, 1,
                Map.of("stepA", "COMPLETED", "stepB", "COMPLETED", "final", "COMPLETED"));
    }
}

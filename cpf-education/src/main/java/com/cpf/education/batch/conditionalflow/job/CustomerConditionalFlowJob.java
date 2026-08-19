package com.cpf.education.batch.conditionalflow.job;

import com.cpf.batch.api.annotation.CpfBatchJob;
import com.cpf.batch.api.annotation.CpfBatchStep;
import com.cpf.batch.spi.BatchStepHandler.BatchStepCommand;
import com.cpf.batch.spi.BatchStepHandler.BatchStepResult;
import com.cpf.education.batch.conditionalflow.service.CustomerConditionalFlowJobService;

@CpfBatchJob(value = "EDU_CUSTOMER_FLOW_JOB")
/** Canonical Batch Job entrypoint. Scenario logic is separated into the feature service role. */
/** 조건 분기 교육 예제의 Job 역할과 CPF 표준 사용 경계를 보여줍니다. */
public class CustomerConditionalFlowJob {
    private final CustomerConditionalFlowJobService service;
    /** 조건 분기 예제의 Job 의존성을 주입해 표준 실행 경계를 구성합니다. */
    public CustomerConditionalFlowJob(CustomerConditionalFlowJobService service) { this.service = service; }

    @CpfBatchStep(value = "step-a-validate", order = 1)
    /** 조건 분기 예제의 stepA 단계를 실행하고 실제 처리 책임은 분리된 역할로 위임합니다. */
    public BatchStepResult stepA(BatchStepCommand command) { return service.stepA(command); }

    @CpfBatchStep(value = "step-b-process", order = 2)
    /** 조건 분기 예제의 stepB 단계를 실행하고 실제 처리 책임은 분리된 역할로 위임합니다. */
    public BatchStepResult stepB(BatchStepCommand command) { return service.stepB(command); }
}

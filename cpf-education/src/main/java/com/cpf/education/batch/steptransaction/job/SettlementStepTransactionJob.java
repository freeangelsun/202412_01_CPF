package com.cpf.education.batch.steptransaction.job;

import com.cpf.batch.api.annotation.CpfBatchJob;
import com.cpf.batch.api.annotation.CpfBatchStep;
import com.cpf.batch.spi.BatchStepHandler.BatchStepCommand;
import com.cpf.batch.spi.BatchStepHandler.BatchStepResult;
import com.cpf.education.batch.steptransaction.service.SettlementStepTransactionJobService;

@CpfBatchJob(value="EDU_SETTLEMENT_STEP_TX_JOB")
/** Canonical Batch Job entrypoint. Scenario logic is separated into the feature service role. */
/** Step Transaction 교육 예제의 Job 역할과 CPF 표준 사용 경계를 보여줍니다. */
public class SettlementStepTransactionJob {
    private final SettlementStepTransactionJobService service;
    /** Step Transaction 예제의 Job 의존성을 주입해 표준 실행 경계를 구성합니다. */
    public SettlementStepTransactionJob(SettlementStepTransactionJobService service) { this.service = service; }

    @CpfBatchStep(value="step-a",order=1)
    /** Step Transaction 예제의 stepA 단계를 실행하고 실제 처리 책임은 분리된 역할로 위임합니다. */
    public BatchStepResult stepA(BatchStepCommand command) { return service.stepA(command); }

    @CpfBatchStep(value="step-b",order=2)
    /** Step Transaction 예제의 stepB 단계를 실행하고 실제 처리 책임은 분리된 역할로 위임합니다. */
    public BatchStepResult stepB(BatchStepCommand command) { return service.stepB(command); }
}

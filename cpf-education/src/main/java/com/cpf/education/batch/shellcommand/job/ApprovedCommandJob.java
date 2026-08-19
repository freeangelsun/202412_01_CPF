package com.cpf.education.batch.shellcommand.job;

import com.cpf.batch.api.annotation.CpfBatchJob;
import com.cpf.batch.api.annotation.CpfBatchStep;
import com.cpf.batch.spi.BatchStepHandler.BatchStepCommand;
import com.cpf.batch.spi.BatchStepHandler.BatchStepResult;
import com.cpf.education.batch.shellcommand.service.ApprovedCommandJobService;

@CpfBatchJob(value="EDU_APPROVED_COMMAND_JOB")
/** Canonical Batch Job entrypoint. Scenario logic is separated into the feature service role. */
/** 승인형 Shell Command 교육 예제의 Job 역할과 CPF 표준 사용 경계를 보여줍니다. */
public class ApprovedCommandJob {
    private final ApprovedCommandJobService service;
    /** 승인형 Shell Command 예제의 Job 의존성을 주입해 표준 실행 경계를 구성합니다. */
    public ApprovedCommandJob(ApprovedCommandJobService service) { this.service = service; }

    @CpfBatchStep(value="approved-command",order=1)
    /** 승인형 Shell Command 예제의 run 단계를 실행하고 실제 처리 책임은 분리된 역할로 위임합니다. */
    public BatchStepResult run(BatchStepCommand command) { return service.run(command); }
}

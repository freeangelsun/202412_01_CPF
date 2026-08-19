package com.cpf.education.batch.ondemand.job;

import com.cpf.batch.api.annotation.CpfBatchJob;
import com.cpf.batch.api.annotation.CpfBatchStep;
import com.cpf.batch.spi.BatchStepHandler.BatchStepCommand;
import com.cpf.batch.spi.BatchStepHandler.BatchStepResult;
import com.cpf.education.batch.ondemand.service.MemberExportJobService;

@CpfBatchJob(value = "EDU_MEMBER_EXPORT_JOB")
/** Canonical Batch Job entrypoint. Scenario logic is separated into the feature service role. */
/** On-Demand Batch 교육 예제의 Job 역할과 CPF 표준 사용 경계를 보여줍니다. */
public class MemberExportJob {
    private final MemberExportJobService service;
    /** On-Demand Batch 예제의 Job 의존성을 주입해 표준 실행 경계를 구성합니다. */
    public MemberExportJob(MemberExportJobService service) { this.service = service; }

    @CpfBatchStep(value = "on-demand", order = 1)
    /** On-Demand Batch 예제의 run 단계를 실행하고 실제 처리 책임은 분리된 역할로 위임합니다. */
    public BatchStepResult run(BatchStepCommand command) { return service.run(command); }
}

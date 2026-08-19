package com.cpf.education.batch.scheduler.job;

import com.cpf.batch.api.annotation.CpfBatchJob;
import com.cpf.batch.api.annotation.CpfBatchStep;
import com.cpf.batch.spi.BatchStepHandler.BatchStepCommand;
import com.cpf.batch.spi.BatchStepHandler.BatchStepResult;
import com.cpf.education.batch.scheduler.service.BusinessDateSchedulerJobService;

@CpfBatchJob(value = "EDU_BUSINESS_DATE_JOB")
/** Canonical Batch Job entrypoint. Scenario logic is separated into the feature service role. */
/** Scheduler 교육 예제의 Job 역할과 CPF 표준 사용 경계를 보여줍니다. */
public class BusinessDateSchedulerJob {
    private final BusinessDateSchedulerJobService service;
    /** Scheduler 예제의 Job 의존성을 주입해 표준 실행 경계를 구성합니다. */
    public BusinessDateSchedulerJob(BusinessDateSchedulerJobService service) { this.service = service; }

    @CpfBatchStep(value = "business-day", order = 1)
    /** Scheduler 예제의 run 단계를 실행하고 실제 처리 책임은 분리된 역할로 위임합니다. */
    public BatchStepResult run(BatchStepCommand command) { return service.run(command); }
}

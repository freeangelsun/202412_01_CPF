package com.cpf.batch.job.centercut;

import com.cpf.core.common.batch.CpfBatchRuntimeListener;
import com.cpf.core.common.execution.CpfBatchJob;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/** 센터컷 대상 조회와 건별 실행 결과 집계를 검증하는 독립 JobDefinition입니다. */
@Configuration
public class BatCenterCutJobConfig {
    public static final String JOB_ID = "CPF_BAT_CENTER_CUT_JOB";
    public static final String STEP_ID = "CPF_BAT_CENTER_CUT_STEP";

    @Bean
    @CpfBatchJob(id = "BBATCU0001", name = "BAT센터컷검증배치", ownerDomain = "BAT")
    public Job cpfBatCenterCutJob(
            JobRepository jobRepository,
            Step cpfBatCenterCutStep,
            CpfBatchRuntimeListener listener) {
        return new JobBuilder(JOB_ID, jobRepository).listener(listener).start(cpfBatCenterCutStep).build();
    }

    @Bean
    public Step cpfBatCenterCutStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            BatCenterCutSmokeTasklet tasklet,
            CpfBatchRuntimeListener listener) {
        return new StepBuilder(STEP_ID, jobRepository)
                .listener(listener)
                .tasklet(tasklet, transactionManager)
                .build();
    }
}

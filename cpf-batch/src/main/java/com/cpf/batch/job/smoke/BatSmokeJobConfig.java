package com.cpf.batch.job.smoke;

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

/**
 * BAT 독립 실행 검증을 위한 최소 Job과 Step을 구성합니다.
 *
 * <p>이 Job은 운영 업무 배치가 아니라 BAT runtime, Spring Batch JobRepository,
 * CPF Batch 공통 API, CPF 운영 메타 연결을 검증하기 위한 기준 Job입니다.</p>
 */
@Configuration
public class BatSmokeJobConfig {
    public static final String SMOKE_JOB_ID = "CPF_BAT_SMOKE_JOB";
    public static final String SMOKE_STEP_ID = "CPF_BAT_SMOKE_STEP";

    @Bean
    @CpfBatchJob(id = "BBATOP0003", name = "BAT기동검증배치", ownerDomain = "BAT")
    public Job cpfBatSmokeJob(
            JobRepository jobRepository,
            Step cpfBatSmokeStep,
            CpfBatchRuntimeListener cpfBatchRuntimeListener) {
        return new JobBuilder(SMOKE_JOB_ID, jobRepository)
                .listener(cpfBatchRuntimeListener)
                .start(cpfBatSmokeStep)
                .build();
    }

    @Bean
    public Step cpfBatSmokeStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            BatSmokeTasklet batSmokeTasklet,
            CpfBatchRuntimeListener cpfBatchRuntimeListener) {
        return new StepBuilder(SMOKE_STEP_ID, jobRepository)
                .listener(cpfBatchRuntimeListener)
                .tasklet(batSmokeTasklet, transactionManager)
                .build();
    }

}

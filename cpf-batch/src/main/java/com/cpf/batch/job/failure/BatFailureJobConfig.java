package com.cpf.batch.job.failure;

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

/** 실패 상태와 운영 로그 연결을 검증하는 독립 JobDefinition입니다. */
@Configuration
public class BatFailureJobConfig {
    public static final String JOB_ID = "CPF_BAT_FAIL_JOB";
    public static final String STEP_ID = "CPF_BAT_FAIL_STEP";

    @Bean
    @CpfBatchJob(id = "BBATOP0001", name = "BAT실패검증배치", ownerDomain = "BAT")
    public Job cpfBatFailJob(
            JobRepository jobRepository,
            Step cpfBatFailStep,
            CpfBatchRuntimeListener listener) {
        return new JobBuilder(JOB_ID, jobRepository).listener(listener).start(cpfBatFailStep).build();
    }

    @Bean
    public Step cpfBatFailStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            BatFailTasklet tasklet,
            CpfBatchRuntimeListener listener) {
        return new StepBuilder(STEP_ID, jobRepository)
                .listener(listener)
                .tasklet(tasklet, transactionManager)
                .build();
    }
}

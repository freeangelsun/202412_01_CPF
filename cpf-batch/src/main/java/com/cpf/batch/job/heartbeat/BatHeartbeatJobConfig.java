package com.cpf.batch.job.heartbeat;

import com.cpf.core.common.batch.CpfBatchRuntimeListener;
import com.cpf.core.common.execution.CpfBatchJob;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/** worker heartbeat와 ghost 판정 입력을 검증하는 독립 JobDefinition입니다. */
@Configuration
public class BatHeartbeatJobConfig {
    public static final String JOB_ID = "CPF_BAT_HEARTBEAT_JOB";
    public static final String STEP_ID = "CPF_BAT_HEARTBEAT_STEP";

    @Bean
    @CpfBatchJob(id = "BBATOP0002", name = "BAT하트비트검증배치", ownerDomain = "BAT")
    public Job cpfBatHeartbeatJob(
            JobRepository jobRepository,
            Step cpfBatHeartbeatStep,
            CpfBatchRuntimeListener listener) {
        return new JobBuilder(JOB_ID, jobRepository).listener(listener).start(cpfBatHeartbeatStep).build();
    }

    @Bean
    public Step cpfBatHeartbeatStep(
            JobRepository jobRepository,
            @Qualifier("batTransactionManager") PlatformTransactionManager transactionManager,
            BatHeartbeatSmokeTasklet tasklet,
            CpfBatchRuntimeListener listener) {
        return new StepBuilder(STEP_ID, jobRepository)
                .listener(listener)
                .tasklet(tasklet, transactionManager)
                .build();
    }
}

package cpf.bat.job;

import cpf.pfw.common.batch.CpfBatchRuntimeListener;
import cpf.pfw.common.execution.CpfBatchJob;
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
 * PFW Batch 공통 API, CPF 운영 메타 연결을 검증하기 위한 기준 Job입니다.</p>
 */
@Configuration
public class BatSmokeJobConfig {
    public static final String SMOKE_JOB_ID = "CPF_BAT_SMOKE_JOB";
    public static final String SMOKE_STEP_ID = "CPF_BAT_SMOKE_STEP";
    public static final String FAIL_JOB_ID = "CPF_BAT_FAIL_JOB";
    public static final String FAIL_STEP_ID = "CPF_BAT_FAIL_STEP";
    public static final String HEARTBEAT_JOB_ID = "CPF_BAT_HEARTBEAT_JOB";
    public static final String HEARTBEAT_STEP_ID = "CPF_BAT_HEARTBEAT_STEP";
    public static final String CENTER_CUT_JOB_ID = "CPF_BAT_CENTER_CUT_JOB";
    public static final String CENTER_CUT_STEP_ID = "CPF_BAT_CENTER_CUT_STEP";

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

    @Bean
    @CpfBatchJob(id = "BBATOP0001", name = "BAT실패검증배치", ownerDomain = "BAT")
    public Job cpfBatFailJob(
            JobRepository jobRepository,
            Step cpfBatFailStep,
            CpfBatchRuntimeListener cpfBatchRuntimeListener) {
        return new JobBuilder(FAIL_JOB_ID, jobRepository)
                .listener(cpfBatchRuntimeListener)
                .start(cpfBatFailStep)
                .build();
    }

    @Bean
    public Step cpfBatFailStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            BatFailTasklet batFailTasklet,
            CpfBatchRuntimeListener cpfBatchRuntimeListener) {
        return new StepBuilder(FAIL_STEP_ID, jobRepository)
                .listener(cpfBatchRuntimeListener)
                .tasklet(batFailTasklet, transactionManager)
                .build();
    }

    @Bean
    @CpfBatchJob(id = "BBATOP0002", name = "BAT하트비트검증배치", ownerDomain = "BAT")
    public Job cpfBatHeartbeatJob(
            JobRepository jobRepository,
            Step cpfBatHeartbeatStep,
            CpfBatchRuntimeListener cpfBatchRuntimeListener) {
        return new JobBuilder(HEARTBEAT_JOB_ID, jobRepository)
                .listener(cpfBatchRuntimeListener)
                .start(cpfBatHeartbeatStep)
                .build();
    }

    @Bean
    public Step cpfBatHeartbeatStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            BatHeartbeatSmokeTasklet batHeartbeatSmokeTasklet,
            CpfBatchRuntimeListener cpfBatchRuntimeListener) {
        return new StepBuilder(HEARTBEAT_STEP_ID, jobRepository)
                .listener(cpfBatchRuntimeListener)
                .tasklet(batHeartbeatSmokeTasklet, transactionManager)
                .build();
    }

    @Bean
    @CpfBatchJob(id = "BBATCU0001", name = "BAT센터컷검증배치", ownerDomain = "BAT")
    public Job cpfBatCenterCutJob(
            JobRepository jobRepository,
            Step cpfBatCenterCutStep,
            CpfBatchRuntimeListener cpfBatchRuntimeListener) {
        return new JobBuilder(CENTER_CUT_JOB_ID, jobRepository)
                .listener(cpfBatchRuntimeListener)
                .start(cpfBatCenterCutStep)
                .build();
    }

    @Bean
    public Step cpfBatCenterCutStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            cpf.bat.centercut.BatCenterCutSmokeTasklet batCenterCutSmokeTasklet,
            CpfBatchRuntimeListener cpfBatchRuntimeListener) {
        return new StepBuilder(CENTER_CUT_STEP_ID, jobRepository)
                .listener(cpfBatchRuntimeListener)
                .tasklet(batCenterCutSmokeTasklet, transactionManager)
                .build();
    }
}

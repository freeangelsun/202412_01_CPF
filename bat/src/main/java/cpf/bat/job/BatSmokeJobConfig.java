package cpf.bat.job;

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

    @Bean
    public Job cpfBatSmokeJob(JobRepository jobRepository, Step cpfBatSmokeStep) {
        return new JobBuilder(SMOKE_JOB_ID, jobRepository)
                .start(cpfBatSmokeStep)
                .build();
    }

    @Bean
    public Step cpfBatSmokeStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            BatSmokeTasklet batSmokeTasklet) {
        return new StepBuilder(SMOKE_STEP_ID, jobRepository)
                .tasklet(batSmokeTasklet, transactionManager)
                .build();
    }

    @Bean
    public Job cpfBatFailJob(JobRepository jobRepository, Step cpfBatFailStep) {
        return new JobBuilder(FAIL_JOB_ID, jobRepository)
                .start(cpfBatFailStep)
                .build();
    }

    @Bean
    public Step cpfBatFailStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            BatFailTasklet batFailTasklet) {
        return new StepBuilder(FAIL_STEP_ID, jobRepository)
                .tasklet(batFailTasklet, transactionManager)
                .build();
    }
}

package cpf.bat.edu.ondemand;

import cpf.pfw.common.execution.CpfBatchJob;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/** 온디맨드 접수 흐름을 검증하는 실제 Spring Batch Job입니다. */
@Configuration
public class BatOnDemandJobConfig {
    public static final String STANDARD_BATCH_ID = "BBATOD0001";
    public static final String JOB_NAME = "CPF_BAT_ON_DEMAND_EDU_JOB";

    @Bean
    @CpfBatchJob(
            id = "BBATOD0001",
            name = "BAT 온디맨드 EDU 배치",
            ownerDomain = "BAT",
            description = "온라인 202 접수 후 worker가 실행하는 온디맨드 교육 Job",
            requiredPermission = "BAT_ON_DEMAND_EXECUTE")
    public Job cpfBatOnDemandEducationJob(JobRepository jobRepository, Step cpfBatOnDemandEducationStep) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(cpfBatOnDemandEducationStep)
                .build();
    }

    @Bean
    public Step cpfBatOnDemandEducationStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager) {
        return new StepBuilder("CPF_BAT_ON_DEMAND_EDU_STEP", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    // 실제 프로젝트에서는 이 위치에서 멱등 가능한 업무 Service를 호출합니다.
                    contribution.incrementWriteCount(1);
                    return org.springframework.batch.repeat.RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}

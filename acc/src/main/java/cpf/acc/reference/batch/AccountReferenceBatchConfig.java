package cpf.acc.batch;

import cpf.pfw.common.execution.CpfBatchJob;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Account 주제영역의 표준 Tasklet 배치 골격입니다.
 */
@Configuration
public class AccountBatchConfig {

    @Bean
    @CpfBatchJob(id = "BACCTS0001", name = "Account표준배치", ownerDomain = "ACC")
    public Job accStandardJob(JobRepository jobRepository, Step accStandardStep) {
        return new JobBuilder("ACC_STANDARD_JOB", jobRepository)
                .start(accStandardStep)
                .build();
    }

    @Bean
    public Step accStandardStep(
            JobRepository jobRepository,
            @Qualifier("accTransactionManager") PlatformTransactionManager transactionManager) {
        return new StepBuilder("ACC_STANDARD_STEP", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    // 실제 업무 로직은 재시작 가능성과 멱등성을 보장하는 서비스에 위임합니다.
                    return org.springframework.batch.repeat.RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}

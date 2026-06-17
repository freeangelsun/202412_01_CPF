package cpf.xyz.edu.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.stream.IntStream;

/**
 * XYZ 배치 교육용 Job 설정입니다.
 *
 * <p>Spring Batch 인프라가 준비된 실행 환경에서만 Job bean을 등록합니다. 따라서 로컬 개발자가 배치 DB 설정을 아직
 * 붙이지 않은 상태에서도 XYZ 애플리케이션은 정상 기동되고, 배치 교육 API는 비활성 안내를 반환할 수 있습니다.</p>
 */
@Configuration
@ConditionalOnBean(JobRepository.class)
public class XyzBatchEducationConfig {
    private static final Logger log = LoggerFactory.getLogger(XyzBatchEducationConfig.class);

    @Bean
    public Job cpfEduTaskletJob(JobRepository jobRepository, Step cpfEduTaskletStep) {
        return new JobBuilder("CPF_EDU_TASKLET_JOB", jobRepository)
                .start(cpfEduTaskletStep)
                .build();
    }

    @Bean
    public Step cpfEduTaskletStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("CPF_EDU_TASKLET_STEP", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    // Tasklet은 파일 정리, 단건 집계, 외부 시스템 상태 확인처럼 한 번에 끝나는 작업에 적합합니다.
                    String requestUser = String.valueOf(chunkContext.getStepContext().getJobParameters().get("requestUser"));
                    log.info("CPF EDU Tasklet 배치 실행. requestUser={}", requestUser);
                    return org.springframework.batch.repeat.RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Job cpfEduChunkJob(JobRepository jobRepository, Step cpfEduChunkStep) {
        return new JobBuilder("CPF_EDU_CHUNK_JOB", jobRepository)
                .start(cpfEduChunkStep)
                .build();
    }

    @Bean
    public Step cpfEduChunkStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        List<Integer> sampleItems = IntStream.rangeClosed(1, 25).boxed().toList();
        return new StepBuilder("CPF_EDU_CHUNK_STEP", jobRepository)
                .<Integer, String>chunk(5, transactionManager)
                .reader(new ListItemReader<>(sampleItems))
                .processor(item -> {
                    // Processor는 원천 데이터를 업무 DTO나 적재 포맷으로 변환하는 계층입니다.
                    return "회원-" + String.format("%03d", item);
                })
                .writer(chunk -> {
                    // Writer는 DB insert/update, 파일 출력, 외부 전송 같은 최종 기록을 담당합니다.
                    log.info("CPF EDU Chunk 배치 기록. items={}", chunk.getItems());
                })
                .build();
    }

    @Bean
    public Job cpfEduRetryJob(JobRepository jobRepository, Step cpfEduRetryStep) {
        return new JobBuilder("CPF_EDU_RETRY_JOB", jobRepository)
                .start(cpfEduRetryStep)
                .build();
    }

    @Bean
    public Step cpfEduRetryStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("CPF_EDU_RETRY_STEP", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    // 실제 업무에서는 실패 데이터를 별도 테이블에 적재하고, 재처리 가능 상태만 다시 수행합니다.
                    log.info("CPF EDU Retry 정책 샘플 배치 실행. jobParameters={}", chunkContext.getStepContext().getJobParameters());
                    return org.springframework.batch.repeat.RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}

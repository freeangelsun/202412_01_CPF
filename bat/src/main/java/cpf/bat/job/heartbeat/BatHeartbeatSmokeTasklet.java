package cpf.bat.job;

import cpf.pfw.common.batch.CpfBatchHeartbeatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * BAT 실행 중 heartbeat와 진행률 갱신을 검증하기 위한 장시간 smoke Tasklet입니다.
 *
 * <p>실제 업무 배치가 아니라 관제 기준 검증용 Job입니다. 짧은 sleep을 여러 번 반복하며
 * read/write count와 heartbeat를 갱신해 runtime smoke가 실행 중 갱신 증거를 확인할 수 있게 합니다.</p>
 */
@Component
public class BatHeartbeatSmokeTasklet implements Tasklet {
    private static final Logger log = LoggerFactory.getLogger(BatHeartbeatSmokeTasklet.class);

    private final CpfBatchHeartbeatService heartbeatService;
    private final int iterations;
    private final long sleepMillis;

    public BatHeartbeatSmokeTasklet(
            CpfBatchHeartbeatService heartbeatService,
            @Value("${cpf.bat.smoke.heartbeat-iterations:4}") int iterations,
            @Value("${cpf.bat.smoke.heartbeat-sleep-ms:1200}") long sleepMillis) {
        this.heartbeatService = heartbeatService;
        this.iterations = Math.max(2, iterations);
        this.sleepMillis = Math.max(200L, sleepMillis);
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        StepExecution stepExecution = chunkContext.getStepContext().getStepExecution();
        for (int index = 1; index <= iterations; index++) {
            contribution.incrementReadCount();
            contribution.incrementWriteCount(1);
            String message = "heartbeatCount=" + index
                    + ", totalCount=" + iterations
                    + ", processedCount=" + index
                    + ", intervalSeconds=" + heartbeatService.heartbeatIntervalSeconds();
            heartbeatService.recordStepProgress(
                    stepExecution,
                    iterations,
                    index,
                    index,
                    0,
                    0,
                    0,
                    message);
            log.info("BAT heartbeat smoke progress {}/{}", index, iterations);
            if (index < iterations) {
                Thread.sleep(sleepMillis);
            }
        }
        return RepeatStatus.FINISHED;
    }
}

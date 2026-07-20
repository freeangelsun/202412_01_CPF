package cpf.bat.job.smoke;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

/**
 * BAT 정상 실행 흐름을 검증하는 최소 Tasklet입니다.
 */
@Component
public class BatSmokeTasklet implements Tasklet {
    private static final Logger log = LoggerFactory.getLogger(BatSmokeTasklet.class);

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.info("BAT smoke tasklet completed.");
        return RepeatStatus.FINISHED;
    }
}

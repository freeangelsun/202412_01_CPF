package com.cpf.batch.job.failure;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

/**
 * BAT 실패 흐름을 검증하기 위한 최소 Tasklet입니다.
 */
@Component
public class BatFailTasklet implements Tasklet {

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        throw new IllegalStateException("BAT 실패 흐름 검증용 예외입니다.");
    }
}

package com.cpf.batch.job.centercut;

import com.cpf.core.common.batch.CpfBatchHeartbeatService;
import com.cpf.core.common.batch.centercut.CpfCenterCutService;
import com.cpf.core.common.batch.centercut.CpfCenterCutSummary;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * BAT standalone 환경에서 center-cut 기본 흐름을 검증하는 smoke Tasklet입니다.
 */
@Component
public class BatCenterCutSmokeTasklet implements Tasklet {
    private final CpfCenterCutService centerCutService;
    private final BatCenterCutSampleTargetProvider targetProvider;
    private final BatCenterCutSampleHandler handler;
    private final CpfBatchHeartbeatService heartbeatService;
    private final int limit;

    public BatCenterCutSmokeTasklet(
            CpfCenterCutService centerCutService,
            BatCenterCutSampleTargetProvider targetProvider,
            BatCenterCutSampleHandler handler,
            CpfBatchHeartbeatService heartbeatService,
            @Value("${cpf.bat.center-cut.smoke-limit:10}") int limit) {
        this.centerCutService = centerCutService;
        this.targetProvider = targetProvider;
        this.handler = handler;
        this.heartbeatService = heartbeatService;
        this.limit = Math.max(1, limit);
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        targetProvider.reset();
        CpfCenterCutSummary summary = centerCutService.execute(
                "CPF_BAT_CENTER_CUT_JOB",
                limit,
                targetProvider,
                handler);
        for (int index = 0; index < summary.requestedCount(); index++) {
            contribution.incrementReadCount();
        }
        contribution.incrementWriteCount(summary.successCount());
        StepExecution stepExecution = chunkContext.getStepContext().getStepExecution();
        heartbeatService.recordStepProgress(
                stepExecution,
                summary.requestedCount(),
                summary.successCount() + summary.failedCount() + summary.skippedCount(),
                summary.successCount(),
                summary.failedCount(),
                summary.skippedCount(),
                summary.retryRequestedCount(),
                "centerCutRequested=" + summary.requestedCount()
                        + ", centerCutSuccess=" + summary.successCount()
                        + ", centerCutFailed=" + summary.failedCount());
        return RepeatStatus.FINISHED;
    }
}

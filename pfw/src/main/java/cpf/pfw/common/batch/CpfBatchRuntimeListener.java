package cpf.pfw.common.batch;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

/**
 * Spring Batch Job/Step 생명주기를 CPF 운영 메타와 연결하는 listener입니다.
 *
 * <p>배치 개발자는 신규 Job과 Step을 만들 때 이 listener를 연결해 실행 시작, Step 시작,
 * Step 종료, Job 종료 상태가 ADM 관제 메타에 남도록 해야 합니다.</p>
 */
public class CpfBatchRuntimeListener implements JobExecutionListener, StepExecutionListener {
    private final CpfBatchHeartbeatService heartbeatService;

    public CpfBatchRuntimeListener(CpfBatchHeartbeatService heartbeatService) {
        this.heartbeatService = heartbeatService;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        heartbeatService.recordJobStarted(jobExecution);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        heartbeatService.recordJobFinished(jobExecution);
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        heartbeatService.recordStepStarted(stepExecution);
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        heartbeatService.recordStepFinished(stepExecution);
        return stepExecution.getExitStatus();
    }
}

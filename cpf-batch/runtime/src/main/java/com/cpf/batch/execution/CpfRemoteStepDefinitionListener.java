package com.cpf.batch.execution;

import com.cpf.batch.api.BatchStepDefinition;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;

/** RemoteStep 요청을 보내기 전에 Step 정의를 공유 ExecutionContext에 기록합니다. */
final class CpfRemoteStepDefinitionListener implements StepExecutionListener {
    private final BatchStepDefinition definition;
    CpfRemoteStepDefinitionListener(BatchStepDefinition definition) { this.definition = definition; }
    @Override public void beforeStep(StepExecution stepExecution) {
        CpfRemoteStepDefinition.write(stepExecution.getExecutionContext(), definition);
    }
    @Override public ExitStatus afterStep(StepExecution stepExecution) { return stepExecution.getExitStatus(); }
}

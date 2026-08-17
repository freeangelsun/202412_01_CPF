package com.cpf.education.batch;

import com.cpf.batch.api.annotation.CpfBatchJob;
import com.cpf.batch.api.annotation.CpfBatchStep;
import com.cpf.batch.spi.BatchStepHandler.BatchStepCommand;
import com.cpf.batch.spi.BatchStepHandler.BatchStepResult;
import java.util.Map;
import com.cpf.batch.api.BatchJobDefinition;
@CpfBatchJob(value="EDU-BATCH-09")
/** 배치-09 Shell·Command 실행 배치: CPF Batch Runtime과 실행 추적 계약을 사용하는 Canonical 실행 예제입니다. */
public class Batch09ApprovedShellExample {
 @CpfBatchStep(value="approved-command",order=1) public BatchStepResult run(BatchStepCommand command){String reference=String.valueOf(command.jobParameters().getOrDefault("executorReference","SCRIPT:EDU_HEALTH_CHECK"));if(!reference.startsWith("SCRIPT:"))return new BatchStepResult(BatchStepResult.Status.FAILED,"SCRIPT_NOT_APPROVED","승인된 SCRIPT catalog reference만 실행할 수 있습니다.",0,0,0,Map.of());return BatchStepResult.completed("command delegated to "+BatchJobDefinition.ExecutorType.APPROVED_SHELL,0,0,Map.of("executorReference",reference));}
}

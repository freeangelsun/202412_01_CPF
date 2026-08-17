package com.cpf.education.batch;

import com.cpf.batch.api.annotation.CpfBatchJob;
import com.cpf.batch.api.annotation.CpfBatchStep;
import com.cpf.batch.spi.BatchStepHandler.BatchStepCommand;
import com.cpf.batch.spi.BatchStepHandler.BatchStepResult;
import java.util.Map;

@CpfBatchJob(value="EDU-BATCH-01")
/** 배치-01 일반 Tasklet 배치잡: CPF Batch Runtime과 실행 추적 계약을 사용하는 Canonical 실행 예제입니다. */
public class Batch01TaskletExample {
 @CpfBatchStep(value="main",order=1)
 /** 해당 Canonical 배치 예제의 Step을 실행하고 처리 결과를 CPF Batch 결과로 반환합니다. */
 public BatchStepResult run(BatchStepCommand command) throws Exception { return BatchStepResult.completed("tasklet completed",0,0,Map.of("executionId",command.cpfExecutionId())); }
}

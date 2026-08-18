package com.cpf.education.batch.chunktransaction;

import com.cpf.batch.api.annotation.CpfBatchJob;
import com.cpf.batch.api.annotation.CpfBatchStep;
import com.cpf.batch.spi.BatchStepHandler.BatchStepCommand;
import com.cpf.batch.spi.BatchStepHandler.BatchStepResult;
import java.util.Map;
import com.cpf.data.persistence.api.annotation.CpfTransactional; import org.springframework.transaction.annotation.Propagation;
@CpfBatchJob(value="EDU_CUSTOMER_CHUNK_TX_JOB")
/** 배치-11 Chunk Transaction 경계: CPF Batch Runtime과 실행 추적 계약을 사용하는 Canonical 실행 예제입니다. */
public class CustomerChunkTransactionJob {
 @CpfBatchStep(value="chunk-transaction",order=1) @CpfTransactional(propagation=Propagation.REQUIRED) public BatchStepResult run(BatchStepCommand command){int chunk=((Number)command.jobParameters().getOrDefault("chunkSize",100)).intValue();return BatchStepResult.completed("one transaction per CPF chunk boundary",chunk,chunk,Map.of("chunkSize",chunk,"checkpoint",command.stepExecutionId()));}
}

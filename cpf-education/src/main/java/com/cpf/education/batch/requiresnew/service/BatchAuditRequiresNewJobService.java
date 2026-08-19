package com.cpf.education.batch.requiresnew.service;

import com.cpf.foundation.annotation.CpfService;

import com.cpf.batch.spi.BatchStepHandler.BatchStepCommand;
import com.cpf.batch.spi.BatchStepHandler.BatchStepResult;
import java.util.Map;
import com.cpf.data.persistence.api.annotation.CpfTransactional; import org.springframework.transaction.annotation.Propagation;
/** 배치-12 REQUIRES_NEW 독립 Transaction: CPF Batch Runtime과 실행 추적 계약을 사용하는 Canonical 실행 예제입니다. */
@CpfService
public class BatchAuditRequiresNewJobService {
 @CpfTransactional(propagation=Propagation.REQUIRES_NEW) public BatchStepResult run(BatchStepCommand command){return BatchStepResult.completed("independent transaction committed",1,1,Map.of("executionId",command.cpfExecutionId()));}
}

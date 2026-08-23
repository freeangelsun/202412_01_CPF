package com.cpf.education.batch.steptransaction.service;

import com.cpf.foundation.annotation.CpfService;

import com.cpf.batch.spi.BatchStepHandler.BatchStepCommand;
import com.cpf.batch.spi.BatchStepHandler.BatchStepResult;
import com.cpf.batch.spi.BatchStepHandler.Status;
import java.util.Map;
import com.cpf.data.persistence.api.annotation.CpfTransactional; import org.springframework.transaction.annotation.Propagation;
/** 배치-13 Step별 Transaction 분리·부분완료 처리: CPF Batch Runtime과 실행 추적 계약을 사용하는 Canonical 실행 예제입니다. */
@CpfService
public class SettlementStepTransactionJobService {
 @CpfTransactional(propagation=Propagation.REQUIRES_NEW) public BatchStepResult stepA(BatchStepCommand command){return BatchStepResult.completed("step A committed",1,1,Map.of("stepA","COMPLETED"));}
 @CpfTransactional(propagation=Propagation.REQUIRES_NEW) public BatchStepResult stepB(BatchStepCommand command){boolean fail=Boolean.parseBoolean(String.valueOf(command.jobParameters().getOrDefault("failStepB","false")));return fail?new BatchStepResult(Status.FAILED,"STEP_B_FAILED","Step A 결과는 유지되고 B만 재시작 대상입니다.",1,0,0,Map.of("stepA","COMPLETED")):BatchStepResult.completed("step B committed",1,1,Map.of("stepB","COMPLETED"));}
}

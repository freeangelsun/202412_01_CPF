package com.cpf.education.batch.flatfile.service;

import com.cpf.foundation.annotation.CpfService;

import com.cpf.batch.spi.BatchStepHandler.BatchStepCommand;
import com.cpf.batch.spi.BatchStepHandler.BatchStepResult;
import java.util.Map;
import com.cpf.integration.fixedlength.api.CpfFixedLengthOperations;
import java.util.LinkedHashMap;
/** 배치-03 CSV·고정길이 File 배치: CPF Batch Runtime과 실행 추적 계약을 사용하는 Canonical 실행 예제입니다. */
@CpfService
public class SettlementFileJobService {
 private final CpfFixedLengthOperations fixed; public SettlementFileJobService(CpfFixedLengthOperations fixed){this.fixed=fixed;}
 public BatchStepResult run(BatchStepCommand command){var values=new LinkedHashMap<String,Object>();values.put("memberId",String.valueOf(command.jobParameters().getOrDefault("memberId","0000000001")));var out=fixed.write(values,"EDU-BATCH-FILE","1");var parsed=fixed.parse(out.message(),"EDU-BATCH-FILE","1");return BatchStepResult.completed("fixed length file validated",1,1,Map.of("fieldCount",parsed.values().size()));}
}

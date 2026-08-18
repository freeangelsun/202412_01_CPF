package com.cpf.education.online.ondemandbatch;
import com.cpf.batch.api.*;
import com.cpf.core.api.async.*;
import com.cpf.core.api.result.CpfResult;
import com.cpf.foundation.annotation.CpfService;
/** Async Operation이 실제 On-Demand Batch 접수를 수행하는 Handler입니다. */
@CpfService
public final class MemberExportAsyncHandler implements CpfAsyncHandler<MemberExportCommand,MemberExportResult> {
 private final CpfBatchOperations batches;
 public MemberExportAsyncHandler(CpfBatchOperations batches){this.batches=batches;}
 public String operationId(){return "MBR_MEMBER_EXPORT";}
 public Class<MemberExportCommand> commandType(){return MemberExportCommand.class;}
 public Class<MemberExportResult> resultType(){return MemberExportResult.class;}
 /** execute 동작은 비동기 executionId와 실제 Batch 실행 식별자를 분리하는 On-Demand Batch Golden Path에서 필요한 공개 동작을 수행합니다. */
 public CpfResult<MemberExportResult> execute(MemberExportCommand command,CpfAsyncExecution execution){
  if(execution.cancellationRequested()) throw new CpfAsyncCancelledException("회원 Export가 실행 전에 취소되었습니다.");
  CpfBatchExecutionRequest request=CpfBatchExecutionRequest.onDemand(
    "EDU_MEMBER_EXPORT_JOB","EDU_MEMBER_EXPORT_JOB",command.businessDate(),command.idempotencyKey(),
    String.valueOf(command.jobParameters()),command.requestUser(),"회원 Export On-Demand 실행");
  CpfBatchExecutionResult accepted=batches.launch(request);
  return CpfResult.success(new MemberExportResult(accepted.executionRequestId(),accepted.status()));
 }
}

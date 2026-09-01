package com.cpf.education.online.ondemandbatch.handler;

import com.cpf.education.online.common.base.EducationBaseService;

import com.cpf.batch.api.CpfBatchExecutionRequest;
import com.cpf.batch.api.CpfBatchExecutionResult;
import com.cpf.batch.api.CpfBatchOperations;
import com.cpf.core.api.async.CpfAsyncCancelledException;
import com.cpf.core.api.async.CpfAsyncExecution;
import com.cpf.core.api.async.CpfAsyncHandler;
import com.cpf.core.api.result.CpfResult;
import com.cpf.education.online.ondemandbatch.dto.MemberExportCommand;
import com.cpf.education.online.ondemandbatch.dto.MemberExportResult;
import com.cpf.foundation.annotation.CpfService;

/** Async Operation이 실제 On-Demand Batch 접수를 수행하는 Handler입니다. */
// CPF stereotype 이 붙은 Business Type 은 proxy-safe 여야 한다.
// CpfCapabilityUsageAspect.proxySafeBusinessType() 이 final Type 을 proxy-unsafe 로 판정하고,
// Advisor 가 매칭되면 CGLIB subclass 생성이 불가능해 Runtime 기동이 실패한다.
@CpfService
public class MemberExportAsyncHandler extends EducationBaseService implements CpfAsyncHandler<MemberExportCommand, MemberExportResult> {
    private final CpfBatchOperations batches;

    public MemberExportAsyncHandler(CpfBatchOperations batches) {
        this.batches = batches;
    }

    @Override
    public String operationId() {
        return "MBR_MEMBER_EXPORT";
    }

    @Override
    public Class<MemberExportCommand> commandType() {
        return MemberExportCommand.class;
    }

    @Override
    public Class<MemberExportResult> resultType() {
        return MemberExportResult.class;
    }

    @Override
    public CpfResult<MemberExportResult> execute(MemberExportCommand command, CpfAsyncExecution execution) {
        if (execution.cancellationRequested()) {
            throw new CpfAsyncCancelledException("회원 Export가 실행 전에 취소되었습니다.");
        }
        CpfBatchExecutionRequest request = CpfBatchExecutionRequest.onDemand(
                "EDU_MEMBER_EXPORT_JOB",
                "EDU_MEMBER_EXPORT_JOB",
                command.businessDate(),
                command.idempotencyKey(),
                String.valueOf(command.jobParameters()),
                command.requestUser(),
                "회원 Export On-Demand 실행");
        CpfBatchExecutionResult accepted = batches.launch(request);
        return CpfResult.success(new MemberExportResult(accepted.executionRequestId(), accepted.status()));
    }
}

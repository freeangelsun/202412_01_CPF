package com.cpf.education.online.ondemandbatch.service;

import com.cpf.education.online.common.base.EducationBaseService;

import com.cpf.core.api.async.CpfAsyncOperationStatus;
import com.cpf.core.api.async.CpfAsyncOperations;
import com.cpf.core.api.async.CpfAsyncSubmission;
import com.cpf.education.online.ondemandbatch.dto.MemberExportCommand;
import com.cpf.foundation.annotation.CpfService;
import java.time.Duration;

/** Controller가 실행 Framework를 직접 조립하지 않도록 Async Public API를 업무 서비스에서 사용합니다. */
// CPF stereotype 이 붙은 Business Type 은 proxy-safe 여야 한다.
// CpfCapabilityUsageAspect.proxySafeBusinessType() 이 final Type 을 proxy-unsafe 로 판정하고,
// Advisor 가 매칭되면 CGLIB subclass 생성이 불가능해 Runtime 기동이 실패한다.
@CpfService
public class MemberExportService extends EducationBaseService {
    private final CpfAsyncOperations async;

    public MemberExportService(CpfAsyncOperations async) {
        this.async = async;
    }

    /** On-Demand Batch 예제에서 submit 요청을 표준 호출 흐름으로 처리합니다. */
    public CpfAsyncSubmission submit(MemberExportCommand command) {
        return async.submit(command, command.idempotencyKey(), Duration.ofMinutes(30));
    }

    /** On-Demand Batch 예제에서 status 요청을 표준 호출 흐름으로 처리합니다. */
    public CpfAsyncOperationStatus status(String executionId) {
        return async.getStatus(executionId);
    }
}

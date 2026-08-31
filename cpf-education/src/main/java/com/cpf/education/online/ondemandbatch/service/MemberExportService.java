package com.cpf.education.online.ondemandbatch.service;

import com.cpf.education.online.common.base.EducationBaseService;

import com.cpf.core.api.async.CpfAsyncOperationStatus;
import com.cpf.core.api.async.CpfAsyncOperations;
import com.cpf.core.api.async.CpfAsyncSubmission;
import com.cpf.education.online.ondemandbatch.dto.MemberExportCommand;
import com.cpf.foundation.annotation.CpfService;
import java.time.Duration;

/** Controller가 실행 Framework를 직접 조립하지 않도록 Async Public API를 업무 서비스에서 사용합니다. */
@CpfService
public final class MemberExportService extends EducationBaseService {
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

package com.cpf.education.online.ondemandbatch.controller;

import com.cpf.core.api.async.CpfAsyncOperationStatus;
import com.cpf.core.api.async.CpfAsyncSubmission;
import com.cpf.education.online.ondemandbatch.dto.MemberExportCommand;
import com.cpf.education.online.ondemandbatch.service.MemberExportService;
import com.cpf.foundation.execution.api.CpfOnlineTransaction;
import com.cpf.security.api.annotation.CpfPermission;
import com.cpf.web.api.CpfRestController;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/** 온라인 On-Demand Batch: 업무 요청은 Async executionId로 접수하고 Batch 실행 ID와 분리합니다. */
@CpfRestController
@RequestMapping("/edu/online/member-export")
public final class MemberExportController {
    private final MemberExportService service;

    public MemberExportController(MemberExportService service) {
        this.service = service;
    }

    @PostMapping
    @CpfPermission("hasAuthority('BATCH_EXECUTE')")
    @Operation(operationId = "MBR_MEMBER_EXPORT", summary = "회원 Export 비동기 접수")
    @CpfOnlineTransaction(
            operationId = "MBR_MEMBER_EXPORT",
            name = "회원 Export",
            description = "Async executionId로 접수하고 실제 Batch executionRequestId는 Async 결과로 correlation한다.")
    /** On-Demand Batch 예제에서 launch 요청을 표준 호출 흐름으로 처리합니다. */
    public CpfAsyncSubmission launch(@RequestBody MemberExportCommand command) {
        return service.submit(command);
    }

    @GetMapping("/{executionId}")
    @CpfPermission("hasAuthority('BATCH_READ')")
    /** On-Demand Batch 예제에서 status 요청을 표준 호출 흐름으로 처리합니다. */
    public CpfAsyncOperationStatus status(@PathVariable String executionId) {
        return service.status(executionId);
    }
}

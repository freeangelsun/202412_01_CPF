package com.cpf.education.online.ondemandbatch;
import com.cpf.core.api.async.*;
import com.cpf.foundation.execution.api.CpfOnlineTransaction;
import com.cpf.security.api.annotation.CpfPreAuthorize;
import com.cpf.web.api.CpfRestController;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;
/** 온라인-12 On-Demand Batch: 업무 요청은 Async executionId로 접수/조회하고 Batch 실행 ID와 분리합니다. */
@CpfRestController @RequestMapping("/edu/online/member-export")
public final class MemberExportController {
 private final MemberExportService service;
 public MemberExportController(MemberExportService service){this.service=service;}
 @PostMapping @CpfPreAuthorize("hasAuthority('BATCH_EXECUTE')")
 @Operation(operationId="MBR_MEMBER_EXPORT",summary="회원 Export 비동기 접수")
 @CpfOnlineTransaction(operationId="MBR_MEMBER_EXPORT",name="회원 Export",description="Async executionId로 접수하고 실제 Batch executionRequestId는 Async 결과로 correlation한다.")
 /** launch 동작은 비동기 executionId와 실제 Batch 실행 식별자를 분리하는 On-Demand Batch Golden Path에서 필요한 공개 동작을 수행합니다. */
 public CpfAsyncSubmission launch(@RequestBody MemberExportCommand command){return service.submit(command);}
 @GetMapping("/{executionId}") @CpfPreAuthorize("hasAuthority('BATCH_READ')")
 /** status 동작은 비동기 executionId와 실제 Batch 실행 식별자를 분리하는 On-Demand Batch Golden Path에서 필요한 공개 동작을 수행합니다. */
 public CpfAsyncOperationStatus status(@PathVariable String executionId){return service.status(executionId);}
}

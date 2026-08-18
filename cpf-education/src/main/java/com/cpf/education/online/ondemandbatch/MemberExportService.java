package com.cpf.education.online.ondemandbatch;
import com.cpf.core.api.async.*;
import com.cpf.foundation.annotation.CpfService;
import java.time.Duration;
@CpfService
/** Controller가 실행 Framework를 직접 조립하지 않도록 Async Public API를 업무 서비스에서 사용합니다. */
public final class MemberExportService {
 private final CpfAsyncOperations async;
 public MemberExportService(CpfAsyncOperations async){this.async=async;}
 public CpfAsyncSubmission submit(MemberExportCommand command){return async.submit(command,command.idempotencyKey(),Duration.ofMinutes(30));}
 public CpfAsyncOperationStatus status(String executionId){return async.getStatus(executionId);}
}

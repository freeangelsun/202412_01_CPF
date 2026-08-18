package com.cpf.core.api.async;
import com.cpf.core.api.result.CpfResult;
import java.time.Duration;
/** 업무 개발자가 사용하는 범용 Async Operation Golden Path입니다. */
public interface CpfAsyncOperations {
    /** 현재 Context를 자동 capture하고 Handler에서 operationId를 확정해 executionId를 반환합니다. */
    <C> CpfAsyncSubmission submit(C command, String idempotencyKey, Duration timeout);
    CpfAsyncOperationStatus getStatus(String executionId);
    <R> CpfResult<R> getResult(String executionId, Class<R> resultType);
    /** cooperative cancel을 요청합니다. 이미 terminal인 실행은 상태를 그대로 반환합니다. */
    CpfAsyncOperationStatus cancel(String executionId, String reason);
}

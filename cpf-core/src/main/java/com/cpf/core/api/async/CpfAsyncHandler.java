package com.cpf.core.api.async;
import com.cpf.core.api.result.CpfResult;
/**
 * Command type별 durable Async handler 계약입니다. operationId는 Handler 정의에 한 번 선언하고 submit 호출마다 입력하지 않습니다.
 */
public interface CpfAsyncHandler<C, R> {
    String operationId();
    Class<C> commandType();
    Class<R> resultType();
    CpfResult<R> execute(C command, CpfAsyncExecution execution) throws Exception;
}

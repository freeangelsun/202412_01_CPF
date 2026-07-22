package com.cpf.external.execution.port;

import com.cpf.external.execution.domain.ExternalEndpointPolicy;
import com.cpf.external.execution.domain.ExternalExecution;

import java.util.Map;
import java.util.Optional;

/** EXS endpoint 정책과 호출 상태를 저장하는 업무 저장소 계약입니다. */
public interface ExternalExecutionRepository {

    Optional<ExternalEndpointPolicy> findEndpointPolicy(String institutionCode, String endpointCode);

    Optional<ExternalExecution> findByIdempotencyKey(String idempotencyKey);

    Optional<ExternalExecution> findByExecutionId(String executionId);

    void insert(ExternalExecution execution);

    void complete(String executionId, Map<String, Object> response);

    void fail(String executionId, String failureCode, String failureMessage);

    void markUnknown(String executionId, String unknownResultId, String failureCode, String failureMessage);

    void reconcile(String executionId, ExternalExecution.Status status, String operatorId, String reason);
}

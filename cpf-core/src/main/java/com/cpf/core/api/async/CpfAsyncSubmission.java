package com.cpf.core.api.async;
import java.time.Instant;
/** Async submit 결과. 실행건 조회 key는 operationId가 아니라 executionId입니다. */
public record CpfAsyncSubmission(String executionId, String operationId, CpfAsyncState state, Instant acceptedAt, boolean duplicate) { }

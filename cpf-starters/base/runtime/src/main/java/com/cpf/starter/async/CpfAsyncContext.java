package com.cpf.starter.async;
/** Async runtime이 자체 소유하는 추적 메타데이터입니다. Core Context component가 아닙니다. */
public record CpfAsyncContext(String submissionExecutionId,String forkExecutionId,CpfAsyncForkType forkType,String executorName,int attempt) { }

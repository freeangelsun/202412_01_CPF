package com.cpf.education.online.ondemandbatch;
/** 실제 Batch 접수 결과를 Async result로 보존합니다. */
public record MemberExportResult(String batchExecutionRequestId, String batchStatus) { }

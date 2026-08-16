package com.cpf.admin.opr.filejob;

import java.time.Instant;

/** ADM 대량파일 Job 운영 조회 DTO입니다. Payload·Rollback Token·비밀값은 노출하지 않습니다. */
public record AdmFileJobResponse(
        String jobId, String operationId, String requestHash, AdmFileJobType jobType, String templateCode,
        int templateVersion, String format, AdmFileJobState state, boolean dryRun,
        boolean rollbackSupported, long totalRows, long successRows, long failedRows,
        String sourceSha256, String resultSha256, String requestedBy, String reason,
        String approvalId, String appliedBy, String resolvedBy, String controlBy, String controlReason, Instant controlUpdatedAt,
        String errorCode, String errorMessage, Instant retentionUntil,
        Instant createdAt, Instant updatedAt) { }

package com.cpf.admin.opr.dto;

/**
 * ADM 배치 Job 등록 요청입니다.
 *
 * @param jobId CPF에서 관리하는 배치 Job ID
 * @param jobName 운영자가 화면에서 식별할 배치명
 * @param jobType TASKLET, CHUNK 같은 배치 유형
 * @param description 운영 설명
 * @param requestUser 요청 운영자 ID
 * @param reason 감사 로그에 남길 운영 사유
 */
public record AdmBatchJobRegisterRequest(
        String jobId,
        String jobName,
        String jobType,
        String description,
        String requestUser,
        String reason) {
}

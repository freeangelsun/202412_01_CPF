package com.cpf.admin.opr.dto;

/** 운영자 계정 상태 변경 요청입니다. */
public record AdmOperatorStatusUpdateRequest(
        String accountStatus,
        Long expectedVersion,
        String requestUser,
        String reason) {
}

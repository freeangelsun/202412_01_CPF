package com.cpf.admin.opr.dto;

import java.util.List;

/**
 * ADM 운영자 역할 변경 요청입니다.
 *
 * @param roleIds     부여할 역할 ID 목록
 * @param requestUser 요청자 ID
 * @param reason      감사 사유
 */
public record AdmOperatorRoleUpdateRequest(
        List<String> roleIds,
        String requestUser,
        String reason) {
}

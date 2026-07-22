package com.cpf.admin.opr.dto;

import java.util.List;

/**
 * ADM 운영자 응답입니다.
 *
 * @param operatorId             운영자 ID
 * @param operatorName           운영자명
 * @param roleIds                부여된 역할 ID 목록
 * @param locked                 계정 잠금 여부
 * @param passwordExpired        비밀번호 만료 여부
 * @param passwordChangeRequired 비밀번호 변경 필요 여부
 * @param createdAt              등록일시
 * @param updatedAt              수정일시
 */
public record AdmOperator(
        String operatorId,
        String operatorName,
        List<String> roleIds,
        boolean locked,
        boolean passwordExpired,
        boolean passwordChangeRequired,
        String createdAt,
        String updatedAt) {
}

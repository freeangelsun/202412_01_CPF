package com.cpf.admin.opr.dto;

import java.util.List;

/**
 * ADM 운영자 응답입니다.
 *
 * <p>기본 목록/상세 응답의 연락처는 마스킹 Projection이며, 원문은 별도 권한·사유가 필요한
 * Raw API에서만 {@code rawViewAllowed=true}로 반환합니다.</p>
 */
public record AdmOperator(
        String operatorId,
        String operatorName,
        String mobileNo,
        String officePhoneNo,
        String accountStatus,
        long versionNo,
        List<String> roleIds,
        boolean locked,
        boolean passwordExpired,
        boolean passwordChangeRequired,
        boolean rawViewAllowed,
        String createdAt,
        String updatedAt) {

    /** 기존 연락처 DTO Consumer 호환 생성자입니다. */
    public AdmOperator(
            String operatorId, String operatorName, String mobileNo, String officePhoneNo,
            List<String> roleIds, boolean locked, boolean passwordExpired,
            boolean passwordChangeRequired, String createdAt, String updatedAt) {
        this(operatorId, operatorName, mobileNo, officePhoneNo, "ACTIVE", 0L, roleIds,
                locked, passwordExpired, passwordChangeRequired, false, createdAt, updatedAt);
    }

    /** 연락처 필드 추가 전 Consumer의 생성자 계약을 유지합니다. */
    public AdmOperator(
            String operatorId, String operatorName, List<String> roleIds, boolean locked,
            boolean passwordExpired, boolean passwordChangeRequired, String createdAt, String updatedAt) {
        this(operatorId, operatorName, null, null, "ACTIVE", 0L, roleIds,
                locked, passwordExpired, passwordChangeRequired, false, createdAt, updatedAt);
    }
}

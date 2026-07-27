package com.cpf.admin.opr.dto;

import java.util.List;

/**
 * ADM 운영자 생성 요청입니다.
 *
 * <p>mobileNo/officePhoneNo는 인증 Identity가 아니라 운영자 Directory/Profile에 저장됩니다.</p>
 */
public record AdmOperatorCreateRequest(
        String operatorId,
        String operatorName,
        String mobileNo,
        String officePhoneNo,
        String password,
        List<String> roleIds,
        String requestUser,
        String reason) {

    /** 연락처 필드 추가 전 Consumer의 생성자 계약을 유지합니다. */
    public AdmOperatorCreateRequest(
            String operatorId,
            String operatorName,
            String password,
            List<String> roleIds,
            String requestUser,
            String reason) {
        this(operatorId, operatorName, null, null, password, roleIds, requestUser, reason);
    }
}

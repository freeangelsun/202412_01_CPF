package com.cpf.admin.opr.dto;

/** 운영자 Directory/Profile 연락처 수정 요청입니다. */
public record AdmOperatorContactUpdateRequest(
        String mobileNo,
        String officePhoneNo,
        boolean clearMobileNo,
        boolean clearOfficePhoneNo,
        Long expectedVersion,
        String requestUser,
        String reason) {
}

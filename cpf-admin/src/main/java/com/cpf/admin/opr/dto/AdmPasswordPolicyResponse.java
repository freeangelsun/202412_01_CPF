package com.cpf.admin.opr.dto;

/** ADM 운영자 비밀번호 정책의 Typed 공개 계약입니다. */
public record AdmPasswordPolicyResponse(
        int minLength,
        int requiredCategoryCount,
        int maxFailCount,
        int historyCount,
        int expireDays) {
}

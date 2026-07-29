package com.cpf.admin.opr.dto;

import java.util.List;

/** 비밀번호 정책 검증 결과입니다. 비밀번호 원문은 포함하지 않습니다. */
public record AdmPasswordValidationResponse(String operatorId, List<String> violations) {
    public AdmPasswordValidationResponse {
        violations = violations == null ? List.of() : List.copyOf(violations);
    }
    public boolean valid() { return violations.isEmpty(); }
}

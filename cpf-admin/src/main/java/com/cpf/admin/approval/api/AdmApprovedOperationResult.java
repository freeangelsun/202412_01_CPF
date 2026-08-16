package com.cpf.admin.approval.api;

/**
 * Owner Command 실행 결과입니다.
 *
 * @param status 실행 결과 상태
 * @param resultCode Owner 표준 결과 코드
 * @param maskedMessage 민감정보가 제거된 결과 메시지
 */
public record AdmApprovedOperationResult(
        AdmApprovalExecutionStatus status,
        String resultCode,
        String maskedMessage) {

    public AdmApprovedOperationResult {
        if (status == null) {
            throw new IllegalArgumentException("status는 필수입니다.");
        }
    }
}

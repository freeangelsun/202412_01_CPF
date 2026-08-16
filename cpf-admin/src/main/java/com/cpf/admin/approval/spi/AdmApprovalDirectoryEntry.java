package com.cpf.admin.approval.spi;

/**
 * ADM Approval 정책 Target을 실제 운영자로 해석한 결과 Snapshot 후보입니다.
 *
 * @param operatorId 실제 승인 운영자 ID
 * @param organizationCode 승인 시점 조직 코드
 * @param positionCode 승인 시점 직급 코드
 * @param jobTitleCode 승인 시점 직책 코드
 */
public record AdmApprovalDirectoryEntry(
        String operatorId,
        String organizationCode,
        String positionCode,
        String jobTitleCode) {

    public AdmApprovalDirectoryEntry {
        if (operatorId == null || operatorId.isBlank()) {
            throw new IllegalArgumentException("operatorId는 필수입니다.");
        }
    }
}

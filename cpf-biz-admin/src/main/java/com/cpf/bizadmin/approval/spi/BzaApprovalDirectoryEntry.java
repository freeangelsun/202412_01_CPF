package com.cpf.bizadmin.approval.spi;

/**
 * BZA Approval 정책 Target을 실제 직원으로 해석한 Snapshot 후보입니다.
 *
 * @param employeeNo 직원 번호
 * @param organizationCode 유효 조직 코드
 * @param positionCode 유효 직급 코드
 * @param jobTitleCode 유효 직책 코드
 */
public record BzaApprovalDirectoryEntry(
        String employeeNo,
        String organizationCode,
        String positionCode,
        String jobTitleCode) {

    public BzaApprovalDirectoryEntry {
        if (employeeNo == null || employeeNo.isBlank()) {
            throw new IllegalArgumentException("employeeNo는 필수입니다.");
        }
    }
}

package com.cpf.bizadmin.approval.spi;

import com.cpf.bizadmin.approval.api.BzaApprovalTargetType;

import java.time.Instant;
import java.util.List;

/**
 * BZA 업무결재 정책 Target을 실제 참여자 후보로 해석하는 SPI입니다.
 *
 * <p>기본 구현은 BZA 조직/직원/Assignment/Role 정본을 사용합니다. 정책은 동적 Target을
 * 보관하고 실제 Approval Instance는 이 Port의 결과를 Snapshot하여 이후 조직개편과 분리합니다.</p>
 */
public interface BzaApprovalDirectoryPort {

    /**
     * 기준 시점에 유효한 결재자/합의자 후보를 해석합니다.
     *
     * @param targetType EMPLOYEE/ROLE/ORGANIZATION/ORG_MANAGER/POSITION
     * @param targetCode 정책 Target 코드
     * @param effectiveAt 조직/Role/Assignment 유효성 기준 시각
     * @return 중복이 제거된 결정적 순서의 후보 목록
     */
    List<BzaApprovalDirectoryEntry> resolve(
            BzaApprovalTargetType targetType,
            String targetCode,
            Instant effectiveAt);
}

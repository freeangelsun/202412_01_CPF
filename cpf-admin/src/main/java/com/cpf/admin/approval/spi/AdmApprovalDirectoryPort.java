package com.cpf.admin.approval.spi;

import com.cpf.admin.approval.api.AdmApprovalTargetType;

import java.time.Instant;
import java.util.List;

/**
 * ADM 승인 정책의 동적 Target을 실제 운영자 후보로 해석하는 SPI입니다.
 *
 * <p>기본 구현은 {@code adm_operator_profile}/{@code adm_organization}을 사용할 수 있고,
 * 고객 구축에서는 LDAP/AD/IAM/HR Adapter로 교체할 수 있습니다. ADM은 고객 HR 원장을 직접
 * 소유하거나 다른 업무 DB를 직접 조회하지 않습니다.</p>
 */
public interface AdmApprovalDirectoryPort {

    /**
     * 지정 시점의 Target을 실제 승인자 후보로 해석합니다.
     *
     * @param targetType OPERATOR/ROLE/ORGANIZATION/ORG_MANAGER
     * @param targetCode 정책에 저장된 Target 코드
     * @param effectiveAt 정책/조직 유효성을 평가할 기준 시각
     * @return 중복이 제거된 결정적 순서의 후보 목록
     */
    List<AdmApprovalDirectoryEntry> resolve(
            AdmApprovalTargetType targetType,
            String targetCode,
            Instant effectiveAt);
}

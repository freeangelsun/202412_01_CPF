package com.cpf.admin.approval.api;

/**
 * ADM 위험조치 승인 정책에서 실제 승인자 후보를 해석하는 Target 유형입니다.
 *
 * <p>ADM은 고객 HR 원장을 소유하지 않습니다. 기본 DB Profile 또는 LDAP/AD/IAM/HR
 * Directory Adapter를 통해 조직 문맥을 해석하고 Approval Instance에는 결과 Snapshot을 남깁니다.</p>
 */
public enum AdmApprovalTargetType {
    /** 특정 운영자를 직접 지정합니다. */
    OPERATOR,
    /** 유효한 ADM Role 보유 운영자 집합을 지정합니다. */
    ROLE,
    /** 운영 조직 소속 운영자 집합을 지정합니다. */
    ORGANIZATION,
    /** 운영 조직 책임자를 지정합니다. */
    ORG_MANAGER
}

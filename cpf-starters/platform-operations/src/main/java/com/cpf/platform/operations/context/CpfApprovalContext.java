package com.cpf.platform.operations.context;

import java.time.Instant;

/** Platform-Ops가 소유하는 승인 처리 메타데이터입니다. Core Context 확장 component가 아닙니다. */
public record CpfApprovalContext(
        String approvalRequestId,
        String policyId,
        String requesterActorId,
        String approverActorId,
        String reasonCode,
        Instant requestedAt,
        Instant approvedAt,
        String approvalState,
        String targetActionId) { }

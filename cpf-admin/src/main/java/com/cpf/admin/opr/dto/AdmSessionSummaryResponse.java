package com.cpf.admin.opr.dto;

import java.time.LocalDateTime;
import java.util.List;

/** 원문 token과 token hash를 제외한 ADM 운영자 Session 조회 응답입니다. */
public record AdmSessionSummaryResponse(
        String sessionId,
        String operatorId,
        List<String> roleIds,
        LocalDateTime issuedAt,
        LocalDateTime expiresAt,
        boolean revoked,
        String clientIp,
        String userAgent,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
    public AdmSessionSummaryResponse { roleIds = roleIds == null ? List.of() : List.copyOf(roleIds); }
}

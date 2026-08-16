package com.cpf.admin.opr.dto;

/** 감사되는 거래 로그 상세 Export 요청입니다. */
public record AdmLogExportRequest(
        String logId,
        String action,
        String reason,
        String format,
        String requestedBy) {
}

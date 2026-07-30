package com.cpf.admin.opr.dto;

import java.time.LocalDateTime;

/** 감사되는 거래 로그 상세 Export 결과입니다. */
public record AdmLogExportResponse(
        String exportId,
        String status,
        String fileName,
        String downloadUrl,
        String maskedContent,
        LocalDateTime expiresAt,
        String watermark) {
}

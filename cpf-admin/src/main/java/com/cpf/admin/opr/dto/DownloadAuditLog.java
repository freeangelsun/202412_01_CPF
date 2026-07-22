package cpf.adm.opr.dto;

import java.time.LocalDateTime;

/**
 * 다운로드 감사 로그 조회 응답입니다.
 *
 * @param downloadId 다운로드 감사 로그 ID입니다.
 * @param adminId 요청 운영자 ID입니다.
 * @param downloadType 다운로드 유형입니다.
 * @param rowCount 다운로드 행 수입니다.
 * @param maskedYn 마스킹 적용 여부입니다.
 * @param includeSensitiveYn 민감정보 포함 요청 여부입니다.
 * @param reason 감사 사유입니다.
 * @param status 처리 상태입니다.
 * @param requestedAt 요청 일시입니다.
 * @param completedAt 완료 일시입니다.
 */
public record DownloadAuditLog(
        Long downloadId,
        String adminId,
        String downloadType,
        Integer rowCount,
        String maskedYn,
        String includeSensitiveYn,
        String reason,
        String status,
        LocalDateTime requestedAt,
        LocalDateTime completedAt
) {
}

package cpf.adm.opr.dto;

import java.time.LocalDateTime;

/**
 * ADM 운영 알림 발송 이력 응답입니다.
 *
 * @param deliveryId 알림 발송 로그 순번
 * @param ruleId 알림 규칙 순번
 * @param eventType 알림 이벤트 유형
 * @param targetType 알림 대상 유형
 * @param targetId 알림 대상 ID
 * @param receiver 수신자
 * @param deliveryStatus 발송 상태
 * @param deliveryMessage 발송 메시지
 * @param requestedAt 발송 요청 일시
 * @param deliveredAt 발송 완료 일시
 * @param createdAt 등록일시
 * @param updatedAt 수정일시
 */
public record AdmNotificationDeliveryLogResponse(
        long deliveryId,
        Long ruleId,
        String eventType,
        String targetType,
        String targetId,
        String receiver,
        String deliveryStatus,
        String deliveryMessage,
        LocalDateTime requestedAt,
        LocalDateTime deliveredAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}

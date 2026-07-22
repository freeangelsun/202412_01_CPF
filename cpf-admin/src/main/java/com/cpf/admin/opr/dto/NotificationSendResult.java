package cpf.adm.opr.dto;

import java.time.LocalDateTime;

/**
 * 운영 알림 발송 결과입니다.
 *
 * @param success 성공 여부
 * @param deliveryStatus 발송 상태
 * @param deliveryMessage 발송 결과 메시지
 * @param deliveredAt 발송 완료 일시
 */
public record NotificationSendResult(
        boolean success,
        String deliveryStatus,
        String deliveryMessage,
        LocalDateTime deliveredAt) {
}

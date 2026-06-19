package cpf.adm.opr.dto;

/**
 * ADM 운영 알림 테스트 발송 요청입니다.
 *
 * @param targetType 테스트 알림 대상 유형
 * @param targetId 테스트 알림 대상 ID
 * @param receiver 수신자
 * @param message 발송 메시지
 * @param reason 감사 사유
 * @param requestUser 요청 운영자
 */
public record AdmNotificationTestSendRequest(
        String targetType,
        String targetId,
        String receiver,
        String message,
        String reason,
        String requestUser) {
}

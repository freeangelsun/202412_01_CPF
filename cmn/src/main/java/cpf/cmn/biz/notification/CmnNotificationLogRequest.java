package cpf.cmn.biz.notification;

/**
 * CMN 공통 알림 로그 등록 요청입니다.
 *
 * @param notificationType 알림 유형
 * @param receiver         수신자
 * @param title            제목
 * @param message          메시지
 * @param requestUser      요청 사용자
 * @param transactionId    거래 ID
 * @param traceId          추적 ID
 */
public record CmnNotificationLogRequest(
        String notificationType,
        String receiver,
        String title,
        String message,
        String requestUser,
        String transactionId,
        String traceId) {
}

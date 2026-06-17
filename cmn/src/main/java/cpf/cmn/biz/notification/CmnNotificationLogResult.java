package cpf.cmn.biz.notification;

/**
 * CMN 공통 알림 로그 등록 결과입니다.
 *
 * @param notificationId 알림 로그 ID
 * @param sendStatus     발송 상태
 */
public record CmnNotificationLogResult(
        long notificationId,
        String sendStatus) {
}

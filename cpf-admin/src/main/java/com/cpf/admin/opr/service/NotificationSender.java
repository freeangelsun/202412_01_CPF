package cpf.adm.opr.service;

import cpf.adm.opr.dto.AdmNotificationRuleResponse;
import cpf.adm.opr.dto.NotificationSendResult;

/**
 * 운영 알림 발송 채널의 공통 인터페이스입니다.
 *
 * <p>초기 프레임워크는 mock sender로 발송 이력과 운영 흐름을 검증하고,
 * 실제 프로젝트에서는 Email, SMS, Mattermost, Kafka 같은 채널 sender를 이 인터페이스로 확장합니다.</p>
 */
public interface NotificationSender {

    NotificationSendResult send(
            AdmNotificationRuleResponse rule,
            String targetType,
            String targetId,
            String receiver,
            String message,
            String requestUser);
}

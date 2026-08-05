package com.cpf.admin.opr.service;

import com.cpf.admin.opr.dto.AdmNotificationRuleResponse;
import com.cpf.admin.opr.dto.NotificationSendResult;

/**
 * 운영 알림 발송 채널의 공통 인터페이스입니다.
 *
 * <p>실제 제품은 Email, SMS, Webhook, Message Provider 구현을 연결해야 합니다.
 * Provider가 없을 때는 fail-closed 구현이 실패를 반환하며, Simulator는 명시적 설정에서만 활성화됩니다.</p>
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

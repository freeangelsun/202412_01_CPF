package com.cpf.admin.opr.service;

import com.cpf.admin.opr.dto.AdmNotificationRuleResponse;
import com.cpf.admin.opr.dto.NotificationSendResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 외부 발송 채널 없이 운영 알림 흐름을 검증하기 위한 mock sender입니다.
 */
@Component
public class MockNotificationSender implements NotificationSender {

    @Override
    public NotificationSendResult send(
            AdmNotificationRuleResponse rule,
            String targetType,
            String targetId,
            String receiver,
            String message,
            String requestUser) {
        String resultMessage = "mock sender로 알림 발송을 시뮬레이션했습니다. "
                + "eventType=" + rule.eventType()
                + ", receiver=" + receiver
                + ", requestUser=" + requestUser;
        return new NotificationSendResult(true, "MOCK_SENT", resultMessage, LocalDateTime.now());
    }
}

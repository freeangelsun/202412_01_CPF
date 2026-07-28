package com.cpf.admin.opr.service;

import com.cpf.admin.opr.dto.AdmNotificationRuleResponse;
import com.cpf.admin.opr.dto.NotificationSendResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Locale;

/**
 * 외부 제품 없이 운영 알림 흐름과 실패 복구를 검증하는 Provider 중립 Simulator입니다.
 *
 * <p>이 구현의 성공은 실제 Email/SMS/Webhook 수신 성공이 아닙니다. 실제 Provider Bean이 등록되면
 * {@link ConditionalOnMissingBean}에 의해 자동 대체됩니다.</p>
 */
@Component
@ConditionalOnMissingBean(NotificationSender.class)
public class MockNotificationSender implements NotificationSender {
    private final String mode;

    public MockNotificationSender(
            @Value("${cpf.notification.simulator.mode:SUCCESS}") String mode) {
        this.mode = mode == null ? "SUCCESS" : mode.trim().toUpperCase(Locale.ROOT);
    }

    @Override
    public NotificationSendResult send(
            AdmNotificationRuleResponse rule,
            String targetType,
            String targetId,
            String receiver,
            String message,
            String requestUser) {
        String maskedReceiver = mask(receiver);
        return switch (mode) {
            case "PROVIDER_FAILURE" -> new NotificationSendResult(
                    false,
                    "SIMULATED_PROVIDER_FAILURE",
                    "Provider 실패를 시뮬레이션했습니다. receiver=" + maskedReceiver,
                    LocalDateTime.now());
            case "TIMEOUT" -> new NotificationSendResult(
                    false,
                    "SIMULATED_TIMEOUT",
                    "Provider timeout을 시뮬레이션했습니다. receiver=" + maskedReceiver,
                    null);
            case "UNKNOWN_RESULT" -> new NotificationSendResult(
                    false,
                    "SIMULATED_UNKNOWN_RESULT",
                    "응답 유실로 발송 결과를 확정할 수 없습니다. receiver=" + maskedReceiver,
                    null);
            default -> new NotificationSendResult(
                    true,
                    "SIMULATED_SENT",
                    "Simulator 발송 완료. eventType=" + rule.eventType()
                            + ", receiver=" + maskedReceiver
                            + ", requestUser=" + requestUser,
                    LocalDateTime.now());
        };
    }

    private String mask(String value) {
        if (value == null || value.isBlank()) {
            return "***";
        }
        String trimmed = value.trim();
        if (trimmed.length() <= 3) {
            return "***";
        }
        return trimmed.substring(0, 2) + "***" + trimmed.substring(trimmed.length() - 1);
    }
}

package com.cpf.admin.opr.service;

import com.cpf.admin.opr.dto.AdmNotificationRuleResponse;
import com.cpf.admin.opr.dto.NotificationSendResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Locale;

/**
 * 명시적으로 활성화된 개발·시험 환경에서만 사용하는 Provider 중립 Simulator입니다.
 *
 * <p>기본값은 비활성화이며 {@code cpf.notification.simulator.enabled=true}를 명시해야 등록됩니다.
 * 이 구현의 성공은 실제 Email/SMS/Webhook 수신 성공이 아닙니다. 실제 Provider Bean이 등록되면
 * {@link ConditionalOnMissingBean}에 의해 자동 대체됩니다.</p>
 */
// @ConditionalOnMissingBean 은 component scan 으로 등록되는 Bean 에서는 신뢰할 수 없다.
// UnavailableNotificationSender 와는 속성 값으로 이미 상호배타이므로 이 조건은 불필요하다.
@Component
@ConditionalOnProperty(prefix = "cpf.notification.simulator", name = "enabled", havingValue = "true")
public class MockNotificationSender implements NotificationSender {
    private final String mode;

    public MockNotificationSender(
            @Value("${cpf.notification.simulator.mode:PROVIDER_FAILURE}") String mode) {
        this.mode = mode == null ? "PROVIDER_FAILURE" : mode.trim().toUpperCase(Locale.ROOT);
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
            case "SUCCESS" -> new NotificationSendResult(
                    true,
                    "SIMULATED_SENT",
                    "Simulator 발송 완료. eventType=" + rule.eventType()
                            + ", receiver=" + maskedReceiver
                            + ", requestUser=" + requestUser,
                    LocalDateTime.now());
            default -> new NotificationSendResult(
                    false,
                    "SIMULATOR_MODE_UNSUPPORTED",
                    "지원하지 않는 Simulator mode입니다. receiver=" + maskedReceiver,
                    null);
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

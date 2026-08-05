package com.cpf.admin.opr.service;

import com.cpf.admin.opr.dto.AdmNotificationRuleResponse;
import com.cpf.admin.opr.dto.NotificationSendResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Fail-closed notification provider used when neither a real provider nor the explicit simulator is configured.
 *
 * <p>It never reports a successful delivery. Durable outbox retry/UNKNOWN handling therefore remains active
 * instead of recording a simulated success in an operational environment.</p>
 */
@Component
@ConditionalOnMissingBean(NotificationSender.class)
@ConditionalOnProperty(prefix = "cpf.notification.simulator", name = "enabled",
        havingValue = "false", matchIfMissing = true)
public class UnavailableNotificationSender implements NotificationSender {

    @Override
    public NotificationSendResult send(
            AdmNotificationRuleResponse rule,
            String targetType,
            String targetId,
            String receiver,
            String message,
            String requestUser) {
        return new NotificationSendResult(
                false,
                "PROVIDER_NOT_CONFIGURED",
                "Notification provider is not configured. receiver=" + mask(receiver),
                (LocalDateTime) null);
    }

    private static String mask(String value) {
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

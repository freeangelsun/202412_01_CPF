package com.cpf.admin.opr.service;

import com.cpf.admin.opr.dto.AdmNotificationRuleResponse;
import com.cpf.admin.opr.dto.NotificationSendResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NotificationSenderSafetyTest {

    @Test
    void unavailableProviderNeverReportsSuccessAndMasksReceiver() {
        NotificationSendResult result = new UnavailableNotificationSender().send(
                rule(), "ADM_TEST", "1", "operator@example.com", "message", "tester");

        assertThat(result.success()).isFalse();
        assertThat(result.deliveryStatus()).isEqualTo("PROVIDER_NOT_CONFIGURED");
        assertThat(result.deliveryMessage()).doesNotContain("operator@example.com");
        assertThat(result.deliveredAt()).isNull();
    }

    @Test
    void simulatorDefaultsToFailureEvenWhenExplicitlyConstructedWithoutMode() {
        NotificationSendResult result = new MockNotificationSender(null).send(
                rule(), "ADM_TEST", "1", "operator@example.com", "message", "tester");

        assertThat(result.success()).isFalse();
        assertThat(result.deliveryStatus()).isEqualTo("SIMULATED_PROVIDER_FAILURE");
        assertThat(result.deliveryMessage()).doesNotContain("operator@example.com");
    }

    private static AdmNotificationRuleResponse rule() {
        AdmNotificationRuleResponse rule = mock(AdmNotificationRuleResponse.class);
        when(rule.eventType()).thenReturn("TEST");
        return rule;
    }
}

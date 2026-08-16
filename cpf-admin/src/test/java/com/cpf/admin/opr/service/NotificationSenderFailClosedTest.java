package com.cpf.admin.opr.service;

import com.cpf.admin.opr.dto.AdmNotificationRuleResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationSenderFailClosedTest {

    private static final AdmNotificationRuleResponse RULE = new AdmNotificationRuleResponse(
            1L, "TEST_EVENT", "DEFAULT", "EMAIL", "TEST_TEMPLATE", "WARN",
            "OPERATORS", "Y", "tester", null, "tester", null);

    @Test
    void providerAbsenceNeverReportsSuccessAndMasksReceiver() {
        var result = new UnavailableNotificationSender().send(
                RULE, "ORDER", "1", "operator@example.com", "message", "tester");

        assertThat(result.success()).isFalse();
        assertThat(result.deliveryStatus()).isEqualTo("PROVIDER_NOT_CONFIGURED");
        assertThat(result.deliveryMessage()).doesNotContain("operator@example.com");
    }

    @Test
    void simulatorDefaultsToFailureAndUnknownModeFailsClosed() {
        var defaultResult = new MockNotificationSender(null).send(
                RULE, "ORDER", "1", "operator@example.com", "message", "tester");
        var unsupported = new MockNotificationSender("typo").send(
                RULE, "ORDER", "1", "operator@example.com", "message", "tester");

        assertThat(defaultResult.success()).isFalse();
        assertThat(defaultResult.deliveryStatus()).isEqualTo("SIMULATED_PROVIDER_FAILURE");
        assertThat(unsupported.success()).isFalse();
        assertThat(unsupported.deliveryStatus()).isEqualTo("SIMULATOR_MODE_UNSUPPORTED");
    }

    @Test
    void simulatorSuccessRequiresExplicitMode() {
        var result = new MockNotificationSender("SUCCESS").send(
                RULE, "ORDER", "1", "operator@example.com", "message", "tester");

        assertThat(result.success()).isTrue();
        assertThat(result.deliveryStatus()).isEqualTo("SIMULATED_SENT");
        assertThat(result.deliveryMessage()).doesNotContain("operator@example.com");
    }
}

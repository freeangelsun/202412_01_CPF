package com.cpf.admin.opr.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.cpf.admin.opr.dto.AdmNotificationRuleResponse;
import com.cpf.admin.opr.dto.AdmNotificationTestSendRequest;
import com.cpf.admin.opr.dto.NotificationSendResult;
import com.cpf.core.api.error.CpfValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;

class AdmNotificationOutboxServiceTest {

    @Test
    void rejectsSecretPayloadBeforePersisting() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        NotificationSender sender = mock(NotificationSender.class);
        AdmNotificationOutboxService service = new AdmNotificationOutboxService(
                jdbc, sender, mock(PlatformTransactionManager.class), 60, 30, 3);
        AdmNotificationRuleResponse rule = new AdmNotificationRuleResponse(
                1L, "ERROR", null, "ADM", null, "ERROR", "OPS", "Y",
                "ADM", LocalDateTime.now(), "ADM", LocalDateTime.now());

        assertThatThrownBy(() -> service.enqueueTest(
                rule,
                new AdmNotificationTestSendRequest("INSTANCE", "I1", "ops@example.com",
                        "Authorization: Bearer secret-token", "test", "ADM"),
                "ADM"))
                .isInstanceOf(CpfValidationException.class);
    }

    @Test
    void simulatorKeepsProviderResultDistinctFromRealDelivery() {
        MockNotificationSender sender = new MockNotificationSender("SUCCESS");
        AdmNotificationRuleResponse rule = new AdmNotificationRuleResponse(
                1L, "ERROR", null, "ADM", null, "ERROR", "OPS", "Y",
                "ADM", LocalDateTime.now(), "ADM", LocalDateTime.now());
        NotificationSendResult result = sender.send(rule, "INSTANCE", "I1", "ops@example.com", "test", "ADM");
        assertThat(result.deliveryStatus()).isEqualTo("SIMULATED_SENT");
    }
}

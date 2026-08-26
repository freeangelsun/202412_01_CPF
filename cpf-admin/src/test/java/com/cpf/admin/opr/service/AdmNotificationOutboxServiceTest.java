package com.cpf.admin.opr.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cpf.admin.opr.dto.AdmNotificationRuleResponse;
import com.cpf.admin.opr.dto.AdmNotificationTestSendRequest;
import com.cpf.admin.opr.dto.NotificationSendResult;
import com.cpf.core.api.error.CpfValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.mockito.InOrder;

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
    void expiredProcessingLeaseIsQuarantinedAsUnknownResultInsteadOfAutomaticRetry() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        NotificationSender sender = mock(NotificationSender.class);
        AdmNotificationOutboxService service = new AdmNotificationOutboxService(
                jdbc, sender, mock(PlatformTransactionManager.class), 60, 30, 3);
        when(jdbc.update(
                any(String.class),
                eq("worker-1"),
                any(java.sql.Timestamp.class),
                any(java.sql.Timestamp.class))).thenReturn(1);
        when(jdbc.update(
                any(String.class),
                any(java.sql.Timestamp.class),
                eq("worker-1"))).thenReturn(1);

        int recovered = service.recoverExpiredProcessing("worker-1");

        assertThat(recovered).isEqualTo(1);
        InOrder ordered = inOrder(jdbc);
        ordered.verify(jdbc).update(
                org.mockito.ArgumentMatchers.argThat(sql ->
                        sql.contains("delivery_status = 'UNKNOWN_RESULT'")
                                && sql.contains("delivery_status = 'PROCESSING'")
                                && sql.contains("LEASE_EXPIRED_UNKNOWN_RESULT")
                                && !sql.contains("delivery_status = 'RETRY'")),
                eq("worker-1"),
                any(java.sql.Timestamp.class),
                any(java.sql.Timestamp.class));
        ordered.verify(jdbc).update(
                org.mockito.ArgumentMatchers.argThat(sql ->
                        sql.contains("cpf_notification_delivery_attempt")
                                && sql.contains("attempt_status = 'UNKNOWN_RESULT'")
                                && sql.contains("LEASE_EXPIRED_UNKNOWN_RESULT")),
                any(java.sql.Timestamp.class),
                eq("worker-1"));
    }

    @Test
    void providerMessagesAreRedactedBeforePersistence() {
        AdmNotificationOutboxService service = new AdmNotificationOutboxService(
                mock(JdbcTemplate.class), mock(NotificationSender.class),
                mock(PlatformTransactionManager.class), 60, 30, 3);

        String sanitized = service.sanitizeProviderMessage(
                "Authorization: Bearer provider-secret access_token=abc123");

        assertThat(sanitized).doesNotContain("provider-secret", "abc123");
        assertThat(sanitized).contains("[REDACTED]");
    }


    @Test
    void retryAndCancelRequireNonNegativeExpectedVersion() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        NotificationSender sender = mock(NotificationSender.class);
        AdmNotificationOutboxService service = new AdmNotificationOutboxService(
                jdbc, sender, mock(PlatformTransactionManager.class), 60, 30, 3);

        assertThatThrownBy(() -> service.retry(10L, -1L, "ADM"))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("expectedVersion");
        assertThatThrownBy(() -> service.cancel(10L, -1L, "ADM"))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("expectedVersion");
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

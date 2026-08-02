package com.cpf.starter.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

class JdbcCpfNotificationOutboxTest {
    private static final Instant NOW = Instant.parse("2026-08-02T00:00:00Z");

    @Test
    void duplicateIdempotencyReturnsExistingOnlyForSameRequest() {
        FakeJdbcTemplate jdbc = new FakeJdbcTemplate();
        jdbc.duplicateNotificationInsert = true;
        jdbc.rows = List.of(existingNotification("WELCOME", encoded(Map.of("a", "1", "b", "2"))));
        JdbcCpfNotificationOutbox outbox = new JdbcCpfNotificationOutbox(jdbc);
        CpfNotificationRequest request = request("WELCOME", Map.of("b", "2", "a", "1"));

        CpfNotificationResult result = outbox.enqueue(request);

        assertEquals("n-1", result.notificationId());
        assertEquals("PENDING", result.status());
    }

    @Test
    void duplicateIdempotencyRejectsDifferentRequest() {
        FakeJdbcTemplate jdbc = new FakeJdbcTemplate();
        jdbc.duplicateNotificationInsert = true;
        jdbc.rows = List.of(existingNotification("OTHER", encoded(Map.of("a", "1"))));
        JdbcCpfNotificationOutbox outbox = new JdbcCpfNotificationOutbox(jdbc);

        assertThrows(IllegalStateException.class,
                () -> outbox.enqueue(request("WELCOME", Map.of("a", "1"))));
    }

    @Test
    void reconcileClaimRecoversExpiredLeaseAfterProcessKill() {
        FakeJdbcTemplate jdbc = new FakeJdbcTemplate();
        jdbc.rows = List.of(notificationRow());
        jdbc.updateResult = 1;
        JdbcCpfNotificationOutbox outbox = new JdbcCpfNotificationOutbox(jdbc);

        List<CpfNotificationRequest> claimed =
                outbox.claimUnknownForReconcile("worker-1", 10, NOW, Duration.ofSeconds(30));

        assertEquals(1, claimed.size());
        assertTrue(jdbc.lastQuery.contains("notification_status='RECONCILING'"));
        assertTrue(jdbc.lastUpdate.contains("lease_until<CURRENT_TIMESTAMP"));
    }

    @Test
    void unknownResultHasBoundedDlqTransition() {
        FakeJdbcTemplate jdbc = new FakeJdbcTemplate();
        jdbc.updateResult = 1;
        JdbcCpfNotificationOutbox outbox = new JdbcCpfNotificationOutbox(jdbc);

        outbox.markUnknown(
                CpfNotificationResult.unknown("n-1", "EMAIL", "unknown"),
                NOW.plusSeconds(30));

        assertTrue(jdbc.lastUpdate.contains("attempt_count+1>=max_attempts"));
        assertTrue(jdbc.lastUpdate.contains("THEN 'DLQ'"));
    }

    @Test
    void duplicateReceiptIsIdempotentOnlyForSameReceipt() {
        FakeJdbcTemplate jdbc = new FakeJdbcTemplate();
        jdbc.duplicateReceiptInsert = true;
        jdbc.rows = List.of(receiptRow("DELIVERED"));
        JdbcCpfNotificationOutbox outbox = new JdbcCpfNotificationOutbox(jdbc);
        CpfNotificationReceipt receipt = new CpfNotificationReceipt(
                "r-1", "n-1", "EMAIL", "DELIVERED", "ok", NOW);

        outbox.recordReceipt(receipt, "operator");

        jdbc.rows = List.of(receiptRow("REJECTED"));
        assertThrows(IllegalStateException.class,
                () -> outbox.recordReceipt(receipt, "operator"));
    }

    private static CpfNotificationRequest request(String template, Map<String, String> variables) {
        return new CpfNotificationRequest(
                "n-1", "EMAIL", "user@example.invalid", template, variables,
                "idem-1", "transaction-1", null);
    }

    private static Map<String, Object> existingNotification(String template, String variables) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("notificationId", "n-1");
        row.put("status", "PENDING");
        row.put("channelCode", "EMAIL");
        row.put("recipientValue", "user@example.invalid");
        row.put("templateId", template);
        row.put("variableJson", variables);
        row.put("transactionId", "transaction-1");
        row.put("providerName", null);
        row.put("providerMessageId", null);
        row.put("detail", null);
        row.put("updatedAt", Timestamp.from(NOW));
        return row;
    }

    private static Map<String, Object> notificationRow() {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("notification_id", "n-1");
        row.put("channel_code", "EMAIL");
        row.put("recipient_value", "user@example.invalid");
        row.put("template_id", "WELCOME");
        row.put("variable_json", "");
        row.put("idempotency_key", "idem-1");
        row.put("transaction_id", "transaction-1");
        row.put("next_attempt_at", Timestamp.from(NOW));
        return row;
    }

    private static Map<String, Object> receiptRow(String status) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("notification_id", "n-1");
        row.put("provider_name", "EMAIL");
        row.put("receipt_status", status);
        row.put("receipt_detail", "ok");
        row.put("received_at", Timestamp.from(NOW));
        return row;
    }

    private static String encoded(Map<String, String> values) {
        return values.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> base64(entry.getKey()) + "=" + base64(entry.getValue()))
                .reduce((left, right) -> left + "&" + right)
                .orElse("");
    }

    private static String base64(String value) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static final class FakeJdbcTemplate extends JdbcTemplate {
        private boolean duplicateNotificationInsert;
        private boolean duplicateReceiptInsert;
        private int updateResult;
        private List<Map<String, Object>> rows = List.of();
        private String lastQuery = "";
        private String lastUpdate = "";

        @Override
        public int update(String sql, Object... args) {
            lastUpdate = sql;
            if (duplicateNotificationInsert && sql.contains("INSERT INTO cpf_notification_outbox")) {
                throw new DuplicateKeyException("duplicate notification");
            }
            if (duplicateReceiptInsert && sql.contains("INSERT INTO cpf_notification_receipt")) {
                throw new DuplicateKeyException("duplicate receipt");
            }
            return updateResult;
        }

        @Override
        public List<Map<String, Object>> queryForList(String sql, Object... args) {
            lastQuery = sql;
            return rows;
        }
    }
}

package com.cpf.core.api.logging.policy;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CpfLogCaptureGuardTest {
    @Test
    void sensitiveValuesAreMaskedAndForbiddenHeadersAreDropped() {
        LogPolicyDecision policy = policy(LogCaptureMode.MASKED, LogCaptureMode.MASKED,
                LogCaptureMode.MASKED_BODY, 1024);

        CpfLogCaptureGuard.CapturedValue query = CpfLogCaptureGuard.query(
                "memberId=100&password=plain&token=abc", policy);
        assertTrue(query.value().contains("memberId=100"));
        assertFalse(query.value().contains("plain"));
        assertFalse(query.value().contains("token=abc"));

        CpfLogCaptureGuard.CapturedValue headers = CpfLogCaptureGuard.headers(Map.of(
                "Authorization", List.of("Bearer very-secret-token"),
                "Content-Type", List.of("application/json")), false, policy);
        assertFalse(headers.value().toLowerCase().contains("authorization"));
        assertTrue(headers.value().contains("content-type:application/json"));

        CpfLogCaptureGuard.CapturedValue body = CpfLogCaptureGuard.body(
                "{\"accountNo\":\"1234567890\",\"name\":\"cpf\"}", false, policy, null);
        assertFalse(body.value().contains("1234567890"));
        assertTrue(body.value().contains("***"));
    }

    @Test
    void encryptedBodyFailsClosedWithoutProtectionPort() {
        LogPolicyDecision policy = policy(LogCaptureMode.NONE, LogCaptureMode.NONE,
                LogCaptureMode.ENCRYPTED_BODY, 1024);
        assertThrows(IllegalStateException.class,
                () -> CpfLogCaptureGuard.body("secret", false, policy, null));
    }

    @Test
    void utf8TruncationDoesNotReturnBrokenContinuationBytes() {
        CpfLogCaptureGuard.CapturedValue value = CpfLogCaptureGuard.truncate("가나다라마바사", 5);
        assertTrue(value.truncated());
        assertEquals("가", value.value());
    }

    private static LogPolicyDecision policy(
            LogCaptureMode query,
            LogCaptureMode header,
            LogCaptureMode requestBody,
            int maxBytes) {
        return new LogPolicyDecision(
                LogPolicyDecision.CURRENT_SCHEMA_VERSION,
                "ROUTE", "R1", "INFO", true, "INFO",
                query, header, header,
                requestBody, LogCaptureMode.NONE, LogCaptureMode.SUMMARY,
                List.of("memberid", "password", "token"),
                List.of("content-type"), List.of("accountno", "name"),
                maxBytes, maxBytes, maxBytes, maxBytes, maxBytes,
                "DEFAULT", null, "TEST", null, null);
    }
}

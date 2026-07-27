package com.cpf.core.api.security;

import com.cpf.core.api.error.CpfValidationException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CpfSensitiveDataTest {
    @Test
    void phoneAndEmailAreNormalizedWithoutLosingStringSemantics() {
        assertEquals("+82-10-1234-5678 x123", CpfSensitiveData.normalizePhone(" +82-10-1234-5678 x123 ", "mobileNo"));
        assertEquals("User@example.com", CpfSensitiveData.normalizeEmail(" User@EXAMPLE.COM ", "email"));
        assertNull(CpfSensitiveData.normalizePhone("   ", "mobileNo"));
        assertNull(CpfSensitiveData.normalizeEmail(null, "email"));
    }

    @Test
    void defaultProjectionDoesNotExposeRawValue() {
        assertEquals("***-****-5678", CpfSensitiveData.maskPhone("+82-10-1234-5678"));
        assertEquals("u***@example.com", CpfSensitiveData.maskEmail("user@example.com"));
    }

    @Test
    void invalidOrControlCharacterInputFailsWithoutEchoingRawValue() {
        CpfValidationException phone = assertThrows(CpfValidationException.class,
                () -> CpfSensitiveData.normalizePhone("010-1234-5678\nsecret", "mobileNo"));
        assertFalse(phone.getMessage().contains("secret"));
        assertThrows(CpfValidationException.class,
                () -> CpfSensitiveData.normalizeEmail("not-an-email", "email"));
    }

    @Test
    void auditReasonRemovesSecretsAndPii() {
        String result = CpfSensitiveData.sanitizeAuditReason(
                "password=Secret! bearer abc 010-1234-5678 user@example.com 900101-1234567");
        assertFalse(result.contains("Secret!"));
        assertFalse(result.contains("010-1234-5678"));
        assertFalse(result.contains("user@example.com"));
        assertFalse(result.contains("900101-1234567"));
    }

    @Test
    void auditSnapshotRecursivelyRedactsFieldAwareSecretsAndPiiWithoutMutatingSource() {
        Map<String,Object> source = Map.of(
                "loginId", "operator01",
                "password", "Secret!",
                "profile", Map.of(
                        "email", "user@example.com",
                        "mobileNo", "010-1234-5678",
                        "address", "Seoul",
                        "notes", "Bearer abc.def"),
                "items", List.of(Map.of("refreshToken", "token-raw")));

        @SuppressWarnings("unchecked")
        Map<String,Object> result = (Map<String,Object>) CpfSensitiveData.sanitizeAuditSnapshot(source);
        assertEquals("[REDACTED]", result.get("password"));
        @SuppressWarnings("unchecked")
        Map<String,Object> profile = (Map<String,Object>) result.get("profile");
        assertEquals("u***@example.com", profile.get("email"));
        assertEquals("***-****-5678", profile.get("mobileNo"));
        assertEquals("[MASKED]", profile.get("address"));
        assertFalse(String.valueOf(profile.get("notes")).contains("abc.def"));
        assertEquals("Secret!", source.get("password"));
    }

    @Test
    void serializedAuditTextRemovesFieldValuesAndFreeTextPii() {
        String result = CpfSensitiveData.sanitizeAuditText(
                "{\"mobileNo\":\"010-1234-5678\",\"email\":\"user@example.com\",\"token\":\"abc\"}");
        assertFalse(result.contains("010-1234-5678"));
        assertFalse(result.contains("user@example.com"));
        assertFalse(result.contains("\"abc\""));
    }
}

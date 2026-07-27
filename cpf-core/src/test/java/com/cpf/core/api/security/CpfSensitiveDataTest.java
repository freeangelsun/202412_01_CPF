package com.cpf.core.api.security;

import com.cpf.core.api.error.CpfValidationException;
import org.junit.jupiter.api.Test;

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
}

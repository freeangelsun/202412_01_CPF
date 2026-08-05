package com.cpf.core.common.logging;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SensitiveDataMaskerBoundaryTest {
    @Test
    void masksEscapedJsonQuotedValuesAndAuthorizationSchemes() {
        String input = "{\"password\":\"a\\\"b\",\"nested\":\"{\\\"token\\\":\\\"inside\\\"}\"} "
                + "authorization=Basic dXNlcjpwYXNz token='space containing token'";
        String masked = SensitiveDataMasker.mask(input);
        assertFalse(masked.contains("a\\\"b"));
        assertFalse(masked.contains("inside"));
        assertFalse(masked.contains("dXNlcjpwYXNz"));
        assertFalse(masked.contains("space containing token"));
        assertTrue(masked.contains("***"));
    }
}

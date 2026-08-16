package com.cpf.core.api.error;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CpfErrorCodeTest {
    @Test
    void coreErrorHasNoTransportStatus() {
        assertEquals(
                CpfErrorDefinition.Category.VALIDATION,
                CpfErrorCode.INVALID_PARAMETER.category());
        assertFalse(CpfErrorCode.INVALID_PARAMETER.retryable());
        assertTrue(CpfErrorCode.EXTERNAL_SERVICE_ERROR.retryable());
    }
}

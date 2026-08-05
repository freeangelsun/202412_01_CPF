package com.cpf.core.common.runtimecontrol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class CpfRuntimeHttpControlPlaneClientTest {
    @Test
    void conflictStatusIsTranslatedToRuntimeFencingSignal() {
        assertTrue(CpfRuntimeHttpControlPlaneClient.isFencingStatus(HttpStatus.CONFLICT));
        assertFalse(CpfRuntimeHttpControlPlaneClient.isFencingStatus(HttpStatus.UNAUTHORIZED));
        assertFalse(CpfRuntimeHttpControlPlaneClient.isFencingStatus(HttpStatus.SERVICE_UNAVAILABLE));
        assertTrue(CpfRuntimeHttpControlPlaneClient.isRateLimitStatus(HttpStatus.TOO_MANY_REQUESTS));
        assertFalse(CpfRuntimeHttpControlPlaneClient.isRateLimitStatus(HttpStatus.CONFLICT));
    }

    @Test
    void agentTokenIsTrimmedAndHeaderInjectionIsRejected() {
        assertEquals("token-value", CpfRuntimeHttpControlPlaneClient.normalizeAgentToken("  token-value  "));
        assertThrows(IllegalArgumentException.class,
                () -> CpfRuntimeHttpControlPlaneClient.normalizeAgentToken("token\r\nInjected: value"));
        assertThrows(IllegalArgumentException.class,
                () -> CpfRuntimeHttpControlPlaneClient.normalizeAgentToken("x".repeat(2049)));
    }
}

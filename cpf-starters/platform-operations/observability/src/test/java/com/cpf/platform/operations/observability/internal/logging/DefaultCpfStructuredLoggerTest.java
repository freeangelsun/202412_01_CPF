package com.cpf.platform.operations.observability.internal.logging;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultCpfStructuredLoggerTest {
    private final DefaultCpfStructuredLogger logger = new DefaultCpfStructuredLogger();

    @Test void supportsAllApplicationLogCategoriesWithoutRequiringAContext() {
        assertDoesNotThrow(() -> logger.business("MEMBER_STATUS_CHANGED", Map.of("memberNo", "M123456")));
        assertDoesNotThrow(() -> logger.operation("CACHE_REFRESH", Map.of("count", 3)));
        assertDoesNotThrow(() -> logger.security("AUTHZ_DENIED", Map.of("permission", "ADM.WRITE")));
        assertDoesNotThrow(() -> logger.error("REMOTE_CALL_FAILED", new IllegalStateException("token=secret"), Map.of()));
    }

    @Test void rejectsBlankEventName() {
        assertThrows(IllegalArgumentException.class, () -> logger.business(" ", Map.of()));
    }
}

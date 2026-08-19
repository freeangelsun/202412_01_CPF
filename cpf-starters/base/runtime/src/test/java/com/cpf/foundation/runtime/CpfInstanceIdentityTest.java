package com.cpf.foundation.runtime;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class CpfInstanceIdentityTest {
    @Test void explicitIdentityWins() {
        assertEquals("MBR01", CpfInstanceIdentity.resolveInstanceId("MBR01", "host-a"));
    }

    @Test void hostnameIsFallbackWhenExplicitIsAbsent() {
        assertEquals("host-a", CpfInstanceIdentity.resolveInstanceId(null, "host-a"));
    }

    @Test void forbiddenFallbacksFailClosed() {
        for (String forbidden : new String[]{"localhost", "127.0.0.1", "::1", "unknown", "local"}) {
            assertThrows(IllegalStateException.class, () -> CpfInstanceIdentity.resolveInstanceId(null, forbidden));
        }
    }

    @Test void forbiddenExplicitIdentityAlsoFailsClosed() {
        assertThrows(IllegalStateException.class, () -> CpfInstanceIdentity.resolveInstanceId("localhost", "host-a"));
    }
}

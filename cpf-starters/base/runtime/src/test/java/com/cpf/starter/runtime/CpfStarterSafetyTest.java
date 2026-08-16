package com.cpf.starter.runtime;

import static org.junit.jupiter.api.Assertions.*;
import com.cpf.foundation.annotation.CpfLogMode;
import com.cpf.foundation.annotation.CpfLogging;
import org.junit.jupiter.api.Test;

class CpfStarterSafetyTest {
    @Test void loggingAnnotationDefaultsKeepPayloadOff() throws Exception {
        var method = Fixture.class.getDeclaredMethod("sample", String.class);
        var logging = method.getAnnotation(CpfLogging.class);
        assertEquals(CpfLogMode.SUMMARY, logging.mode());
        assertFalse(logging.includeArguments());
        assertFalse(logging.includeResult());
        assertEquals(0, logging.allowlist().length);
        assertEquals(0, logging.resultAllowlist().length);
    }
    static final class Fixture {
        @CpfLogging String sample(String secret) { return secret; }
    }
}

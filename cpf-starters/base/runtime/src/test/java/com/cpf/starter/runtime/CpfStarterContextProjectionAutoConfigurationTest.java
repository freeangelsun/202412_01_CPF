package com.cpf.starter.runtime;

import static org.junit.jupiter.api.Assertions.assertSame;

import com.cpf.foundation.context.CpfContextProjectionRegistry;
import org.junit.jupiter.api.Test;

class CpfStarterContextProjectionAutoConfigurationTest {
    @Test
    void publishesTheServiceLoaderRuntimeRegistry() {
        assertSame(CpfContextProjectionRegistry.runtimeRegistry(),
                new CpfStarterAutoConfiguration().cpfContextProjectionRegistry());
    }
}

package com.cpf.starter.platform.operations.observability;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.cpf.foundation.context.CpfContextProjectionRegistry;
import org.junit.jupiter.api.Test;

class CpfObservabilityContextProjectionLifecycleTest {
    @Test
    void unregistersFromRuntimeRegistryWhenSpringDestroysTheBean() throws Exception {
        CpfContextProjectionRegistry registry = new CpfContextProjectionRegistry();
        CpfObservabilityContextProjection projection = new CpfObservabilityContextProjection(
                new CpfMdcContextProjection(), new CpfTraceContextProjection(), registry);

        assertEquals(1, registry.size());
        projection.close();
        assertEquals(0, registry.size());
    }
}

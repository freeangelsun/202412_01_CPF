package com.cpf.testkit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.cpf.platform.operations.api.health.CpfDependencyHealth;
import com.cpf.platform.operations.api.health.CpfHealthStatus;
import java.time.Instant;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class CpfHealthProbeStubTest {

    @Test
    void exposesDeterministicDependencyHealthThroughSupplierAndProbeContracts() {
        CpfHealthProbeStub stub = new CpfHealthProbeStub("database");
        Supplier<CpfDependencyHealth> supplier = stub;

        assertSame(CpfHealthStatus.UP, stub.probe().status());
        assertEquals("database", stub.get().name());
        assertEquals("test", stub.get().endpointRef());
        assertEquals("testkit", stub.get().reasonCode());
        assertEquals(Instant.EPOCH, stub.get().checkedAt());
        assertSame(CpfHealthStatus.UP, supplier.get().status());

        stub.status(CpfHealthStatus.DOWN);
        assertSame(CpfHealthStatus.DOWN, stub.probe().status());
    }
}

package com.cpf.starter.tcp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfTcpOperationsDurableAuditTest {
    @TempDir Path tempDir;

    @Test
    void reportsDurabilityAndUsesVersionedReconcile() {
        CpfTcpUnknownResultStore store = new CpfTcpUnknownResultStore(10, tempDir.resolve("unknown.journal"));
        store.record(new CpfTcpUnknownResult("C1", Instant.now(), new byte[]{1}, "timeout"));
        CpfTcpOperations operations = new CpfTcpOperations(store, new CpfTcpCorrelationRegistry(10, 10));

        assertThat(operations.snapshot().durableUnknownStore()).isTrue();
        assertThatThrownBy(() -> operations.reconcile("C1", 1L, "OP", "reason"))
                .isInstanceOf(java.util.ConcurrentModificationException.class);
        assertThat(operations.reconcile("C1", 0L, "OP", "reason")).isTrue();
        assertThat(operations.audit()).singleElement().satisfies(audit -> {
            assertThat(audit.correlationId()).isEqualTo("C1");
            assertThat(audit.resultingVersion()).isEqualTo(1L);
        });
    }
}

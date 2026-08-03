package com.cpf.core.common.servicecall;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CpfServiceCallLogWriterTest {

    @Test
    void isCircuitOpenMovesExpiredOpenStateToHalfOpen() {
        CpfServiceRegistryRepository repository = mock(CpfServiceRegistryRepository.class);
        CpfServiceCallLogWriter logWriter = new CpfServiceCallLogWriter(repository);
        ServiceCallResolvedTarget target = new ServiceCallResolvedTarget(
                Map.of("serviceId", "MBR"),
                Map.of("endpointCode", "MBR_API"),
                Map.of("instanceId", "MBR-1"),
                Map.of(),
                "http://localhost:8081",
                "PRIMARY");
        when(repository.findCircuitState(target)).thenReturn(Optional.of(Map.of(
                "circuitState", "OPEN",
                "openedAt", Timestamp.from(Instant.now().minusSeconds(60)))));

        boolean open = logWriter.isCircuitOpen(target, 1000L);

        assertThat(open).isFalse();
        verify(repository).recordCircuitHalfOpen(target);
    }
}

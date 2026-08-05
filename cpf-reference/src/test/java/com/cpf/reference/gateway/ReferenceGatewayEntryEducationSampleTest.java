package com.cpf.reference.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import com.cpf.core.api.gateway.CpfGatewayEntryPolicyPort;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ReferenceGatewayEntryEducationSampleTest {
    @Test
    void exposesOnlyStateVersionAndAggregateTelemetry() {
        CpfGatewayEntryPolicyPort port = new CpfGatewayEntryPolicyPort() {
            @Override public Snapshot snapshot() {
                return new Snapshot(7L, State.MAINTENANCE, Duration.ofSeconds(30), Instant.EPOCH);
            }
            @Override public Telemetry telemetry() {
                return new Telemetry(10L, 3L, 1L, 1L, 0L, 1L, Instant.EPOCH);
            }
            @Override public Snapshot replace(long expectedVersion, long nextVersion, State state, Duration retryAfter) {
                throw new UnsupportedOperationException();
            }
            @Override public Decision evaluate(Request request) {
                throw new AssertionError("Education status consumer must not re-evaluate requests");
            }
        };

        ReferenceGatewayEntryEducationSample.EntryStatus status =
                new ReferenceGatewayEntryEducationSample(port).status();

        assertThat(status.version()).isEqualTo(7L);
        assertThat(status.state()).isEqualTo("MAINTENANCE");
        assertThat(status.allowed()).isEqualTo(10L);
        assertThat(status.denied()).isEqualTo(3L);
    }
}

package com.cpf.core.api.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class CpfGatewayEntryPolicyPortTest {
    @Test
    void requestNormalizesMethodProtocolAndNullMetadata() {
        CpfGatewayEntryPolicyPort.Request request = new CpfGatewayEntryPolicyPort.Request(
                null, " post ", " http/2.0 ", true, 8443, null, null);

        assertThat(request.path()).isEqualTo("/");
        assertThat(request.method()).isEqualTo("POST");
        assertThat(request.protocol()).isEqualTo("HTTP/2.0");
        assertThat(request.remoteAddress()).isEmpty();
        assertThat(request.requestedAt()).isNotNull();
    }

    @Test
    void invalidPortAndNegativeRetryAreRejected() {
        assertThatThrownBy(() -> new CpfGatewayEntryPolicyPort.Request(
                "/", "GET", "HTTP/1.1", false, 65_536, "", Instant.EPOCH))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CpfGatewayEntryPolicyPort.Snapshot(
                1L, CpfGatewayEntryPolicyPort.State.ACTIVE, Duration.ofSeconds(-1), Instant.EPOCH))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

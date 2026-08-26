package com.cpf.batch.runtime;

import com.cpf.batch.api.BatControlHeaders;
import com.cpf.batch.api.RuntimeRegistration;
import com.cpf.batch.api.RuntimeRole;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class BatRuntimeIdentityRestClientCustomizerTest {
    @Test
    void runtimeIdentityOverwritesInboundCallerHeaders() {
        RuntimeRegistration registration = new RuntimeRegistration(
                RuntimeRole.WORKER,
                "cpf-batch-worker",
                "worker-a",
                "BAT",
                "worker-a",
                "localhost",
                "local",
                "default",
                "dev",
                "sha",
                "checksum",
                "test",
                List.of(),
                Map.of(),
                "1",
                "1",
                "v1",
                Instant.now());
        BatRuntimeIdentityRestClientCustomizer customizer =
                new BatRuntimeIdentityRestClientCustomizer(registration);
        HttpHeaders headers = new HttpHeaders();
        headers.set(BatControlHeaders.CALLER_SERVICE, "SPOOFED");
        headers.set(BatControlHeaders.CALLER_INSTANCE_ID, "spoofed-instance");

        customizer.applyIdentity(headers);

        assertThat(headers.getFirst(BatControlHeaders.CALLER_SERVICE)).isEqualTo("BAT");
        assertThat(headers.getFirst(BatControlHeaders.CALLER_INSTANCE_ID)).isEqualTo("worker-a");
    }
}

package com.cpf.batch.runtime;

import com.cpf.batch.api.RuntimeRegistration;
import com.cpf.batch.api.RuntimeRole;
import com.cpf.core.api.util.CpfHeaders;
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
        headers.set(CpfHeaders.callerService(), "SPOOFED");
        headers.set(CpfHeaders.callerInstanceId(), "spoofed-instance");

        customizer.applyIdentity(headers);

        assertThat(headers.getFirst(CpfHeaders.callerService())).isEqualTo("BAT");
        assertThat(headers.getFirst(CpfHeaders.callerInstanceId())).isEqualTo("worker-a");
    }
}

package com.cpf.starter.http.runtimecontrol;

import com.cpf.core.api.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.core.api.runtimecontrol.CpfRuntimePayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cpf.core.common.http.CpfServiceEndpointRegistry;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CpfExternalInstitutionRuntimeApplierTest {
    @Test
    void appliesVersionedEndpointAndTimeoutToActualRegistry() {
        CpfServiceEndpointRegistry registry = new CpfServiceEndpointRegistry(null);
        var applier = new CpfExternalInstitutionRuntimeApplier(registry, null);
        var result = applier.apply(delivery(1L, Map.of("endpoints", List.of(Map.of(
                "serviceId", "partner-a",
                "baseUrl", "https://partner.example",
                "timeoutMillis", 7_500)))));

        assertThat(result.applied()).isTrue();
        assertThat(registry.baseUrl("partner-a")).isEqualTo("https://partner.example");
        assertThat(registry.runtimeEndpoint("partner-a").timeoutMillis()).isEqualTo(7_500);
        assertThat(registry.runtimeSnapshot().version()).isEqualTo(1L);
    }

    @Test
    void rejectsLayoutWithoutVersionAndInvalidTimeout() {
        CpfServiceEndpointRegistry registry = new CpfServiceEndpointRegistry(null);
        var applier = new CpfExternalInstitutionRuntimeApplier(registry, null);
        assertThat(applier.apply(delivery(1L, Map.of("endpoints", List.of(Map.of(
                "serviceId", "partner-a", "baseUrl", "https://partner.example",
                "layoutId", "FEP-A"))))).applied()).isFalse();
        assertThat(applier.apply(delivery(2L, Map.of("endpoints", List.of(Map.of(
                "serviceId", "partner-a", "baseUrl", "https://partner.example",
                "timeoutMillis", 300_001))))).applied()).isFalse();
    }

    private CpfRuntimeDelivery delivery(long version, Map<String, Object> payload) {
        CpfRuntimePayload typed = CpfRuntimePayload.parse(new ObjectMapper().valueToTree(payload).toString());
        return new CpfRuntimeDelivery("D-" + version, "C-" + version, "EXTERNAL_INSTITUTION", "CPF-01",
                version, version, "request-" + version, "payload-" + version,
                typed, 1, Instant.now().plusSeconds(60));
    }
}

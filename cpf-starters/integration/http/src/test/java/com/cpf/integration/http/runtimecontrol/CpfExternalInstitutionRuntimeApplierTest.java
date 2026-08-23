package com.cpf.integration.http.runtimecontrol;

import com.cpf.platform.operations.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimePayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cpf.integration.http.internal.CpfServiceEndpointRegistry;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class CpfExternalInstitutionRuntimeApplierTest {
    @Test
    void appliesVersionedEndpointAndTimeoutToActualRegistry() {
        CpfServiceEndpointRegistry registry = new CpfServiceEndpointRegistry(null);
        var applier = new CpfExternalInstitutionRuntimeApplier(registry, null);
        var result = applier.apply(delivery(1L, Map.of("endpoints", List.of(Map.of(
                "serviceId", "partner-a",
                "baseUrl", "https://partner.example",
                "timeoutMillis", 7_500,
                "attributes", Map.of("allowDns", true, "allowPublic", true, "allowedPorts", "443"))))));

        assertThat(result.applied()).isTrue();
        assertThat(registry.runtimeEndpoint("partner-a").baseUrl()).isEqualTo("https://partner.example");
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

    @Test
    void validatesSelectedLayoutThroughProviderNeutralContract() {
        CpfServiceEndpointRegistry registry = new CpfServiceEndpointRegistry(null);
        AtomicReference<String> validated = new AtomicReference<>();
        var applier = new CpfExternalInstitutionRuntimeApplier(
                registry, (layoutId, version) -> validated.set(layoutId + ":" + version));

        var result = applier.apply(delivery(1L, Map.of("endpoints", List.of(Map.of(
                "serviceId", "partner-a",
                "baseUrl", "https://partner.example",
                "layoutId", "FEP-A",
                "layoutVersion", "2026-08",
                "attributes", dnsAttributes())))));

        assertThat(result.applied()).isTrue();
        assertThat(validated).hasValue("FEP-A:2026-08");
    }

    @Test
    void rejectsDnsEndpointWithoutExplicitNetworkPolicyOptIn() {
        CpfServiceEndpointRegistry registry = new CpfServiceEndpointRegistry(null);
        var applier = new CpfExternalInstitutionRuntimeApplier(registry, null);

        var result = applier.apply(delivery(1L, Map.of("endpoints", List.of(Map.of(
                "serviceId", "partner-a", "baseUrl", "https://partner.example")))));

        assertThat(result.applied()).isFalse();
        assertThat(result.unknownResult()).isFalse();
        assertThat(registry.runtimeSnapshot().version()).isZero();
    }

    @Test
    void sameVersionDifferentPayloadFailsWithoutOverwritingTheAppliedSnapshot() {
        CpfServiceEndpointRegistry registry = new CpfServiceEndpointRegistry(null);
        var applier = new CpfExternalInstitutionRuntimeApplier(registry, null);
        var first = applier.apply(delivery(1L, Map.of("endpoints", List.of(Map.of(
                "serviceId", "partner-a", "baseUrl", "https://partner-a.example",
                "attributes", dnsAttributes())))));
        var conflict = applier.apply(delivery(1L, Map.of("endpoints", List.of(Map.of(
                "serviceId", "partner-a", "baseUrl", "https://partner-b.example",
                "attributes", dnsAttributes())))));

        assertThat(first.applied()).isTrue();
        assertThat(conflict.applied()).isFalse();
        assertThat(conflict.unknownResult()).isFalse();
        assertThat(registry.runtimeEndpoint("partner-a").baseUrl()).isEqualTo("https://partner-a.example");
        assertThat(registry.runtimeSnapshot().version()).isEqualTo(1L);
    }

    private Map<String, Object> dnsAttributes() {
        return Map.of("allowDns", true, "allowPublic", true, "allowedPorts", "443");
    }

    private CpfRuntimeDelivery delivery(long version, Map<String, Object> payload) {
        CpfRuntimePayload typed = CpfRuntimePayload.parse(new ObjectMapper().valueToTree(payload).toString());
        return new CpfRuntimeDelivery("D-" + version, "C-" + version, "EXTERNAL_INSTITUTION", "CPF-01",
                version, version, "request-" + version, "payload-" + version,
                typed, 1, Instant.now().plusSeconds(60));
    }
}

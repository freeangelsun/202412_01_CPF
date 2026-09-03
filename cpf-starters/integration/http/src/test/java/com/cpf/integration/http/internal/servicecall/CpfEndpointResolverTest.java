package com.cpf.integration.http.internal.servicecall;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Generated Domain operation endpoint와 Runtime Agent physical instance endpoint의
 * 분리를 검증합니다. 운영 instance는 service 단위로 재사용해야 합니다.
 */
class CpfEndpointResolverTest {

    @Test
    void fallsBackToTheServiceRuntimeInstanceWhenOperationEndpointHasNoOwnInstance() {
        CpfServiceRegistry services = mock(CpfServiceRegistry.class);
        CpfEndpointRegistry endpoints = mock(CpfEndpointRegistry.class);
        CpfServiceInstanceRegistry instances = mock(CpfServiceInstanceRegistry.class);
        CpfRoutingPolicyResolver policies = mock(CpfRoutingPolicyResolver.class);

        when(services.findService("MBR")).thenReturn(Optional.of(Map.of("serviceId", "MBR")));
        when(endpoints.findEndpoint("MBR", "MBR_SAMPLE_TX_CREATE"))
                .thenReturn(Optional.of(Map.of(
                        "endpointCode", "MBR_SAMPLE_TX_CREATE",
                        "baseUrl", "http://logical-operation.example")));
        when(policies.resolve("MBR", "MBR_SAMPLE_TX_CREATE"))
                .thenReturn(Map.of("routingMode", "PRIMARY"));
        when(instances.findInstances("MBR", "MBR_SAMPLE_TX_CREATE", null, 100))
                .thenReturn(List.of());
        when(instances.findInstances("MBR", null, null, 100))
                .thenReturn(List.of(Map.of(
                        "instanceId", "mbr-online-01",
                        "baseUrl", "http://mbr-online-01.example",
                        "activeYn", "Y",
                        "instanceStatus", "UP",
                        "priorityNo", 100,
                        "weight", 100)));

        ServiceCallResolvedTarget target = new CpfEndpointResolver(
                services, endpoints, instances, policies, new CpfHealthAwareInstanceSelector())
                .resolve(ServiceCallRequest.builder("MBR")
                        .endpointCode("MBR_SAMPLE_TX_CREATE")
                        .requestPath("/_cpf/domain/MBR/MBR_SAMPLE_TX_CREATE")
                        .build());

        assertThat(target.endpointCode()).isEqualTo("MBR_SAMPLE_TX_CREATE");
        assertThat(target.instanceId()).isEqualTo("mbr-online-01");
        assertThat(target.baseUrl()).isEqualTo("http://mbr-online-01.example");
        verify(instances).findInstances("MBR", "MBR_SAMPLE_TX_CREATE", null, 100);
        verify(instances).findInstances("MBR", null, null, 100);
    }
}

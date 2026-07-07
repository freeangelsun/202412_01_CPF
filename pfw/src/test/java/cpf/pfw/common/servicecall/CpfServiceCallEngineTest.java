package cpf.pfw.common.servicecall;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CpfServiceCallEngineTest {

    @Test
    void resolveSelectsHealthyActiveInstance() {
        CpfServiceRegistry serviceRegistry = mock(CpfServiceRegistry.class);
        CpfEndpointRegistry endpointRegistry = mock(CpfEndpointRegistry.class);
        CpfServiceInstanceRegistry instanceRegistry = mock(CpfServiceInstanceRegistry.class);
        CpfRoutingPolicyResolver routingPolicyResolver = mock(CpfRoutingPolicyResolver.class);

        when(serviceRegistry.findService("MBR"))
                .thenReturn(java.util.Optional.of(Map.of("serviceId", "MBR")));
        when(endpointRegistry.findEndpoint("MBR", "MBR_API"))
                .thenReturn(java.util.Optional.of(Map.of(
                        "endpointCode", "MBR_API",
                        "baseUrl", "http://localhost:8081")));
        when(routingPolicyResolver.resolve("MBR", "MBR_API"))
                .thenReturn(Map.of("routingMode", "PRIMARY"));
        when(instanceRegistry.findInstances("MBR", "MBR_API", null, 100))
                .thenReturn(List.of(
                        Map.of("instanceId", "MBR-2", "activeYn", "Y", "instanceStatus", "DOWN", "baseUrl", "http://localhost:18081"),
                        Map.of("instanceId", "MBR-1", "activeYn", "Y", "instanceStatus", "UP", "baseUrl", "http://localhost:8081")));

        CpfEndpointResolver resolver = new CpfEndpointResolver(
                serviceRegistry,
                endpointRegistry,
                instanceRegistry,
                routingPolicyResolver,
                new CpfHealthAwareInstanceSelector());

        ServiceCallResolvedTarget target = resolver.resolve(ServiceCallRequest.builder("MBR")
                .endpointCode("MBR_API")
                .build());

        assertThat(target.baseUrl()).isEqualTo("http://localhost:8081");
        assertThat(target.instance()).containsEntry("instanceId", "MBR-1");
    }

    @Test
    void invokeWritesSuccessHistory() {
        CpfEndpointResolver resolver = mock(CpfEndpointResolver.class);
        CpfServiceCallLogWriter logWriter = mock(CpfServiceCallLogWriter.class);
        CpfServiceCallProperties properties = new CpfServiceCallProperties();
        ServiceCallResolvedTarget target = new ServiceCallResolvedTarget(
                Map.of("serviceId", "MBR"),
                Map.of("endpointCode", "MBR_API", "baseUrl", "http://localhost:8081"),
                Map.of("instanceId", "MBR-1", "baseUrl", "http://localhost:8081"),
                Map.of("routingMode", "PRIMARY"),
                "http://localhost:8081",
                "PRIMARY");
        when(resolver.resolve(any())).thenReturn(target);
        CpfServiceCallEngine engine = new CpfServiceCallEngine(resolver, logWriter, properties);

        ServiceCallResult<String> result = engine.invoke(
                ServiceCallRequest.builder("MBR").endpointCode("MBR_API").build(),
                () -> "OK");

        assertThat(result.status()).isEqualTo("SUCCESS");
        assertThat(result.responseBody()).isEqualTo("OK");
        verify(logWriter).write(any(), any(), org.mockito.ArgumentMatchers.eq("SUCCESS"),
                org.mockito.ArgumentMatchers.eq(200), org.mockito.ArgumentMatchers.anyLong(), any(), any());
    }
}

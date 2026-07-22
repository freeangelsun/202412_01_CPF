package cpf.pfw.common.servicecall;

import cpf.pfw.common.logging.segment.TransactionSegmentRecord;
import cpf.pfw.common.logging.segment.TransactionSegmentScope;
import cpf.pfw.common.logging.segment.TransactionSegmentService;
import cpf.pfw.common.reconciliation.CpfReconciliationPort;
import cpf.pfw.common.reconciliation.CpfUnknownResultRecord;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
        ServiceCallResolvedTarget target = target("MBR-1", "http://localhost:8081");
        when(resolver.resolve(any(), any())).thenReturn(target);
        when(logWriter.isCircuitOpen(any(), anyLong())).thenReturn(false);
        CpfServiceCallEngine engine = new CpfServiceCallEngine(resolver, logWriter, properties);

        ServiceCallResult<String> result = engine.invoke(
                ServiceCallRequest.builder("MBR").endpointCode("MBR_API").build(),
                () -> "OK");

        assertThat(result.status()).isEqualTo("SUCCESS");
        assertThat(result.responseBody()).isEqualTo("OK");
        verify(logWriter).write(any(), any(), org.mockito.ArgumentMatchers.eq("SUCCESS"),
                org.mockito.ArgumentMatchers.eq(200), org.mockito.ArgumentMatchers.anyLong(), any(), any());
        verify(logWriter).markSuccess(org.mockito.ArgumentMatchers.eq(target), org.mockito.ArgumentMatchers.eq(200), anyLong());
    }

    @Test
    void invokeRetriesAndFailoversToNextInstance() {
        CpfEndpointResolver resolver = mock(CpfEndpointResolver.class);
        CpfServiceCallLogWriter logWriter = mock(CpfServiceCallLogWriter.class);
        CpfServiceCallProperties properties = new CpfServiceCallProperties();
        properties.setDefaultRetryCount(1);
        ServiceCallResolvedTarget first = target("MBR-1", "http://localhost:18081");
        ServiceCallResolvedTarget second = target("MBR-2", "http://localhost:28081");
        when(resolver.resolve(any(), any())).thenReturn(first, second);
        when(logWriter.isCircuitOpen(any(), anyLong())).thenReturn(false);
        CpfServiceCallEngine engine = new CpfServiceCallEngine(resolver, logWriter, properties);
        AtomicInteger callCount = new AtomicInteger();

        ServiceCallResult<String> result = engine.invoke(
                ServiceCallRequest.builder("MBR").endpointCode("MBR_API").retryCount(1).build(),
                selectedTarget -> {
                    if (callCount.incrementAndGet() == 1) {
                        throw new IllegalStateException("첫 번째 instance 실패");
                    }
                    return selectedTarget.instanceId() + ":OK";
                });

        assertThat(result.status()).isEqualTo("SUCCESS");
        assertThat(result.attemptCount()).isEqualTo(2);
        assertThat(result.target().instanceId()).isEqualTo("MBR-2");
        assertThat(result.responseBody()).isEqualTo("MBR-2:OK");
        verify(resolver, times(2)).resolve(any(), any());
        verify(logWriter).markFailure(org.mockito.ArgumentMatchers.eq(first), any(), anyLong(), any(), org.mockito.ArgumentMatchers.anyInt());
        verify(logWriter).markSuccess(org.mockito.ArgumentMatchers.eq(second), org.mockito.ArgumentMatchers.eq(200), anyLong());
    }

    @Test
    void invokeBlocksRemoteCallWhenCircuitIsOpen() {
        CpfEndpointResolver resolver = mock(CpfEndpointResolver.class);
        CpfServiceCallLogWriter logWriter = mock(CpfServiceCallLogWriter.class);
        CpfServiceCallProperties properties = new CpfServiceCallProperties();
        ServiceCallResolvedTarget target = target("MBR-1", "http://localhost:8081");
        when(resolver.resolve(any(), any())).thenReturn(target);
        when(logWriter.isCircuitOpen(any(), anyLong())).thenReturn(true);
        CpfServiceCallEngine engine = new CpfServiceCallEngine(resolver, logWriter, properties);

        ServiceCallResult<String> result = engine.invoke(
                ServiceCallRequest.builder("MBR").endpointCode("MBR_API").build(),
                () -> {
                    throw new AssertionError("circuit open 상태에서는 원격 호출이 실행되면 안 됩니다.");
                });

        assertThat(result.status()).isEqualTo("FAILED");
        assertThat(result.failureCode()).isEqualTo("CIRCUIT_OPEN");
        verify(logWriter, never()).markFailure(any(), any(), anyLong(), any(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void recordsAttemptSegmentAndRegistersUnknownResultOnTimeout() {
        CpfEndpointResolver resolver = mock(CpfEndpointResolver.class);
        CpfServiceCallLogWriter logWriter = mock(CpfServiceCallLogWriter.class);
        TransactionSegmentService segmentService = mock(TransactionSegmentService.class);
        TransactionSegmentScope scope = mock(TransactionSegmentScope.class);
        TransactionSegmentRecord segment = new TransactionSegmentRecord();
        CpfReconciliationPort reconciliationPort = mock(CpfReconciliationPort.class);
        CpfServiceCallProperties properties = new CpfServiceCallProperties();
        properties.setDefaultRetryCount(0);
        properties.setRetryBackoffMillis(0);
        ServiceCallResolvedTarget target = target("MBR-1", "http://localhost:8081");
        when(resolver.resolve(any(), any())).thenReturn(target);
        when(logWriter.isCircuitOpen(any(), anyLong())).thenReturn(false);
        when(segmentService.start(any(), any(), any(), any(), any(), any(), any())).thenReturn(scope);
        when(scope.record()).thenReturn(segment);
        when(scope.transactionGlobalId()).thenReturn("GLOBAL-001");
        when(scope.transactionSegmentId()).thenReturn("SEG-001");
        when(reconciliationPort.register(any())).thenAnswer(invocation -> invocation.getArgument(0));
        CpfServiceCallEngine engine = new CpfServiceCallEngine(
                resolver,
                logWriter,
                properties,
                segmentService,
                reconciliationPort);

        ServiceCallResult<String> result = engine.invoke(
                ServiceCallRequest.builder("MBR")
                        .endpointCode("MBR_API")
                        .attribute("sourceModuleCode", "XYZ")
                        .attribute("externalKey", "EXT-001")
                        .build(),
                () -> {
                    throw new IllegalStateException("하위 호출 timeout", new TimeoutException("timeout"));
                });

        assertThat(result.status()).isEqualTo("UNKNOWN");
        assertThat(segment.getSelectedInstanceId()).isEqualTo("MBR-1");
        assertThat(segment.getAttemptNo()).isEqualTo(1);
        assertThat(segment.getResultState()).isEqualTo("UNKNOWN");
        assertThat(segment.getUnknownResultId()).isNotBlank();
        verify(scope).fail(org.mockito.ArgumentMatchers.eq("IllegalStateException"), any());
        verify(reconciliationPort).register(org.mockito.ArgumentMatchers.argThat((CpfUnknownResultRecord row) ->
                "GLOBAL-001".equals(row.transactionGlobalId())
                        && "SEG-001".equals(row.segmentId())
                        && "EXT-001".equals(row.externalKey())));
    }

    @Test
    void recordsRetryAndFailoverOnSecondAttemptSegment() {
        CpfEndpointResolver resolver = mock(CpfEndpointResolver.class);
        CpfServiceCallLogWriter logWriter = mock(CpfServiceCallLogWriter.class);
        TransactionSegmentService segmentService = mock(TransactionSegmentService.class);
        TransactionSegmentScope firstScope = mock(TransactionSegmentScope.class);
        TransactionSegmentScope secondScope = mock(TransactionSegmentScope.class);
        TransactionSegmentRecord firstRecord = new TransactionSegmentRecord();
        TransactionSegmentRecord secondRecord = new TransactionSegmentRecord();
        CpfServiceCallProperties properties = new CpfServiceCallProperties();
        properties.setDefaultRetryCount(1);
        properties.setRetryBackoffMillis(0);
        ServiceCallResolvedTarget first = target("MBR-1", "http://localhost:18081");
        ServiceCallResolvedTarget second = target("MBR-2", "http://localhost:28081");
        when(resolver.resolve(any(), any())).thenReturn(first, second);
        when(logWriter.isCircuitOpen(any(), anyLong())).thenReturn(false);
        when(segmentService.start(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(firstScope, secondScope);
        when(firstScope.record()).thenReturn(firstRecord);
        when(secondScope.record()).thenReturn(secondRecord);
        CpfServiceCallEngine engine = new CpfServiceCallEngine(
                resolver,
                logWriter,
                properties,
                segmentService,
                null);
        AtomicInteger calls = new AtomicInteger();

        ServiceCallResult<String> result = engine.invoke(
                ServiceCallRequest.builder("MBR").retryCount(1).build(),
                target -> {
                    if (calls.incrementAndGet() == 1) {
                        throw new IllegalStateException("first failed");
                    }
                    return "OK";
                });

        assertThat(result.status()).isEqualTo("SUCCESS");
        assertThat(firstRecord.getAttemptNo()).isEqualTo(1);
        assertThat(firstRecord.getFailoverYn()).isEqualTo("N");
        assertThat(secondRecord.getAttemptNo()).isEqualTo(2);
        assertThat(secondRecord.getRetryYn()).isEqualTo("Y");
        assertThat(secondRecord.getFailoverYn()).isEqualTo("Y");
        assertThat(secondRecord.getSelectedInstanceId()).isEqualTo("MBR-2");
        verify(firstScope).fail(any(), any());
        verify(secondScope).success();
    }

    @Test
    @SuppressWarnings("unchecked")
    void remoteFacadeProxySupportDelegatesToServiceCallEngine() {
        CpfServiceCallEngine engine = mock(CpfServiceCallEngine.class);
        ServiceCallRequest request = ServiceCallRequest.builder("MBR").endpointCode("MBR_API").build();
        ServiceCallResolvedTarget target = target("MBR-1", "http://localhost:8081");
        when(engine.invoke(org.mockito.ArgumentMatchers.eq(request), any(Function.class)))
                .thenReturn(ServiceCallResult.success(target, "OK", 200, 3L, 1));
        TestRemoteFacadeProxy proxy = new TestRemoteFacadeProxy(engine);

        ServiceCallResult<String> result = proxy.callRemote(request);

        assertThat(result.status()).isEqualTo("SUCCESS");
        verify(engine).invoke(org.mockito.ArgumentMatchers.eq(request), any(Function.class));
    }

    private ServiceCallResolvedTarget target(String instanceId, String baseUrl) {
        return new ServiceCallResolvedTarget(
                Map.of("serviceId", "MBR"),
                Map.of("endpointCode", "MBR_API", "baseUrl", "http://localhost:8081", "defaultTimeoutMs", 3000),
                Map.of("instanceId", instanceId, "baseUrl", baseUrl),
                Map.of("routingMode", "PRIMARY", "failoverEnabledYn", "Y"),
                baseUrl,
                "PRIMARY");
    }

    private static final class TestRemoteFacadeProxy extends CpfRemoteFacadeProxySupport {
        private TestRemoteFacadeProxy(CpfServiceCallEngine serviceCallEngine) {
            super(serviceCallEngine);
        }

        private ServiceCallResult<String> callRemote(ServiceCallRequest request) {
            return call(request, target -> target.instanceId() + ":OK");
        }
    }
}

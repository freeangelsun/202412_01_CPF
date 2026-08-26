package com.cpf.gateway.scg;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.cpf.gateway.api.CpfGatewayAuditEvent;
import com.cpf.gateway.api.CpfGatewayAuthenticationPort;
import com.cpf.gateway.api.CpfGatewayAuthorizationPort;
import com.cpf.core.api.transaction.CpfTransactionIds;
import com.cpf.gateway.api.CpfGatewayEntryPolicyPort;
import com.cpf.platform.operations.channelregistry.application.CpfChannelPolicyService;
import com.cpf.gateway.config.CpfGatewaySafetyEnforcer;
import com.cpf.gateway.config.CpfGatewaySafetyProperties;
import com.cpf.gateway.logging.CpfGatewayCaptureService;
import com.cpf.gateway.route.CpfGatewayRouteSnapshot;
import com.cpf.gateway.runtime.CpfGatewayRuntimePolicy;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

@SuppressWarnings("deprecation")
class CpfScgPrimaryHandlerEntryPolicyTest {
    @Test
    void maintenanceIsDeniedAndAuditedBeforeRouteLookupOrBodyProcessing() throws Exception {
        CpfGatewayRouteSnapshot snapshot = mock(CpfGatewayRouteSnapshot.class);
        CpfScgTargetResolver targets = mock(CpfScgTargetResolver.class);
        CpfGatewayAuthenticationPort authentication = mock(CpfGatewayAuthenticationPort.class);
        CpfGatewayAuthorizationPort authorization = mock(CpfGatewayAuthorizationPort.class);
        CpfChannelPolicyService channelPolicies = mock(CpfChannelPolicyService.class);
        CpfGatewayRuntimePolicy runtimePolicy = mock(CpfGatewayRuntimePolicy.class);
        CpfGatewayAuditRecoverySpool auditRecovery = mock(CpfGatewayAuditRecoverySpool.class);
        CpfGatewayLedgerRecoverySpool ledgerRecovery = mock(CpfGatewayLedgerRecoverySpool.class);
        CpfGatewayCaptureService captureService = mock(CpfGatewayCaptureService.class);
        CircuitBreakerFactory<?, ?> circuitBreakers = mock(CircuitBreakerFactory.class);
        CpfGatewaySafetyProperties safety = new CpfGatewaySafetyProperties();
        CpfGatewaySafetyEnforcer safetyEnforcer = mock(CpfGatewaySafetyEnforcer.class);
        CpfGatewayEntryPolicyPort entryPolicy = mock(CpfGatewayEntryPolicyPort.class);
        when(entryPolicy.evaluate(any())).thenReturn(new CpfGatewayEntryPolicyPort.Decision(
                false,
                503,
                CpfGatewayEntryPolicyPort.State.MAINTENANCE,
                Duration.ofSeconds(90),
                "GATEWAY_MAINTENANCE",
                7L));

        CpfScgPrimaryHandler handler = new CpfScgPrimaryHandler(
                snapshot, targets, authentication, authorization, channelPolicies, runtimePolicy,
                auditRecovery, ledgerRecovery, captureService, circuitBreakers, safety,
                safetyEnforcer, entryPolicy);

        ServerResponse response = handler.handle(request());

        assertThat(response.statusCode().value()).isEqualTo(503);
        assertThat(response.headers().getFirst(HttpHeaders.RETRY_AFTER)).isEqualTo("90");
        assertThat(response.headers().getFirst("X-Cpf-Gateway-State")).isEqualTo("MAINTENANCE");
        assertThat(response.headers().getFirst("X-Cpf-Gateway-Policy-Version")).isEqualTo("7");
        ArgumentCaptor<CpfGatewayAuditEvent> auditEvent = ArgumentCaptor.forClass(CpfGatewayAuditEvent.class);
        verify(auditRecovery).record(auditEvent.capture());
        assertThat(CpfTransactionIds.isCanonical(auditEvent.getValue().transactionId())).isTrue();
        verifyNoInteractions(snapshot, targets, authentication, authorization, channelPolicies,
                runtimePolicy, ledgerRecovery, captureService, circuitBreakers, safetyEnforcer);
    }

    @Test
    void auditFailureKeepsRequestFailClosed() throws Exception {
        CpfGatewayRouteSnapshot snapshot = mock(CpfGatewayRouteSnapshot.class);
        CpfGatewayAuditRecoverySpool auditRecovery = mock(CpfGatewayAuditRecoverySpool.class);
        org.mockito.Mockito.doThrow(new IllegalStateException("spool unavailable"))
                .when(auditRecovery).record(any());
        CpfGatewayEntryPolicyPort entryPolicy = new CpfGatewayEntryPolicyPort() {
            public Snapshot snapshot() { return new Snapshot(0L, State.ACTIVE, Duration.ZERO, Instant.EPOCH); }
            public Telemetry telemetry() { return new Telemetry(0, 0, 0, 0, 0, 0, Instant.EPOCH); }
            public Snapshot replace(long expected, long next, State state, Duration retry) {
                throw new UnsupportedOperationException();
            }
            public Decision evaluate(Request request) {
                return new Decision(false, 426, State.ACTIVE, Duration.ZERO,
                        "INGRESS_TLS_REQUIRED", 0L);
            }
        };
        CpfScgPrimaryHandler handler = new CpfScgPrimaryHandler(
                snapshot,
                mock(CpfScgTargetResolver.class),
                mock(CpfGatewayAuthenticationPort.class),
                mock(CpfGatewayAuthorizationPort.class),
                mock(CpfChannelPolicyService.class),
                mock(CpfGatewayRuntimePolicy.class),
                auditRecovery,
                mock(CpfGatewayLedgerRecoverySpool.class),
                mock(CpfGatewayCaptureService.class),
                mock(CircuitBreakerFactory.class),
                new CpfGatewaySafetyProperties(),
                mock(CpfGatewaySafetyEnforcer.class),
                entryPolicy);

        ServerResponse response = handler.handle(request());

        assertThat(response.statusCode().value()).isEqualTo(503);
        assertThat(response.headers().getFirst("X-Cpf-Gateway-Entry-Reason"))
                .isEqualTo("ENTRY_AUDIT_UNAVAILABLE");
        verifyNoInteractions(snapshot);
    }

    private static ServerRequest request() {
        MockHttpServletRequest servlet = new MockHttpServletRequest(
                "POST", "/cpf/execute/OACCAC0001");
        servlet.setLocalPort(8443);
        servlet.setSecure(true);
        servlet.setProtocol("HTTP/1.1");
        servlet.setRemoteAddr("127.0.0.1");
        return ServerRequest.create(servlet, List.of(new ByteArrayHttpMessageConverter()));
    }
}

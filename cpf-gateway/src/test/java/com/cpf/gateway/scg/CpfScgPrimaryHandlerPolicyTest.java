package com.cpf.gateway.scg;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.cpf.core.api.gateway.CpfGatewayAuthenticationPort;
import com.cpf.core.api.gateway.CpfGatewayAuthorizationPort;
import com.cpf.core.api.gateway.CpfGatewayPrincipal;
import com.cpf.core.api.gateway.CpfGatewayRoute;
import com.cpf.core.api.header.CpfHeaderNames;
import com.cpf.core.api.logging.policy.LogPolicyDecision;
import com.cpf.core.api.logging.policy.LogPolicyTargetType;
import com.cpf.core.channel.api.CpfChannelRegistryPort;
import com.cpf.core.channel.application.CpfChannelPolicyService;
import com.cpf.core.channel.model.CpfChannelDefinition;
import com.cpf.core.channel.model.CpfChannelExecutionPolicy;
import com.cpf.core.channel.model.CpfChannelPolicySnapshot;
import com.cpf.gateway.config.CpfGatewaySafetyEnforcer;
import com.cpf.gateway.config.CpfGatewaySafetyProperties;
import com.cpf.gateway.logging.CpfGatewayCaptureService;
import com.cpf.gateway.route.CpfGatewayRouteSnapshot;
import com.cpf.gateway.runtime.CpfGatewayRuntimePolicy;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.function.ServerRequest;

class CpfScgPrimaryHandlerPolicyTest {

    @Test
    void channelPolicyDenialStopsBeforeScgTargetSelection() {
        Fixture fixture = fixture(new PolicyPort(false, false));

        assertThatThrownBy(() -> fixture.handler().handle(request(Map.of())))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("채널 정책");

        verifyNoInteractions(fixture.targets(), fixture.circuitBreakers());
    }

    @Test
    void callerSignatureHeaderAloneDoesNotSatisfyVerifiedSignaturePolicy() {
        Fixture fixture = fixture(new PolicyPort(true, true));

        assertThatThrownBy(() -> fixture.handler().handle(request(Map.of(
                CpfHeaderNames.REQUEST_SIGNATURE, "attacker-supplied-value"))))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("요청 서명");

        verifyNoInteractions(fixture.targets(), fixture.circuitBreakers());
    }

    @Test
    void spoofedInternalCpfHeaderIsRejectedBeforeScgExchange() {
        Fixture fixture = fixture(new PolicyPort(true, false));

        assertThatThrownBy(() -> fixture.handler().handle(request(Map.of(
                CpfHeaderNames.GATEWAY_ROUTE_ID, "attacker-route"))))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Untrusted internal");

        verifyNoInteractions(fixture.targets(), fixture.circuitBreakers());
    }

    @Test
    void executionPathAndHeaderMismatchIsRejectedBeforeSnapshotLookup() {
        Fixture fixture = fixture(new PolicyPort(true, false));
        ServerRequest request = request(
                "/cpf/execute/OACCAC0002",
                Map.of(CpfHeaderNames.STANDARD_EXECUTION_ID, "OACCAC0001"));

        assertThatThrownBy(() -> fixture.handler().handle(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("일치하지 않습니다");

        verifyNoInteractions(fixture.targets(), fixture.circuitBreakers());
    }

    @Test
    void rawCredentialsNeverEnterTrustedDownstreamContext() {
        Fixture fixture = fixture(new PolicyPort(true, false));
        ServerRequest request = request(Map.of(
                CpfHeaderNames.AUTHORIZATION, "Bearer opaque",
                CpfHeaderNames.API_KEY, "raw-api-key",
                CpfHeaderNames.REQUEST_SIGNATURE, "raw-signature"));

        @SuppressWarnings("unchecked")
        Map<String, String> trusted = ReflectionTestUtils.invokeMethod(
                fixture.handler(), "trustedHeaders", request);

        assertThat(trusted).doesNotContainKeys(
                "authorization",
                CpfHeaderNames.API_KEY.toLowerCase(),
                CpfHeaderNames.REQUEST_SIGNATURE.toLowerCase());
        assertThat(trusted).containsEntry(
                CpfHeaderNames.CHANNEL_CODE.toLowerCase(), "WEB");
    }

    private Fixture fixture(CpfChannelRegistryPort registryPort) {
        CpfGatewayRouteSnapshot snapshot = mock(CpfGatewayRouteSnapshot.class);
        when(snapshot.resolve("OACCAC0001")).thenReturn(route());
        when(snapshot.resolve("OACCAC0002")).thenReturn(route());
        CpfScgTargetResolver targets = mock(CpfScgTargetResolver.class);
        CpfGatewayAuthenticationPort authentication = (ignored, credentials) ->
                new CpfGatewayPrincipal(
                        true, "client-1", Set.of("ACC_WRITE"), Map.of("authType", "TEST"));
        CpfGatewayAuthorizationPort authorization = (ignored, trusted) -> true;
        CpfGatewaySafetyProperties safety = new CpfGatewaySafetyProperties();
        CpfGatewayAuditRecoverySpool auditRecovery = mock(CpfGatewayAuditRecoverySpool.class);
        CpfGatewayLedgerRecoverySpool ledgerRecovery = mock(CpfGatewayLedgerRecoverySpool.class);
        CpfGatewayCaptureService captureService = mock(CpfGatewayCaptureService.class);
        when(captureService.resolve("OACCAC0001")).thenReturn(
                LogPolicyDecision.cpfDefault(
                        LogPolicyTargetType.ONLINE_TRANSACTION, "OACCAC0001"));
        @SuppressWarnings("unchecked")
        CircuitBreakerFactory<?, ?> circuitBreakers = mock(CircuitBreakerFactory.class);

        CpfScgPrimaryHandler handler = new CpfScgPrimaryHandler(
                snapshot,
                targets,
                authentication,
                authorization,
                new CpfChannelPolicyService(registryPort),
                new CpfGatewayRuntimePolicy(),
                auditRecovery,
                ledgerRecovery,
                captureService,
                circuitBreakers,
                safety,
                new CpfGatewaySafetyEnforcer(safety));
        return new Fixture(handler, targets, circuitBreakers);
    }

    private ServerRequest request(Map<String, String> extraHeaders) {
        return request("/cpf/execute/OACCAC0001", extraHeaders);
    }

    private ServerRequest request(String path, Map<String, String> extraHeaders) {
        MockHttpServletRequest servlet = new MockHttpServletRequest("POST", path);
        servlet.setRemoteAddr("127.0.0.1");
        servlet.setContent(new byte[0]);
        servlet.addHeader(CpfHeaderNames.ORIGINAL_CHANNEL_CODE, "WEB");
        servlet.addHeader(CpfHeaderNames.CHANNEL_CODE, "WEB");
        servlet.addHeader(CpfHeaderNames.REQUEST_TYPE, "INQUIRY");
        servlet.addHeader(CpfHeaderNames.API_KEY, "masked-test-key");
        extraHeaders.forEach(servlet::addHeader);
        return ServerRequest.create(servlet, List.of(new ByteArrayHttpMessageConverter()));
    }

    private CpfGatewayRoute route() {
        return new CpfGatewayRoute(
                "OACCAC0001",
                "ACC",
                "POST",
                "/api/v1/acc/accounts",
                "accAccountCreate",
                "ACC_WRITE",
                false,
                "1");
    }

    private record Fixture(
            CpfScgPrimaryHandler handler,
            CpfScgTargetResolver targets,
            CircuitBreakerFactory<?, ?> circuitBreakers) {}

    private static final class PolicyPort implements CpfChannelRegistryPort {
        private final boolean allowed;
        private final boolean signatureRequired;

        private PolicyPort(boolean allowed, boolean signatureRequired) {
            this.allowed = allowed;
            this.signatureRequired = signatureRequired;
        }

        @Override
        public CpfChannelPolicySnapshot loadSnapshot() {
            CpfChannelDefinition web = new CpfChannelDefinition(
                    "WEB", "웹", "CLIENT", "EXTERNAL", true, false, true,
                    false, true, "테스트", 1);
            CpfChannelExecutionPolicy policy = new CpfChannelExecutionPolicy(
                    "WEB.ACC",
                    "OACCAC0001",
                    "WEB",
                    "WEB",
                    "INQUIRY",
                    allowed,
                    true,
                    signatureRequired,
                    0,
                    null,
                    null,
                    true,
                    1);
            return new CpfChannelPolicySnapshot(
                    1, Instant.now(), Map.of("WEB", web), List.of(policy));
        }

        @Override
        public long saveChannel(CpfChannelDefinition channel, String actor, String reason) {
            throw new UnsupportedOperationException();
        }

        @Override
        public long savePolicy(CpfChannelExecutionPolicy policy, String actor, String reason) {
            throw new UnsupportedOperationException();
        }
    }
}

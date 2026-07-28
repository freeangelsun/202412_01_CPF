package com.cpf.gateway.service;

import com.cpf.core.api.gateway.CpfGatewayAuditPort;
import com.cpf.core.api.gateway.CpfGatewayAuthenticationPort;
import com.cpf.core.api.gateway.CpfGatewayAuthorizationPort;
import com.cpf.core.api.gateway.CpfGatewayPrincipal;
import com.cpf.core.api.gateway.CpfGatewayRoute;
import com.cpf.core.api.header.CpfHeaderNames;
import com.cpf.core.api.servicecall.CpfServiceCallExecutor;
import com.cpf.core.channel.api.CpfChannelRegistryPort;
import com.cpf.core.channel.application.CpfChannelPolicyService;
import com.cpf.core.channel.model.CpfChannelDefinition;
import com.cpf.core.channel.model.CpfChannelExecutionPolicy;
import com.cpf.core.channel.model.CpfChannelPolicySnapshot;
import com.cpf.gateway.route.CpfGatewayRouteSnapshot;
import com.cpf.gateway.runtime.CpfGatewayRuntimePolicy;
import com.cpf.gateway.transport.CpfGatewayHttpExchangePort;
import com.cpf.gateway.transport.CpfGatewayTransferPolicy;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CpfGatewayChannelPolicyTest {

    @Test
    void channelPolicyDenialStopsBeforeTargetSelectionAndHttpExchange() {
        Fixture fixture = fixture(new PolicyPort(false, false));
        HttpHeaders headers = channelHeaders();

        assertThatThrownBy(() -> fixture.service().execute("OACCAC0001", headers, new byte[0]))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("채널 정책");

        verifyNoInteractions(fixture.serviceCallExecutor(), fixture.httpExchange());
    }

    @Test
    void unverifiedSignatureHeaderDoesNotSatisfySignaturePolicy() {
        Fixture fixture = fixture(new PolicyPort(true, true));
        HttpHeaders headers = channelHeaders();
        headers.set(CpfHeaderNames.REQUEST_SIGNATURE, "attacker-supplied-value");

        assertThatThrownBy(() -> fixture.service().execute("OACCAC0001", headers, new byte[0]))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("요청 서명");

        verifyNoInteractions(fixture.serviceCallExecutor(), fixture.httpExchange());
    }


    @Test
    void rawCredentialsAndCallerSignatureAreNeverForwardedDownstream() {
        Fixture fixture = fixture(new PolicyPort(true, false));
        HttpHeaders inbound = channelHeaders();
        inbound.set(CpfHeaderNames.AUTHORIZATION, "Bearer attacker-visible");
        inbound.set(CpfHeaderNames.API_KEY, "raw-api-key");
        inbound.set(CpfHeaderNames.REQUEST_SIGNATURE, "raw-signature");
        inbound.set(CpfHeaderNames.STANDARD_EXECUTION_ID, "OATTACK0001");

        CpfGatewayRoute route = route();
        HttpHeaders outbound = ReflectionTestUtils.invokeMethod(
                fixture.service(), "outboundHeaders", inbound, route);

        assertThat(outbound).doesNotContainKeys(
                CpfHeaderNames.AUTHORIZATION, CpfHeaderNames.API_KEY, CpfHeaderNames.REQUEST_SIGNATURE);
        assertThat(outbound.getFirst(CpfHeaderNames.STANDARD_EXECUTION_ID))
                .isEqualTo(route.standardExecutionId());
    }

    @Test
    void routeCanonicalExecutionIdOverridesSpoofedInboundValue() {
        Fixture fixture = fixture(new PolicyPort(true, false));
        HttpHeaders inbound = channelHeaders();
        inbound.set(CpfHeaderNames.STANDARD_EXECUTION_ID, "OATTACK0001");
        CpfGatewayPrincipal principal = new CpfGatewayPrincipal(
                true, "client-1", Set.of("ACC_WRITE"), Map.of("requestSignatureVerified", "true"));

        Map<String, String> trusted = ReflectionTestUtils.invokeMethod(
                fixture.service(), "trustedHeaders", inbound, principal, route());

        assertThat(trusted.get(CpfHeaderNames.STANDARD_EXECUTION_ID)).isEqualTo("OACCAC0001");
    }

    @Test
    void encodedTraversalEndpointIsRejectedBeforeNetworkCall() {
        Fixture fixture = fixture(new PolicyPort(true, false));

        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(
                fixture.service(), "targetUri", "https://service.internal", "/api/%2e%2e/admin", null))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(fixture.httpExchange());
    }

    private Fixture fixture(CpfChannelRegistryPort registryPort) {
        CpfGatewayRouteSnapshot routeSnapshot = mock(CpfGatewayRouteSnapshot.class);
        CpfGatewayRoute route = route();
        when(routeSnapshot.resolve("OACCAC0001")).thenReturn(route);

        CpfServiceCallExecutor serviceCallExecutor = mock(CpfServiceCallExecutor.class);
        CpfGatewayHttpExchangePort httpExchange = mock(CpfGatewayHttpExchangePort.class);
        CpfGatewayAuthenticationPort authenticationPort = (ignored, credentials) -> new CpfGatewayPrincipal(
                true,
                "client-1",
                Set.of("ACC_WRITE"),
                Map.of("authType", "TEST"));
        CpfGatewayAuthorizationPort authorizationPort = (ignored, trusted) -> true;
        CpfGatewayAuditPort auditPort = mock(CpfGatewayAuditPort.class);
        CpfGatewayTransferPolicy transferPolicy = new CpfGatewayTransferPolicy(
                1024 * 1024,
                1024,
                4096,
                1000,
                3000,
                Path.of(System.getProperty("java.io.tmpdir"), "cpf-gateway-test"));

        CpfGatewayProxyService service = new CpfGatewayProxyService(
                routeSnapshot,
                serviceCallExecutor,
                authenticationPort,
                authorizationPort,
                auditPort,
                new CpfChannelPolicyService(registryPort),
                new CpfGatewayRuntimePolicy(),
                transferPolicy,
                httpExchange);
        return new Fixture(service, serviceCallExecutor, httpExchange);
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

    private HttpHeaders channelHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(CpfHeaderNames.ORIGINAL_CHANNEL_CODE, "WEB");
        headers.set(CpfHeaderNames.CHANNEL_CODE, "WEB");
        headers.set(CpfHeaderNames.REQUEST_TYPE, "INQUIRY");
        headers.set(CpfHeaderNames.API_KEY, "masked-test-key");
        return headers;
    }

    private record Fixture(
            CpfGatewayProxyService service,
            CpfServiceCallExecutor serviceCallExecutor,
            CpfGatewayHttpExchangePort httpExchange) {
    }

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
                    "WEB",
                    "웹",
                    "CLIENT",
                    "EXTERNAL",
                    true,
                    false,
                    true,
                    false,
                    true,
                    "테스트",
                    1);
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
                    1,
                    Instant.now(),
                    Map.of("WEB", web),
                    List.of(policy));
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

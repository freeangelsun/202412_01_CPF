package cpf.pfw.gateway.service;

import cpf.pfw.channel.api.CpfChannelRegistryPort;
import cpf.pfw.channel.application.CpfChannelPolicyService;
import cpf.pfw.channel.model.CpfChannelDefinition;
import cpf.pfw.channel.model.CpfChannelExecutionPolicy;
import cpf.pfw.channel.model.CpfChannelPolicySnapshot;
import cpf.pfw.common.gateway.CpfGatewayAuthorizationPort;
import cpf.pfw.common.gateway.CpfGatewayRoute;
import cpf.pfw.common.header.CpfHeaderNames;
import cpf.pfw.common.servicecall.CpfEndpointResolver;
import cpf.pfw.gateway.route.PfwGatewayRouteSnapshot;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PfwGatewayChannelPolicyTest {

    @Test
    void 채널정책거부시대상선택과HTTP호출전에중단한다() {
        PfwGatewayRouteSnapshot routeSnapshot = mock(PfwGatewayRouteSnapshot.class);
        CpfGatewayRoute route = new CpfGatewayRoute(
                "OACCAC0001", "ACC", "POST", "/api/v1/acc/accounts", "accAccountCreate",
                "ACC_WRITE", true, "1");
        when(routeSnapshot.resolve("OACCAC0001")).thenReturn(route);
        CpfEndpointResolver endpointResolver = mock(CpfEndpointResolver.class);
        RestClient restClient = mock(RestClient.class);
        CpfChannelPolicyService policyService = new CpfChannelPolicyService(new DenyPolicyPort());
        PfwGatewayProxyService service = new PfwGatewayProxyService(
                routeSnapshot, endpointResolver, mock(CpfGatewayAuthorizationPort.class), policyService, restClient);
        HttpHeaders headers = new HttpHeaders();
        headers.set(CpfHeaderNames.ORIGINAL_CHANNEL_CODE, "WEB");
        headers.set(CpfHeaderNames.CHANNEL_CODE, "WEB");
        headers.set(CpfHeaderNames.REQUEST_TYPE, "INQUIRY");
        headers.setBearerAuth("masked-test-token");

        assertThatThrownBy(() -> service.execute("OACCAC0001", headers, new byte[0]))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("채널 정책");
        verifyNoInteractions(endpointResolver, restClient);
    }

    private static final class DenyPolicyPort implements CpfChannelRegistryPort {
        @Override
        public CpfChannelPolicySnapshot loadSnapshot() {
            CpfChannelDefinition web = new CpfChannelDefinition(
                    "WEB", "웹", "CLIENT", "EXTERNAL", true, false,
                    true, false, true, "테스트", 1);
            CpfChannelExecutionPolicy deny = new CpfChannelExecutionPolicy(
                    "WEB.DENY", "OACCAC0001", "WEB", "WEB", "INQUIRY",
                    false, true, false, 0, null, null, true, 1);
            return new CpfChannelPolicySnapshot(1, Instant.now(), Map.of("WEB", web), List.of(deny));
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

package com.cpf.gateway.registry;

import com.cpf.gateway.api.CpfGatewayProtocol;
import com.cpf.gateway.api.CpfGatewayRoute;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayBindingKeyHashTest {
    @Test
    void hashesTheCompleteNormalizedRouteMatchKeyDeterministically() {
        CpfGatewayRoute route = route("/very/long/{memberId}/history/**", "GET", "42");

        String first = JdbcCpfGatewayRegistryAdapter.bindingKeyHash(route);
        String second = JdbcCpfGatewayRegistryAdapter.bindingKeyHash(route);

        assertThat(first).hasSize(64).matches("[0-9a-f]{64}").isEqualTo(second);
        assertThat(JdbcCpfGatewayRegistryAdapter.bindingKeyHash(
                route("/very/long/{memberId}/history/**", "GET", "43"))).isNotEqualTo(first);
        assertThat(JdbcCpfGatewayRegistryAdapter.bindingKeyHash(
                route("/very/long/{memberId}/history/*", "GET", "42"))).isNotEqualTo(first);
    }

    @Test
    void normalizesBlankHttpMethodBeforeHashingAndPersistence() {
        CpfGatewayRoute blank = route("/members/**", "  ", "1");
        CpfGatewayRoute wildcard = route("/members/**", "*", "1");

        assertThat(blank.httpMethod()).isEqualTo("*");
        assertThat(JdbcCpfGatewayRegistryAdapter.bindingKeyHash(blank))
                .isEqualTo(JdbcCpfGatewayRegistryAdapter.bindingKeyHash(wildcard));
    }

    private static CpfGatewayRoute route(String path, String method, String version) {
        return new CpfGatewayRoute(
                "execution", "member", method, path, "operation", "permission", false, version,
                "route", "LOCAL", "api.example.test", path, "v1", "group",
                CpfGatewayProtocol.HTTP, CpfGatewayProtocol.HTTP, "", "", "", "", "", "",
                1_000, 2_000, 3_000, 0, false, "", false, 0L);
    }
}

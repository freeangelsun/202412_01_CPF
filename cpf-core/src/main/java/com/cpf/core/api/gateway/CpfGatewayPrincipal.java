package com.cpf.core.api.gateway;

import java.util.Map;
import java.util.Set;

/** Gateway 신뢰경계에서 검증이 끝난 Principal만 표현하는 Public 계약입니다. */
public record CpfGatewayPrincipal(
        boolean authenticated,
        String principalId,
        Set<String> authorities,
        Map<String, String> attributes) {
    public CpfGatewayPrincipal {
        principalId = principalId == null ? "" : principalId;
        authorities = authorities == null ? Set.of() : Set.copyOf(authorities);
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    public static CpfGatewayPrincipal anonymous() {
        return new CpfGatewayPrincipal(false, "", Set.of(), Map.of());
    }
}

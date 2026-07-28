package com.cpf.core.api.gateway;

import java.util.Map;

/** Authorization/API-Key/mTLS 등 실제 credential 검증을 담당하는 Gateway Public SPI입니다. */
@FunctionalInterface
public interface CpfGatewayAuthenticationPort {
    CpfGatewayPrincipal authenticate(CpfGatewayRoute route, Map<String, String> credentialHeaders);
}

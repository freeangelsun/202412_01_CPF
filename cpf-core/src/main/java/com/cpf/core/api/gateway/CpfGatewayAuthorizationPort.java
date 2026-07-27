package com.cpf.core.api.gateway;

import java.util.Map;

/** Gateway 인증/권한 adapter 확장 Public SPI입니다. */
@FunctionalInterface
public interface CpfGatewayAuthorizationPort {
    boolean isAllowed(CpfGatewayRoute route, Map<String, String> trustedHeaders);
}

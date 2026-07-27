package com.cpf.core.api.gateway;

import java.util.Map;

/** Gateway route snapshot 공급 Public SPI입니다. */
public interface CpfGatewayRouteProvider {
    Map<String, CpfGatewayRoute> loadPublicRoutes();
    CpfGatewayRoute resolve(Map<String, CpfGatewayRoute> snapshot, String executionId);
}

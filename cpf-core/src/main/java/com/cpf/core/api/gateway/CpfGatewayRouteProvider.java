package com.cpf.core.api.gateway;

import java.util.Map;

/** Gateway route snapshot 공급 Public SPI입니다. */
public interface CpfGatewayRouteProvider {
    Map<String, CpfGatewayRoute> loadPublicRoutes();

    /** ACK 기록 전 Runtime Instance가 원자적으로 검증할 Candidate Route Snapshot입니다. */
    default Map<String, CpfGatewayRoute> loadCandidateRoutes() {
        return loadPublicRoutes();
    }

    CpfGatewayRoute resolve(Map<String, CpfGatewayRoute> snapshot, String executionId);
}

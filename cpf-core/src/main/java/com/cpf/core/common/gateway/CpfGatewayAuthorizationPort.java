package com.cpf.core.common.gateway;

import java.util.Map;

/** Gateway 인증 adapter가 실행 권한을 판정하는 확장점입니다. */
@FunctionalInterface
public interface CpfGatewayAuthorizationPort {
    boolean isAllowed(CpfGatewayRoute route, Map<String, String> trustedHeaders);
}

package com.cpf.integration.realtime;

import java.security.Principal;

/** Realtime 구독 권한 검증 SPI. 인증은 상위 Web/Security chain이 담당하고 여기서는 resource/action 권한을 검증합니다. */
@FunctionalInterface
public interface CpfRealtimeAuthorization {
    boolean canSubscribe(Principal principal, String tenantId, String channel, String topic, String subjectId);

    static CpfRealtimeAuthorization authenticatedPrincipal() {
        return (principal, tenant, channel, topic, subject) -> principal != null;
    }
}

package com.cpf.core.api.gateway;

/** Probe, 실호출, 수동 상태를 합성한 운영 상태입니다. */
public enum CpfGatewayHealthStatus {
    UP, DEGRADED, RECOVERING, DOWN, DRAINING, MAINTENANCE, DISABLED, UNKNOWN, STALE;

    /** 신규 Traffic을 받을 수 있는 상태인지 반환합니다. */
    public boolean routable() {
        return this == UP || this == DEGRADED || this == RECOVERING;
    }
}

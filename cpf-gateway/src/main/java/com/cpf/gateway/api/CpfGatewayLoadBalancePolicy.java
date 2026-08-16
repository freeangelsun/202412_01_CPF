package com.cpf.gateway.api;

/** SLB가 없는 Server Group에서 Gateway/Service Call이 공통으로 사용하는 선택 정책입니다. */
public enum CpfGatewayLoadBalancePolicy {
    ROUND_ROBIN, WEIGHTED_ROUND_ROBIN, RENDEZVOUS_HASH, PRIORITY_FAILOVER, LEAST_LOAD
}

package com.cpf.gateway.api;

/** Gateway 입·출력과 Health Probe에서 사용하는 표준 Protocol입니다. */
public enum CpfGatewayProtocol {
    HTTP, HTTPS, GRPC, WEBSOCKET, SSE, TCP;

    public boolean tls() { return this == HTTPS || this == GRPC; }
    public boolean streaming() { return this == GRPC || this == WEBSOCKET || this == SSE || this == TCP; }
}

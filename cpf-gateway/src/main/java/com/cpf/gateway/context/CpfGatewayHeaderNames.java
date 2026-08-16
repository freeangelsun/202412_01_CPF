package com.cpf.gateway.context;

/** Gateway-owned headers. General HTTP context headers are defined by the Web profile. */
public final class CpfGatewayHeaderNames {
    private CpfGatewayHeaderNames() {}
    public static final String GATEWAY_TRANSACTION_ID = "X-Cpf-Gateway-Transaction-Id";
    public static final String GATEWAY_INSTANCE_ID = "X-Cpf-Gateway-Instance-Id";
    public static final String GATEWAY_ROUTE_ID = "X-Cpf-Gateway-Route-Id";
    public static final String GATEWAY_ROUTE_VERSION = "X-Cpf-Gateway-Route-Version";
    public static final String INGRESS_TYPE = "X-Cpf-Ingress-Type";
    public static final String REQUEST_SIGNATURE = "X-Request-Signature";
}

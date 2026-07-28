package com.cpf.core.api.header;

/** 외부 Module이 사용할 CPF 표준 HTTP Header Public API입니다. */
public final class CpfHeaderNames {
    public static final String STANDARD_EXECUTION_ID = "X-Cpf-Standard-Execution-Id";
    public static final String ORIGINAL_CHANNEL_CODE = "X-Original-Channel-Code";
    public static final String CHANNEL_CODE = "X-Channel-Code";
    public static final String REQUEST_TYPE = "X-Request-Type";
    public static final String GATEWAY_INSTANCE_ID = "X-Cpf-Gateway-Instance-Id";
    public static final String GATEWAY_ROUTE_ID = "X-Cpf-Gateway-Route-Id";
    public static final String GATEWAY_ROUTE_VERSION = "X-Cpf-Gateway-Route-Version";
    public static final String INGRESS_TYPE = "X-Cpf-Ingress-Type";
    public static final String AUTHORIZATION = "Authorization";
    public static final String API_KEY = "X-Api-Key";
    public static final String REQUEST_SIGNATURE = "X-Request-Signature";
    public static final String AUDIT_REASON = "X-Cpf-Audit-Reason";
    private CpfHeaderNames() {}
}

package com.cpf.core.api.gateway;

/** ADM과 Gateway Owner 사이 Control Plane 호출에 사용하는 서명 Header 정본입니다. */
public final class CpfGatewayControlHeaders {
    public static final String CALLER_SERVICE = "X-CPF-Caller-Service";
    public static final String OPERATOR_ID = "X-CPF-Operator-Id";
    public static final String TIMESTAMP = "X-CPF-Gateway-Control-Timestamp";
    public static final String NONCE = "X-CPF-Gateway-Control-Nonce";
    public static final String CONTENT_SHA256 = "X-CPF-Gateway-Control-Content-SHA256";
    public static final String AUDIENCE = "X-CPF-Gateway-Control-Audience";
    public static final String KEY_ID = "X-CPF-Gateway-Control-Key-Id";
    public static final String SIGNATURE = "X-CPF-Gateway-Control-Signature";

    private CpfGatewayControlHeaders() {
    }
}

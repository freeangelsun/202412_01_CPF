package com.cpf.web.context;

/** HTTP/Web-only request context. Raw credentials and untrusted security assertions are never stored here. */
public record CpfWebContext(
        String requestId,
        String externalRequestId,
        String apiVersion,
        String countryCode,
        String clientId,
        String clientInstanceId,
        String clientVersion,
        String deviceId,
        String locale,
        String resolvedClientIp,
        String userAgent,
        String traceparent,
        String tracestate,
        CpfHttpIngressTrust trustLevel) {
    public CpfWebContext {
        if (trustLevel == null) trustLevel = CpfHttpIngressTrust.UNTRUSTED_EXTERNAL;
    }

}

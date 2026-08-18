package com.cpf.web.context;

import com.cpf.foundation.context.header.CpfHeaderNames;

/**
 * CPF HTTP Header wire-name canonical catalog.
 *
 * <p>The six transaction headers below are the only CPF-owned headers that define the online
 * domain-to-domain protocol. Runtime-known application/instance/provider metadata is not copied to
 * HTTP headers. Standard HTTP/W3C headers keep their standard names.</p>
 */
public final class CpfHttpHeaderNames {
    private CpfHttpHeaderNames() {}

    // Canonical online transaction protocol (mandatory on trusted internal CPF hops).
    public static final String TRANSACTION_ID = CpfHeaderNames.TRANSACTION_ID;
    public static final String ORIGINAL_CHANNEL = CpfHeaderNames.ORIGINAL_CHANNEL;
    public static final String CURRENT_CHANNEL = CpfHeaderNames.CURRENT_CHANNEL;
    public static final String CALLER_CHANNEL = CpfHeaderNames.CALLER_CHANNEL;
    public static final String TARGET_CHANNEL = CpfHeaderNames.TARGET_CHANNEL;
    public static final String TARGET_OPERATION_ID = CpfHeaderNames.TARGET_OPERATION_ID;

    // Optional client/application context. Values are never a security authority by themselves.
    public static final String COUNTRY_CODE = "X-Country-Code";
    public static final String CLIENT_ID = "X-Client-Id";
    public static final String CLIENT_INSTANCE_ID = "X-Client-Instance-Id";
    public static final String CLIENT_VERSION = "X-Client-Version";
    public static final String DEVICE_ID = "X-Device-Id";
    // Optional subject tracking metadata. Not part of the mandatory transaction-header contract.
    public static final String SUBJECT_TYPE = "X-Subject-Type";
    public static final String SUBJECT_ID = "X-Subject-Id";

    // Existing non-canonical request semantics retained as bounded compatibility/control metadata.
    public static final String BUSINESS_DATE = "X-Cpf-Business-Date";
    public static final String PROTOCOL_VERSION = "X-Cpf-Protocol-Version";
    public static final String IDEMPOTENCY_KEY = "Idempotency-Key";
    public static final String IDEMPOTENCY_LEGACY = "X-Idempotency-Key";
    public static final String API_VERSION = "X-Api-Version";
    public static final String REQUEST_TYPE = "X-Request-Type";
    public static final String REQUEST_ID = "X-Request-Id";
    public static final String CORRELATION_ID = "X-Correlation-Id";
    public static final String SCREEN_ID = "X-Screen-Id";
    public static final String OPERATOR_ID = "X-Operator-Id";
    public static final String TENANT_ID = "X-Tenant-Id";
    public static final String AUDIT_REASON = "X-Cpf-Audit-Reason";

    // Standard HTTP / W3C headers. Do not introduce CPF aliases for these.
    public static final String USER_AGENT = "User-Agent";
    public static final String ACCEPT_LANGUAGE = "Accept-Language";
    public static final String AUTHORIZATION = "Authorization";
    public static final String API_KEY = "X-Api-Key";
    public static final String FORWARDED = "Forwarded";
    public static final String X_FORWARDED_FOR = "X-Forwarded-For";
    public static final String X_FORWARDED_HOST = "X-Forwarded-Host";
    public static final String X_FORWARDED_PROTO = "X-Forwarded-Proto";
    public static final String TRACEPARENT = "traceparent";
    public static final String TRACESTATE = "tracestate";

}

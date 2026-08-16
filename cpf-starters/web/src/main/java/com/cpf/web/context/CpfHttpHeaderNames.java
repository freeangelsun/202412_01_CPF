package com.cpf.web.context;

/**
 * CPF HTTP 표준 Header wire name 정본입니다.
 *
 * <p>Wire name은 외부 시스템·Gateway·Frontend·운영 Script까지 영향도가 크므로 기존 계약을 유지합니다.
 * 개발자는 긴 literal을 직접 쓰지 않고 {@code CpfHeaders}의 짧은 API(txId/execId/caller/target 등)를 사용합니다.
 * 신규 내부 호출자/대상 Header만 CPF 표준 prefix로 추가합니다.</p>
 */
public final class CpfHttpHeaderNames {
    private CpfHttpHeaderNames() {}

    // 거래/실행 추적: 기존 wire 호환을 유지합니다.
    public static final String TRANSACTION_ID = "X-Transaction-Id";
    public static final String ROOT_TRANSACTION_ID = "X-Cpf-Root-Transaction-Id";
    public static final String BUSINESS_DATE = "X-Cpf-Business-Date";
    public static final String EXECUTION_ID = "X-Cpf-Execution-Id";
    public static final String ROOT_EXECUTION_ID = "X-Cpf-Root-Execution-Id";
    public static final String PARENT_EXECUTION_ID = "X-Cpf-Parent-Execution-Id";
    public static final String SEGMENT_ID = "X-Transaction-Segment-Id";
    public static final String PARENT_SEGMENT_ID = "X-Parent-Transaction-Segment-Id";
    public static final String STANDARD_EXECUTION_ID = "X-Cpf-Standard-Execution-Id";
    public static final String REQUEST_ID = "X-Request-Id";
    public static final String CORRELATION_ID = "X-Correlation-Id";

    // 내부 서비스 간 라우팅/감사에 필요한 신규 표준값입니다.
    public static final String CALLER = "X-Cpf-Caller";
    public static final String TARGET = "X-Cpf-Target";

    // 거래 정책/업무 문맥
    public static final String PROTOCOL_VERSION = "X-Cpf-Protocol-Version";
    public static final String IDEMPOTENCY_KEY = "Idempotency-Key";
    public static final String IDEMPOTENCY_LEGACY = "X-Idempotency-Key";
    public static final String API_VERSION = "X-Api-Version";
    public static final String ORIGINAL_CHANNEL_CODE = "X-Original-Channel-Code";
    public static final String CHANNEL_CODE = "X-Channel-Code";
    public static final String REQUEST_TYPE = "X-Request-Type";
    public static final String CLIENT_APP = "X-Client-App";
    public static final String CLIENT_VERSION = "X-Client-Version";
    public static final String SCREEN_ID = "X-Screen-Id";
    public static final String DEVICE_ID = "X-Device-Id";
    public static final String LOCALE = "X-Locale";
    public static final String CLIENT_TIMEZONE = "X-Client-Timezone";
    public static final String CLIENT_IP = "X-Client-IP";
    public static final String USER_AGENT = "User-Agent";
    public static final String USER_ID = "X-User-Id";
    public static final String OPERATOR_ID = "X-Operator-Id";
    public static final String TENANT_ID = "X-Tenant-Id";
    public static final String AUDIT_REASON = "X-Cpf-Audit-Reason";

    // W3C/보안 표준은 CPF 이름으로 다시 감싸지 않습니다.
    public static final String TRACEPARENT = "traceparent";
    public static final String TRACESTATE = "tracestate";
    public static final String AUTHORIZATION = "Authorization";
    public static final String API_KEY = "X-Api-Key";
    public static final String FORWARDED = "Forwarded";
    public static final String X_FORWARDED_FOR = "X-Forwarded-For";
    public static final String X_FORWARDED_HOST = "X-Forwarded-Host";
    public static final String X_FORWARDED_PROTO = "X-Forwarded-Proto";

    // 명시적 compatibility alias. 신규 코드는 IDEMPOTENCY_KEY를 사용합니다.
    public static final String LEGACY_TRANSACTION_ID = TRANSACTION_ID;
    public static final String LEGACY_ROOT_TRANSACTION_ID = ROOT_TRANSACTION_ID;
    public static final String LEGACY_EXECUTION_ID = EXECUTION_ID;
    public static final String LEGACY_ROOT_EXECUTION_ID = ROOT_EXECUTION_ID;
    public static final String LEGACY_PARENT_EXECUTION_ID = PARENT_EXECUTION_ID;
    public static final String LEGACY_SEGMENT_ID = SEGMENT_ID;
    public static final String LEGACY_PARENT_SEGMENT_ID = PARENT_SEGMENT_ID;
    public static final String LEGACY_STANDARD_EXECUTION_ID = STANDARD_EXECUTION_ID;
    public static final String LEGACY_REQUEST_ID = REQUEST_ID;
    public static final String LEGACY_CORRELATION_ID = CORRELATION_ID;
    public static final String LEGACY_IDEMPOTENCY_KEY = IDEMPOTENCY_LEGACY;
}

package cpf.pfw.common.header;

/**
 * CPF 온라인 거래에서 사용하는 표준 HTTP 헤더명입니다.
 *
 * <p>업무 코드, 로그 저장, Swagger 문서, EDU 샘플, 하위 호출 전파는 이 상수를 기준으로 작성합니다.
 * 헤더명을 문자열로 직접 반복 작성하면 문서와 구현이 어긋나기 쉬우므로 신규 코드는 반드시 이 상수를 사용합니다.</p>
 */
public final class CpfHeaderNames {
    public static final String TRANSACTION_ID = "X-Transaction-Id";
    public static final String STANDARD_EXECUTION_ID = "X-Cpf-Standard-Execution-Id";
    public static final String PROTOCOL_VERSION = "X-Cpf-Protocol-Version";
    public static final String PARENT_TRANSACTION_ID = "X-Parent-Transaction-Id";
    public static final String ORIGINAL_TRANSACTION_ID = "X-Original-Transaction-Id";
    public static final String ROOT_TRANSACTION_ID = "X-Root-Transaction-Id";
    public static final String TRANSACTION_SEGMENT_ID = "X-Transaction-Segment-Id";
    public static final String PARENT_TRANSACTION_SEGMENT_ID = "X-Parent-Transaction-Segment-Id";
    public static final String TRANSACTION_CALL_DEPTH = "X-Transaction-Call-Depth";
    public static final String REQUEST_ID = "X-Request-Id";
    public static final String EXTERNAL_REQUEST_ID = "X-External-Request-Id";
    public static final String CORRELATION_ID = "X-Correlation-Id";
    public static final String IDEMPOTENCY_KEY = "X-Idempotency-Key";
    public static final String IDEMPOTENCY_KEY_ALIAS = "Idempotency-Key";

    public static final String TRACE_ID = "X-Trace-Id";
    public static final String SPAN_ID = "X-Span-Id";
    public static final String PARENT_SPAN_ID = "X-Parent-Span-Id";
    public static final String TRACEPARENT = "traceparent";
    public static final String TRACESTATE = "tracestate";

    public static final String API_VERSION = "X-Api-Version";
    public static final String REQUEST_TYPE = "X-Request-Type";
    public static final String ORIGINAL_CHANNEL_CODE = "X-Original-Channel-Code";
    public static final String CHANNEL_CODE = "X-Channel-Code";
    public static final String CHANNEL_DETAIL_CODE = "X-Channel-Detail-Code";

    public static final String USER_ID = "X-User-Id";
    public static final String OPERATOR_ID = "X-Operator-Id";
    public static final String CUSTOMER_NO = "X-Customer-No";
    public static final String MEMBER_NO = "X-Member-No";
    public static final String TENANT_ID = "X-Tenant-Id";
    public static final String ORGANIZATION_CODE = "X-Organization-Code";
    public static final String BRANCH_CODE = "X-Branch-Code";

    public static final String CLIENT_IP = "X-Client-IP";
    public static final String FORWARDED_FOR = "X-Forwarded-For";
    public static final String FORWARDED = "Forwarded";
    public static final String REAL_IP = "X-Real-IP";
    public static final String CLIENT_COUNTRY_CODE = "X-Client-Country-Code";
    public static final String CLIENT_REGION_CODE = "X-Client-Region-Code";
    public static final String TIMEZONE = "X-Timezone";
    public static final String CLIENT_TIMEZONE = "X-Client-Timezone";

    public static final String CLIENT_APP_ID = "X-Client-App-Id";
    public static final String CLIENT_VERSION = "X-Client-Version";
    public static final String CALLER_SERVICE = "X-Caller-Service";
    public static final String CALLER_INSTANCE_ID = "X-Caller-Instance-Id";
    public static final String GATEWAY_INSTANCE_ID = "X-Cpf-Gateway-Instance-Id";
    public static final String GATEWAY_ROUTE_ID = "X-Cpf-Gateway-Route-Id";
    public static final String GATEWAY_ROUTE_VERSION = "X-Cpf-Gateway-Route-Version";
    public static final String INGRESS_TYPE = "X-Cpf-Ingress-Type";
    public static final String USER_AGENT = "User-Agent";
    public static final String LOCALE = "X-Locale";
    public static final String SCREEN_ID = "X-Screen-Id";
    public static final String DEVICE_ID = "X-Device-Id";
    public static final String CLIENT_REQUEST_TIME = "X-Client-Request-Time";

    public static final String AUTHORIZATION = "Authorization";
    public static final String API_KEY = "X-Api-Key";
    public static final String REQUEST_SIGNATURE = "X-Request-Signature";
    public static final String REQUEST_TIMESTAMP = "X-Request-Timestamp";
    public static final String NONCE = "X-Nonce";

    public static final String RESERVED_FIELD_1 = "X-Reserved-Field-1";
    public static final String RESERVED_FIELD_2 = "X-Reserved-Field-2";
    public static final String RESERVED_FIELD_3 = "X-Reserved-Field-3";
    public static final String RESERVED_FIELD_4 = "X-Reserved-Field-4";
    public static final String RESERVED_FIELD_5 = "X-Reserved-Field-5";

    public static final String EXTENSION_1 = "X-Cpf-Ext-1";
    public static final String EXTENSION_2 = "X-Cpf-Ext-2";
    public static final String EXTENSION_3 = "X-Cpf-Ext-3";
    public static final String EXTENSION_4 = "X-Cpf-Ext-4";
    public static final String EXTENSION_5 = "X-Cpf-Ext-5";

    private CpfHeaderNames() {
    }
}

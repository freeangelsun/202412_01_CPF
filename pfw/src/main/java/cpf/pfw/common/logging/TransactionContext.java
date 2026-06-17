package cpf.pfw.common.logging;

import org.slf4j.MDC;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public final class TransactionContext {

    public static final String HEADER_TRANSACTION_ID = "X-Transaction-Id";
    public static final String HEADER_TRACE_ID = "X-Trace-Id";
    public static final String HEADER_SPAN_ID = "X-Span-Id";
    public static final String HEADER_PARENT_SPAN_ID = "X-Parent-Span-Id";
    public static final String HEADER_API_VERSION = "X-Api-Version";
    public static final String HEADER_CLIENT_APP_ID = "X-Client-App-Id";
    public static final String HEADER_CLIENT_VERSION = "X-Client-Version";
    public static final String HEADER_CALLER_SERVICE = "X-Caller-Service";
    public static final String HEADER_CALLER_INSTANCE_ID = "X-Caller-Instance-Id";
    public static final String HEADER_CORRELATION_ID = "X-Correlation-Id";
    public static final String HEADER_IDEMPOTENCY_KEY = "X-Idempotency-Key";
    public static final String HEADER_LOCALE = "X-Locale";
    public static final String HEADER_TIMEZONE = "X-Timezone";
    public static final String HEADER_REQUEST_TYPE = "X-Request-Type";
    public static final String HEADER_ORIGINAL_CHANNEL_CODE = "X-Original-Channel-Code";
    public static final String HEADER_CHANNEL_CODE = "X-Channel-Code";
    public static final String HEADER_MEMBER_NO = "X-Member-No";
    public static final String HEADER_CUSTOMER_NO = "X-Customer-No";
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_SCREEN_ID = "X-Screen-Id";
    public static final String HEADER_DEVICE_ID = "X-Device-Id";
    public static final String HEADER_CLIENT_REQUEST_TIME = "X-Client-Request-Time";
    public static final String HEADER_CLIENT_IP = "X-Client-IP";
    public static final String HEADER_RESERVED_FIELD_1 = "X-Reserved-Field-1";
    public static final String HEADER_RESERVED_FIELD_2 = "X-Reserved-Field-2";
    public static final String HEADER_RESERVED_FIELD_3 = "X-Reserved-Field-3";
    public static final String HEADER_RESERVED_FIELD_4 = "X-Reserved-Field-4";
    public static final String HEADER_RESERVED_FIELD_5 = "X-Reserved-Field-5";

    public static final String ATTR_TRANSACTION_ID = "transactionId";
    public static final String ATTR_TRACE_ID = "traceId";
    public static final String ATTR_SPAN_ID = "spanId";
    public static final String ATTR_PARENT_SPAN_ID = "parentSpanId";
    public static final String ATTR_SEQUENCE_NO = "transactionSequenceNo";
    public static final String ATTR_HEADER = "transactionHeader";

    private static final String MDC_TRANSACTION_ID = "transactionId";
    private static final String MDC_TRACE_ID = "traceId";
    private static final String MDC_SPAN_ID = "spanId";
    private static final String MDC_BUSINESS_TRANSACTION_ID = "businessTransactionId";
    private static final String MDC_BUSINESS_TRANSACTION_NAME = "businessTransactionName";
    private static final String MDC_DYNAMIC_LOG_LEVEL = "dynamicLogLevel";
    private static final DateTimeFormatter FALLBACK_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final AtomicLong FALLBACK_SEQUENCE = new AtomicLong();

    private TransactionContext() {
    }

    public static void initialize(String transactionId, String traceId, String parentSpanId) {
        initialize(transactionId, traceId, parentSpanId, generateFallbackTransactionId(), null);
    }

    public static void initialize(String transactionId, String traceId, String parentSpanId, String generatedTransactionId) {
        initialize(transactionId, traceId, parentSpanId, generatedTransactionId, null);
    }

    public static void initialize(
            String transactionId,
            String traceId,
            String parentSpanId,
            String generatedTransactionId,
            TransactionHeader transactionHeader) {

        String resolvedTransactionId = hasText(transactionId) ? transactionId : generatedTransactionId;
        String resolvedTraceId = hasText(traceId) ? traceId : UUID.randomUUID().toString();
        String resolvedSpanId = generateSpanId();

        setAttribute(ATTR_TRANSACTION_ID, resolvedTransactionId);
        setAttribute(ATTR_TRACE_ID, resolvedTraceId);
        setAttribute(ATTR_SPAN_ID, resolvedSpanId);
        setAttribute(ATTR_PARENT_SPAN_ID, hasText(parentSpanId) ? parentSpanId : null);
        setAttribute(ATTR_SEQUENCE_NO, 0);
        setAttribute(ATTR_HEADER, transactionHeader);

        putMdc(resolvedTransactionId, resolvedTraceId, resolvedSpanId);
    }

    public static String getOrCreateTransactionId() {
        String transactionId = currentTransactionId();
        if (hasText(transactionId)) {
            return transactionId;
        }

        String generated = generateFallbackTransactionId();
        setAttribute(ATTR_TRANSACTION_ID, generated);
        MDC.put(MDC_TRANSACTION_ID, generated);
        return generated;
    }

    public static String getOrCreateTraceId() {
        String traceId = currentTraceId();
        if (hasText(traceId)) {
            return traceId;
        }

        String generated = UUID.randomUUID().toString();
        setAttribute(ATTR_TRACE_ID, generated);
        MDC.put(MDC_TRACE_ID, generated);
        return generated;
    }

    public static String getOrCreateSpanId() {
        String spanId = currentSpanId();
        if (hasText(spanId)) {
            return spanId;
        }

        String generated = generateSpanId();
        setAttribute(ATTR_SPAN_ID, generated);
        MDC.put(MDC_SPAN_ID, generated);
        return generated;
    }

    public static String currentTransactionId() {
        return firstText(getAttributeAsString(ATTR_TRANSACTION_ID), MDC.get(MDC_TRANSACTION_ID));
    }

    public static String currentTraceId() {
        return firstText(getAttributeAsString(ATTR_TRACE_ID), MDC.get(MDC_TRACE_ID));
    }

    public static String currentSpanId() {
        return firstText(getAttributeAsString(ATTR_SPAN_ID), MDC.get(MDC_SPAN_ID));
    }

    public static String currentParentSpanId() {
        return getAttributeAsString(ATTR_PARENT_SPAN_ID);
    }

    public static TransactionHeader currentHeader() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }

        Object value = attributes.getAttribute(ATTR_HEADER, RequestAttributes.SCOPE_REQUEST);
        return value instanceof TransactionHeader transactionHeader ? transactionHeader : null;
    }

    public static int nextSequenceNo() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return 1;
        }

        Object value = attributes.getAttribute(ATTR_SEQUENCE_NO, RequestAttributes.SCOPE_REQUEST);
        int next = value instanceof Number number ? number.intValue() + 1 : 1;
        attributes.setAttribute(ATTR_SEQUENCE_NO, next, RequestAttributes.SCOPE_REQUEST);
        return next;
    }

    public static Map<String, String> propagationHeaders() {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put(HEADER_TRANSACTION_ID, getOrCreateTransactionId());
        headers.put(HEADER_TRACE_ID, getOrCreateTraceId());
        headers.put(HEADER_PARENT_SPAN_ID, getOrCreateSpanId());

        TransactionHeader transactionHeader = currentHeader();
        if (transactionHeader != null) {
            putIfHasText(headers, HEADER_API_VERSION, transactionHeader.getApiVersion());
            putIfHasText(headers, HEADER_CLIENT_APP_ID, transactionHeader.getClientAppId());
            putIfHasText(headers, HEADER_CLIENT_VERSION, transactionHeader.getClientVersion());
            putIfHasText(headers, HEADER_CALLER_SERVICE, transactionHeader.getCallerService());
            putIfHasText(headers, HEADER_CALLER_INSTANCE_ID, transactionHeader.getCallerInstanceId());
            putIfHasText(headers, HEADER_CORRELATION_ID, transactionHeader.getCorrelationId());
            putIfHasText(headers, HEADER_IDEMPOTENCY_KEY, transactionHeader.getIdempotencyKey());
            putIfHasText(headers, HEADER_LOCALE, transactionHeader.getLocale());
            putIfHasText(headers, HEADER_TIMEZONE, transactionHeader.getTimezone());
            putIfHasText(headers, HEADER_REQUEST_TYPE, transactionHeader.getRequestType());
            putIfHasText(headers, HEADER_ORIGINAL_CHANNEL_CODE, transactionHeader.getOriginalChannelCode());
            putIfHasText(headers, HEADER_CHANNEL_CODE, transactionHeader.getChannelCode());
            putIfHasText(headers, HEADER_MEMBER_NO, transactionHeader.getMemberNo());
            putIfHasText(headers, HEADER_CUSTOMER_NO, transactionHeader.getCustomerNo());
            putIfHasText(headers, HEADER_USER_ID, transactionHeader.getUserId());
            putIfHasText(headers, HEADER_SCREEN_ID, transactionHeader.getScreenId());
            putIfHasText(headers, HEADER_DEVICE_ID, transactionHeader.getDeviceId());
            putIfHasText(headers, HEADER_CLIENT_REQUEST_TIME, transactionHeader.getClientRequestTime());
            putIfHasText(headers, HEADER_CLIENT_IP, transactionHeader.getClientIp());
            putIfHasText(headers, HEADER_RESERVED_FIELD_1, transactionHeader.getReservedField1());
            putIfHasText(headers, HEADER_RESERVED_FIELD_2, transactionHeader.getReservedField2());
            putIfHasText(headers, HEADER_RESERVED_FIELD_3, transactionHeader.getReservedField3());
            putIfHasText(headers, HEADER_RESERVED_FIELD_4, transactionHeader.getReservedField4());
            putIfHasText(headers, HEADER_RESERVED_FIELD_5, transactionHeader.getReservedField5());
        }
        return headers;
    }

    public static void clear() {
        MDC.remove(MDC_TRANSACTION_ID);
        MDC.remove(MDC_TRACE_ID);
        MDC.remove(MDC_SPAN_ID);
        MDC.remove(MDC_BUSINESS_TRANSACTION_ID);
        MDC.remove(MDC_BUSINESS_TRANSACTION_NAME);
        MDC.remove(MDC_DYNAMIC_LOG_LEVEL);
    }

    /**
     * 업무 거래 ID와 거래명을 MDC에 저장합니다.
     *
     * <p>로그 패턴과 APM 연동에서 동일한 값을 사용할 수 있도록
     * {@code LoggingAspect}가 {@code @FpsTransaction} 정보를 해석한 뒤 호출합니다.</p>
     *
     * @param businessTransactionId   업무 거래 ID
     * @param businessTransactionName 업무 거래명
     */
    public static void putBusinessTransaction(String businessTransactionId, String businessTransactionName) {
        putIfHasText(MDC_BUSINESS_TRANSACTION_ID, businessTransactionId);
        putIfHasText(MDC_BUSINESS_TRANSACTION_NAME, businessTransactionName);
    }

    /**
     * 동적 로그 레벨 규칙으로 결정된 로그 레벨을 MDC에 저장합니다.
     *
     * @param logLevel 로그 레벨 문자열
     */
    public static void putDynamicLogLevel(String logLevel) {
        putIfHasText(MDC_DYNAMIC_LOG_LEVEL, logLevel);
    }

    private static void putMdc(String transactionId, String traceId, String spanId) {
        MDC.put(MDC_TRANSACTION_ID, transactionId);
        MDC.put(MDC_TRACE_ID, traceId);
        MDC.put(MDC_SPAN_ID, spanId);
    }

    private static void putIfHasText(String key, String value) {
        if (hasText(value)) {
            MDC.put(key, value);
        }
    }

    private static void putIfHasText(Map<String, String> headers, String name, String value) {
        if (hasText(value)) {
            headers.put(name, value);
        }
    }

    private static void setAttribute(String name, Object value) {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            attributes.setAttribute(name, value, RequestAttributes.SCOPE_REQUEST);
        }
    }

    private static String getAttributeAsString(String name) {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }

        Object value = attributes.getAttribute(name, RequestAttributes.SCOPE_REQUEST);
        return value != null ? value.toString() : null;
    }

    private static String firstText(String first, String second) {
        return hasText(first) ? first : (hasText(second) ? second : null);
    }

    private static String generateFallbackTransactionId() {
        long sequence = FALLBACK_SEQUENCE.updateAndGet(value -> value >= 9_999_999L ? 1L : value + 1L);
        return LocalDateTime.now().format(FALLBACK_FORMAT) + "PFWlocal01" + String.format("%07d", sequence);
    }

    private static String generateSpanId() {
        return "SPN-" + UUID.randomUUID();
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

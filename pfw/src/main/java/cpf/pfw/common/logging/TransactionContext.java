package cpf.pfw.common.logging;

import cpf.pfw.common.header.CpfHeaderNames;
import cpf.pfw.common.header.CpfHeaderPropagator;
import cpf.pfw.common.logging.segment.TransactionSegmentContext;
import org.slf4j.MDC;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public final class TransactionContext {

    public static final String HEADER_TRANSACTION_ID = CpfHeaderNames.TRANSACTION_ID;
    public static final String HEADER_PARENT_TRANSACTION_ID = CpfHeaderNames.PARENT_TRANSACTION_ID;
    public static final String HEADER_ORIGINAL_TRANSACTION_ID = CpfHeaderNames.ORIGINAL_TRANSACTION_ID;
    public static final String HEADER_ROOT_TRANSACTION_ID = CpfHeaderNames.ROOT_TRANSACTION_ID;
    public static final String HEADER_TRANSACTION_SEGMENT_ID = CpfHeaderNames.TRANSACTION_SEGMENT_ID;
    public static final String HEADER_PARENT_TRANSACTION_SEGMENT_ID = CpfHeaderNames.PARENT_TRANSACTION_SEGMENT_ID;
    public static final String HEADER_TRANSACTION_CALL_DEPTH = CpfHeaderNames.TRANSACTION_CALL_DEPTH;
    public static final String HEADER_REQUEST_ID = CpfHeaderNames.REQUEST_ID;
    public static final String HEADER_EXTERNAL_REQUEST_ID = CpfHeaderNames.EXTERNAL_REQUEST_ID;
    public static final String HEADER_TRACE_ID = CpfHeaderNames.TRACE_ID;
    public static final String HEADER_SPAN_ID = CpfHeaderNames.SPAN_ID;
    public static final String HEADER_PARENT_SPAN_ID = CpfHeaderNames.PARENT_SPAN_ID;
    public static final String HEADER_TRACEPARENT = CpfHeaderNames.TRACEPARENT;
    public static final String HEADER_TRACESTATE = CpfHeaderNames.TRACESTATE;
    public static final String HEADER_API_VERSION = CpfHeaderNames.API_VERSION;
    public static final String HEADER_CLIENT_APP_ID = CpfHeaderNames.CLIENT_APP_ID;
    public static final String HEADER_CLIENT_VERSION = CpfHeaderNames.CLIENT_VERSION;
    public static final String HEADER_CALLER_SERVICE = CpfHeaderNames.CALLER_SERVICE;
    public static final String HEADER_CALLER_INSTANCE_ID = CpfHeaderNames.CALLER_INSTANCE_ID;
    public static final String HEADER_CORRELATION_ID = CpfHeaderNames.CORRELATION_ID;
    public static final String HEADER_IDEMPOTENCY_KEY = CpfHeaderNames.IDEMPOTENCY_KEY;
    public static final String HEADER_IDEMPOTENCY_KEY_ALIAS = CpfHeaderNames.IDEMPOTENCY_KEY_ALIAS;
    public static final String HEADER_LOCALE = CpfHeaderNames.LOCALE;
    public static final String HEADER_TIMEZONE = CpfHeaderNames.TIMEZONE;
    public static final String HEADER_REQUEST_TYPE = CpfHeaderNames.REQUEST_TYPE;
    public static final String HEADER_ORIGINAL_CHANNEL_CODE = CpfHeaderNames.ORIGINAL_CHANNEL_CODE;
    public static final String HEADER_CHANNEL_CODE = CpfHeaderNames.CHANNEL_CODE;
    public static final String HEADER_CHANNEL_DETAIL_CODE = CpfHeaderNames.CHANNEL_DETAIL_CODE;
    public static final String HEADER_MEMBER_NO = CpfHeaderNames.MEMBER_NO;
    public static final String HEADER_CUSTOMER_NO = CpfHeaderNames.CUSTOMER_NO;
    public static final String HEADER_USER_ID = CpfHeaderNames.USER_ID;
    public static final String HEADER_OPERATOR_ID = CpfHeaderNames.OPERATOR_ID;
    public static final String HEADER_TENANT_ID = CpfHeaderNames.TENANT_ID;
    public static final String HEADER_ORGANIZATION_CODE = CpfHeaderNames.ORGANIZATION_CODE;
    public static final String HEADER_BRANCH_CODE = CpfHeaderNames.BRANCH_CODE;
    public static final String HEADER_SCREEN_ID = CpfHeaderNames.SCREEN_ID;
    public static final String HEADER_DEVICE_ID = CpfHeaderNames.DEVICE_ID;
    public static final String HEADER_CLIENT_REQUEST_TIME = CpfHeaderNames.CLIENT_REQUEST_TIME;
    public static final String HEADER_CLIENT_IP = CpfHeaderNames.CLIENT_IP;
    public static final String HEADER_FORWARDED_FOR = CpfHeaderNames.FORWARDED_FOR;
    public static final String HEADER_FORWARDED = CpfHeaderNames.FORWARDED;
    public static final String HEADER_REAL_IP = CpfHeaderNames.REAL_IP;
    public static final String HEADER_CLIENT_COUNTRY_CODE = CpfHeaderNames.CLIENT_COUNTRY_CODE;
    public static final String HEADER_CLIENT_REGION_CODE = CpfHeaderNames.CLIENT_REGION_CODE;
    public static final String HEADER_CLIENT_TIMEZONE = CpfHeaderNames.CLIENT_TIMEZONE;
    public static final String HEADER_USER_AGENT = CpfHeaderNames.USER_AGENT;
    public static final String HEADER_REQUEST_TIMESTAMP = CpfHeaderNames.REQUEST_TIMESTAMP;
    public static final String HEADER_RESERVED_FIELD_1 = CpfHeaderNames.RESERVED_FIELD_1;
    public static final String HEADER_RESERVED_FIELD_2 = CpfHeaderNames.RESERVED_FIELD_2;
    public static final String HEADER_RESERVED_FIELD_3 = CpfHeaderNames.RESERVED_FIELD_3;
    public static final String HEADER_RESERVED_FIELD_4 = CpfHeaderNames.RESERVED_FIELD_4;
    public static final String HEADER_RESERVED_FIELD_5 = CpfHeaderNames.RESERVED_FIELD_5;

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
    private static final ThreadLocal<Map<String, Object>> FALLBACK_ATTRIBUTES =
            ThreadLocal.withInitial(HashMap::new);

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
        Object value = getAttribute(ATTR_HEADER);
        return value instanceof TransactionHeader transactionHeader ? transactionHeader : null;
    }

    public static void replaceCurrentHeader(TransactionHeader transactionHeader) {
        setAttribute(ATTR_HEADER, transactionHeader);
    }

    public static int nextSequenceNo() {
        Object value = getAttribute(ATTR_SEQUENCE_NO);
        int next = value instanceof Number number ? number.intValue() + 1 : 1;
        setAttribute(ATTR_SEQUENCE_NO, next);
        return next;
    }

    public static Map<String, String> propagationHeaders() {
        return CpfHeaderPropagator.outboundHeaders();
    }

    public static String parentTransactionId() {
        TransactionHeader header = currentHeader();
        return header != null ? header.getParentTransactionId() : null;
    }

    public static String originalTransactionId() {
        TransactionHeader header = currentHeader();
        return header != null && hasText(header.getOriginalTransactionId())
                ? header.getOriginalTransactionId()
                : currentTransactionId();
    }

    public static String requestId() {
        TransactionHeader header = currentHeader();
        return header != null ? header.getRequestId() : null;
    }

    public static String externalRequestId() {
        TransactionHeader header = currentHeader();
        return header != null ? header.getExternalRequestId() : null;
    }

    public static String correlationId() {
        TransactionHeader header = currentHeader();
        return header != null ? header.getCorrelationId() : null;
    }

    public static String originalChannelCode() {
        TransactionHeader header = currentHeader();
        return header != null ? header.getOriginalChannelCode() : null;
    }

    public static String channelCode() {
        TransactionHeader header = currentHeader();
        return header != null ? header.getChannelCode() : null;
    }

    public static String channelDetailCode() {
        TransactionHeader header = currentHeader();
        return header != null ? header.getChannelDetailCode() : null;
    }

    public static String clientAppId() {
        TransactionHeader header = currentHeader();
        return header != null ? header.getClientAppId() : null;
    }

    public static String callerService() {
        TransactionHeader header = currentHeader();
        return header != null ? header.getCallerService() : null;
    }

    public static String userId() {
        TransactionHeader header = currentHeader();
        return header != null ? header.getUserId() : null;
    }

    public static String operatorId() {
        TransactionHeader header = currentHeader();
        return header != null ? header.getOperatorId() : null;
    }

    public static String customerNo() {
        TransactionHeader header = currentHeader();
        return header != null ? header.getCustomerNo() : null;
    }

    public static String memberNo() {
        TransactionHeader header = currentHeader();
        return header != null ? header.getMemberNo() : null;
    }

    public static String tenantId() {
        TransactionHeader header = currentHeader();
        return header != null ? header.getTenantId() : null;
    }

    public static String organizationCode() {
        TransactionHeader header = currentHeader();
        return header != null ? header.getOrganizationCode() : null;
    }

    public static String branchCode() {
        TransactionHeader header = currentHeader();
        return header != null ? header.getBranchCode() : null;
    }

    public static String clientIp() {
        TransactionHeader header = currentHeader();
        return header != null ? header.getClientIp() : null;
    }

    public static String clientCountryCode() {
        TransactionHeader header = currentHeader();
        return header != null ? header.getClientCountryCode() : null;
    }

    public static String clientTimezone() {
        TransactionHeader header = currentHeader();
        return header != null ? firstText(header.getClientTimezone(), header.getTimezone()) : null;
    }

    public static void clear() {
        MDC.remove(MDC_TRANSACTION_ID);
        MDC.remove(MDC_TRACE_ID);
        MDC.remove(MDC_SPAN_ID);
        MDC.remove(MDC_BUSINESS_TRANSACTION_ID);
        MDC.remove(MDC_BUSINESS_TRANSACTION_NAME);
        MDC.remove(MDC_DYNAMIC_LOG_LEVEL);
        TransactionSegmentContext.clear();
        FALLBACK_ATTRIBUTES.remove();
    }

    /**
     * 업무 거래 ID와 거래명을 MDC에 저장합니다.
     *
     * <p>로그 패턴과 APM 연동에서 동일한 값을 사용할 수 있도록
     * {@code LoggingAspect}가 {@code @CpfTransaction} 정보를 해석한 뒤 호출합니다.</p>
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

    /**
     * 현재 거래에 적용된 동적 로그 레벨을 반환합니다.
     */
    public static String currentDynamicLogLevel() {
        return MDC.get(MDC_DYNAMIC_LOG_LEVEL);
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

    private static void setAttribute(String name, Object value) {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            attributes.setAttribute(name, value, RequestAttributes.SCOPE_REQUEST);
        }
        FALLBACK_ATTRIBUTES.get().put(name, value);
    }

    private static String getAttributeAsString(String name) {
        Object value = getAttribute(name);
        return value != null ? value.toString() : null;
    }

    private static Object getAttribute(String name) {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            Object value = attributes.getAttribute(name, RequestAttributes.SCOPE_REQUEST);
            if (value != null) {
                return value;
            }
        }

        return FALLBACK_ATTRIBUTES.get().get(name);
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

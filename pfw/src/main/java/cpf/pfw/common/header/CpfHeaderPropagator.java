package cpf.pfw.common.header;

import cpf.pfw.common.logging.TransactionContext;
import cpf.pfw.common.logging.TransactionHeader;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 현재 거래 컨텍스트를 하위 서비스 호출 헤더로 변환합니다.
 */
public final class CpfHeaderPropagator {
    private CpfHeaderPropagator() {
    }

    public static Map<String, String> inboundHeaders(TransactionHeader transactionHeader) {
        Map<String, String> headers = new LinkedHashMap<>();
        appendResolvedIdentity(headers, transactionHeader, false);
        appendBusinessHeaders(headers, transactionHeader);
        appendNetworkHeaders(headers, transactionHeader);
        return CpfHeaderMasker.maskHeaders(headers);
    }

    public static Map<String, String> resolvedHeaders() {
        Map<String, String> headers = new LinkedHashMap<>();
        TransactionHeader transactionHeader = TransactionContext.currentHeader();
        appendResolvedIdentity(headers, transactionHeader, false);
        appendBusinessHeaders(headers, transactionHeader);
        appendNetworkHeaders(headers, transactionHeader);
        return CpfHeaderMasker.maskHeaders(headers);
    }

    public static Map<String, String> outboundHeaders() {
        Map<String, String> headers = new LinkedHashMap<>();
        TransactionHeader transactionHeader = TransactionContext.currentHeader();
        appendResolvedIdentity(headers, transactionHeader, true);
        appendBusinessHeaders(headers, transactionHeader);
        appendOutboundAllowed(headers);
        return headers;
    }

    public static CpfHeaderSnapshot currentSnapshot(TransactionHeader transactionHeader) {
        Map<String, String> inbound = inboundHeaders(transactionHeader);
        Map<String, String> resolved = resolvedHeaders();
        Map<String, String> outbound = CpfHeaderMasker.maskHeaders(outboundHeaders());
        Map<String, String> response = new LinkedHashMap<>();
        putIfHasText(response, CpfHeaderNames.TRANSACTION_ID, TransactionContext.currentTransactionId());
        putIfHasText(response, CpfHeaderNames.TRACE_ID, TransactionContext.currentTraceId());
        putIfHasText(response, CpfHeaderNames.SPAN_ID, TransactionContext.currentSpanId());
        putIfHasText(response, CpfHeaderNames.CORRELATION_ID, headerValue(transactionHeader, TransactionHeader::getCorrelationId));
        return new CpfHeaderSnapshot(inbound, resolved, outbound, CpfHeaderMasker.maskHeaders(response));
    }

    private static void appendResolvedIdentity(
            Map<String, String> headers,
            TransactionHeader transactionHeader,
            boolean outbound) {
        String transactionId = TransactionContext.getOrCreateTransactionId();
        putIfHasText(headers, CpfHeaderNames.TRANSACTION_ID, transactionId);
        putIfHasText(headers, CpfHeaderNames.PARENT_TRANSACTION_ID, outbound
                ? transactionId
                : headerValue(transactionHeader, TransactionHeader::getParentTransactionId));
        putIfHasText(headers, CpfHeaderNames.ORIGINAL_TRANSACTION_ID, firstText(
                headerValue(transactionHeader, TransactionHeader::getOriginalTransactionId),
                headerValue(transactionHeader, TransactionHeader::getParentTransactionId),
                transactionId));
        putIfHasText(headers, CpfHeaderNames.REQUEST_ID, headerValue(transactionHeader, TransactionHeader::getRequestId));
        putIfHasText(headers, CpfHeaderNames.EXTERNAL_REQUEST_ID, headerValue(transactionHeader, TransactionHeader::getExternalRequestId));
        putIfHasText(headers, CpfHeaderNames.CORRELATION_ID, headerValue(transactionHeader, TransactionHeader::getCorrelationId));
        putIfHasText(headers, CpfHeaderNames.IDEMPOTENCY_KEY, headerValue(transactionHeader, TransactionHeader::getIdempotencyKey));
        putIfHasText(headers, CpfHeaderNames.TRACE_ID, TransactionContext.getOrCreateTraceId());
        if (outbound) {
            putIfHasText(headers, CpfHeaderNames.PARENT_SPAN_ID, TransactionContext.getOrCreateSpanId());
        } else {
            putIfHasText(headers, CpfHeaderNames.SPAN_ID, TransactionContext.currentSpanId());
            putIfHasText(headers, CpfHeaderNames.PARENT_SPAN_ID, TransactionContext.currentParentSpanId());
        }
        putIfHasText(headers, CpfHeaderNames.TRACEPARENT, headerValue(transactionHeader, TransactionHeader::getTraceparent));
        putIfHasText(headers, CpfHeaderNames.TRACESTATE, headerValue(transactionHeader, TransactionHeader::getTracestate));
    }

    private static void appendBusinessHeaders(Map<String, String> headers, TransactionHeader transactionHeader) {
        putIfHasText(headers, CpfHeaderNames.API_VERSION, headerValue(transactionHeader, TransactionHeader::getApiVersion));
        putIfHasText(headers, CpfHeaderNames.CLIENT_APP_ID, headerValue(transactionHeader, TransactionHeader::getClientAppId));
        putIfHasText(headers, CpfHeaderNames.CLIENT_VERSION, headerValue(transactionHeader, TransactionHeader::getClientVersion));
        putIfHasText(headers, CpfHeaderNames.CALLER_SERVICE, headerValue(transactionHeader, TransactionHeader::getCallerService));
        putIfHasText(headers, CpfHeaderNames.CALLER_INSTANCE_ID, headerValue(transactionHeader, TransactionHeader::getCallerInstanceId));
        putIfHasText(headers, CpfHeaderNames.LOCALE, headerValue(transactionHeader, TransactionHeader::getLocale));
        putIfHasText(headers, CpfHeaderNames.TIMEZONE, headerValue(transactionHeader, TransactionHeader::getTimezone));
        putIfHasText(headers, CpfHeaderNames.REQUEST_TYPE, headerValue(transactionHeader, TransactionHeader::getRequestType));
        putIfHasText(headers, CpfHeaderNames.ORIGINAL_CHANNEL_CODE, headerValue(transactionHeader, TransactionHeader::getOriginalChannelCode));
        putIfHasText(headers, CpfHeaderNames.CHANNEL_CODE, headerValue(transactionHeader, TransactionHeader::getChannelCode));
        putIfHasText(headers, CpfHeaderNames.CHANNEL_DETAIL_CODE, headerValue(transactionHeader, TransactionHeader::getChannelDetailCode));
        putIfHasText(headers, CpfHeaderNames.MEMBER_NO, headerValue(transactionHeader, TransactionHeader::getMemberNo));
        putIfHasText(headers, CpfHeaderNames.CUSTOMER_NO, headerValue(transactionHeader, TransactionHeader::getCustomerNo));
        putIfHasText(headers, CpfHeaderNames.USER_ID, headerValue(transactionHeader, TransactionHeader::getUserId));
        putIfHasText(headers, CpfHeaderNames.OPERATOR_ID, headerValue(transactionHeader, TransactionHeader::getOperatorId));
        putIfHasText(headers, CpfHeaderNames.TENANT_ID, headerValue(transactionHeader, TransactionHeader::getTenantId));
        putIfHasText(headers, CpfHeaderNames.ORGANIZATION_CODE, headerValue(transactionHeader, TransactionHeader::getOrganizationCode));
        putIfHasText(headers, CpfHeaderNames.BRANCH_CODE, headerValue(transactionHeader, TransactionHeader::getBranchCode));
        putIfHasText(headers, CpfHeaderNames.SCREEN_ID, headerValue(transactionHeader, TransactionHeader::getScreenId));
        putIfHasText(headers, CpfHeaderNames.DEVICE_ID, headerValue(transactionHeader, TransactionHeader::getDeviceId));
        putIfHasText(headers, CpfHeaderNames.CLIENT_REQUEST_TIME, headerValue(transactionHeader, TransactionHeader::getClientRequestTime));
        putIfHasText(headers, CpfHeaderNames.CLIENT_TIMEZONE, headerValue(transactionHeader, TransactionHeader::getClientTimezone));
        putIfHasText(headers, CpfHeaderNames.REQUEST_TIMESTAMP, headerValue(transactionHeader, TransactionHeader::getRequestTimestamp));
    }

    private static void appendNetworkHeaders(Map<String, String> headers, TransactionHeader transactionHeader) {
        putIfHasText(headers, CpfHeaderNames.CLIENT_IP, headerValue(transactionHeader, TransactionHeader::getClientIp));
        putIfHasText(headers, CpfHeaderNames.REAL_IP, headerValue(transactionHeader, TransactionHeader::getRealIp));
        putIfHasText(headers, CpfHeaderNames.FORWARDED_FOR, headerValue(transactionHeader, TransactionHeader::getForwardedFor));
        putIfHasText(headers, CpfHeaderNames.FORWARDED, headerValue(transactionHeader, TransactionHeader::getForwarded));
        putIfHasText(headers, CpfHeaderNames.CLIENT_COUNTRY_CODE, headerValue(transactionHeader, TransactionHeader::getClientCountryCode));
        putIfHasText(headers, CpfHeaderNames.CLIENT_REGION_CODE, headerValue(transactionHeader, TransactionHeader::getClientRegionCode));
        putIfHasText(headers, CpfHeaderNames.USER_AGENT, headerValue(transactionHeader, TransactionHeader::getUserAgent));
        putIfHasText(headers, "CPF-Was-Id", headerValue(transactionHeader, TransactionHeader::getWasId));
        putIfHasText(headers, CpfHeaderNames.RESERVED_FIELD_1, headerValue(transactionHeader, TransactionHeader::getReservedField1));
        putIfHasText(headers, CpfHeaderNames.RESERVED_FIELD_2, headerValue(transactionHeader, TransactionHeader::getReservedField2));
        putIfHasText(headers, CpfHeaderNames.RESERVED_FIELD_3, headerValue(transactionHeader, TransactionHeader::getReservedField3));
        putIfHasText(headers, CpfHeaderNames.RESERVED_FIELD_4, headerValue(transactionHeader, TransactionHeader::getReservedField4));
        putIfHasText(headers, CpfHeaderNames.RESERVED_FIELD_5, headerValue(transactionHeader, TransactionHeader::getReservedField5));
    }

    private static void appendOutboundAllowed(Map<String, String> headers) {
        headers.entrySet().removeIf(entry -> !CpfHeaderSpecs.shouldPropagate(entry.getKey()));
    }

    private static String headerValue(TransactionHeader header, HeaderValueReader reader) {
        return header != null ? reader.read(header) : null;
    }

    private static void putIfHasText(Map<String, String> headers, String name, String value) {
        if (hasText(value)) {
            headers.put(name, value);
        }
    }

    private static String firstText(String first, String second, String third) {
        if (hasText(first)) {
            return first;
        }
        return hasText(second) ? second : third;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    @FunctionalInterface
    private interface HeaderValueReader {
        String read(TransactionHeader header);
    }
}

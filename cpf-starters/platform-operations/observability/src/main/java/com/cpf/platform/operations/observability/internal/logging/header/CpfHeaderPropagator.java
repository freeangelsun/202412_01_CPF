package com.cpf.platform.operations.observability.internal.logging.header;

import com.cpf.foundation.context.header.CpfExtensionHeaderPolicy;
import com.cpf.foundation.context.header.CpfHeaderMasker;
import com.cpf.foundation.context.header.CpfHeaderNames;
import com.cpf.foundation.context.header.CpfHeaderSpecs;

import com.cpf.platform.operations.observability.internal.logging.TransactionContext;
import com.cpf.platform.operations.observability.internal.logging.TransactionHeader;
import com.cpf.platform.operations.observability.internal.logging.segment.TransactionSegmentContext;

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
        appendBusinessHeaders(headers, transactionHeader, false);
        appendNetworkHeaders(headers, transactionHeader);
        appendExtensionHeaders(headers, transactionHeader);
        return CpfHeaderMasker.maskHeaders(headers);
    }

    public static Map<String, String> resolvedHeaders() {
        Map<String, String> headers = new LinkedHashMap<>();
        TransactionHeader transactionHeader = TransactionContext.currentHeader();
        appendResolvedIdentity(headers, transactionHeader, false);
        appendBusinessHeaders(headers, transactionHeader, false);
        appendNetworkHeaders(headers, transactionHeader);
        appendExtensionHeaders(headers, transactionHeader);
        return CpfHeaderMasker.maskHeaders(headers);
    }

    public static Map<String, String> outboundHeaders() {
        Map<String, String> headers = new LinkedHashMap<>();
        TransactionHeader transactionHeader = TransactionContext.currentHeader();
        appendResolvedIdentity(headers, transactionHeader, true);
        appendBusinessHeaders(headers, transactionHeader, true);
        appendExtensionHeaders(headers, transactionHeader);
        appendOutboundAllowed(headers);
        appendSegmentHeaders(headers);
        return headers;
    }

    public static CpfHeaderSnapshot currentSnapshot(TransactionHeader transactionHeader) {
        Map<String, String> inbound = inboundHeaders(transactionHeader);
        Map<String, String> resolved = resolvedHeaders();
        Map<String, String> outbound = CpfHeaderMasker.maskHeaders(outboundHeaders());
        Map<String, String> response = new LinkedHashMap<>();
        putIfHasText(response, CpfHeaderNames.TRANSACTION_ID, TransactionContext.currentTransactionId());
        putIfHasText(response, CpfHeaderNames.TRANSACTION_SEGMENT_ID, TransactionSegmentContext.currentSegmentId());
        putIfHasText(response, CpfHeaderNames.TRACE_ID, TransactionContext.currentTraceId());
        putIfHasText(response, CpfHeaderNames.SPAN_ID, TransactionContext.currentSpanId());
        putIfHasText(response, CpfHeaderNames.CORRELATION_ID, headerValue(transactionHeader, value -> value.getCorrelationId()));
        return new CpfHeaderSnapshot(inbound, resolved, outbound, CpfHeaderMasker.maskHeaders(response));
    }

    private static void appendResolvedIdentity(
            Map<String, String> headers,
            TransactionHeader transactionHeader,
            boolean outbound) {
        String transactionId = TransactionContext.getOrCreateTransactionId();
        putIfHasText(headers, CpfHeaderNames.TRANSACTION_ID, transactionId);
        putIfHasText(headers, CpfHeaderNames.STANDARD_EXECUTION_ID, TransactionContext.currentStandardExecutionId());
        headers.put(CpfHeaderNames.PROTOCOL_VERSION, "1.0");
        putIfHasText(headers, CpfHeaderNames.REQUEST_ID, headerValue(transactionHeader, value -> value.getRequestId()));
        putIfHasText(headers, CpfHeaderNames.EXTERNAL_REQUEST_ID, headerValue(transactionHeader, value -> value.getExternalRequestId()));
        putIfHasText(headers, CpfHeaderNames.CORRELATION_ID, headerValue(transactionHeader, value -> value.getCorrelationId()));
        putIfHasText(headers, CpfHeaderNames.IDEMPOTENCY_KEY, headerValue(transactionHeader, value -> value.getIdempotencyKey()));
        putIfHasText(headers, CpfHeaderNames.TRACE_ID, TransactionContext.getOrCreateTraceId());
        if (outbound) {
            putIfHasText(headers, CpfHeaderNames.PARENT_SPAN_ID, TransactionContext.getOrCreateSpanId());
        } else {
            putIfHasText(headers, CpfHeaderNames.SPAN_ID, TransactionContext.currentSpanId());
            putIfHasText(headers, CpfHeaderNames.PARENT_SPAN_ID, TransactionContext.currentParentSpanId());
        }
        putIfHasText(headers, CpfHeaderNames.TRACEPARENT, headerValue(transactionHeader, value -> value.getTraceparent()));
        putIfHasText(headers, CpfHeaderNames.TRACESTATE, headerValue(transactionHeader, value -> value.getTracestate()));
    }

    private static void appendSegmentHeaders(Map<String, String> headers) {
        /*
         * transactionId는 호출 구간이 바뀌어도 그대로 승계합니다.
         * 하위 서비스는 전달받은 parent segment를 기준으로 자기 segmentId를 새로 생성하므로
         * outbound에서 현재 segment를 child의 parentSegmentId로만 전달합니다.
         */
        String currentSegmentId = TransactionSegmentContext.currentSegmentId();
        putIfHasText(headers, CpfHeaderNames.PARENT_TRANSACTION_SEGMENT_ID, currentSegmentId);
        int callDepth = TransactionSegmentContext.currentCallDepth();
        if (callDepth >= 0) {
            headers.put(CpfHeaderNames.TRANSACTION_CALL_DEPTH, String.valueOf(callDepth + 1));
        }
    }

    private static void appendBusinessHeaders(
            Map<String, String> headers,
            TransactionHeader transactionHeader,
            boolean outbound) {
        putIfHasText(headers, CpfHeaderNames.API_VERSION, headerValue(transactionHeader, value -> value.getApiVersion()));
        putIfHasText(headers, CpfHeaderNames.CLIENT_ID, headerValue(transactionHeader, value -> value.getClientId()));
        putIfHasText(headers, CpfHeaderNames.CLIENT_VERSION, headerValue(transactionHeader, value -> value.getClientVersion()));
        putIfHasText(headers, CpfHeaderNames.ORIGINAL_SYSTEM_CODE, TransactionContext.originalSystemCode());
        putIfHasText(headers, CpfHeaderNames.SYSTEM_CODE, TransactionContext.currentSystemCode());
        putIfHasText(headers, CpfHeaderNames.CALLER_SYSTEM_CODE, TransactionContext.callerSystemCode());
        putIfHasText(headers, CpfHeaderNames.TARGET_SYSTEM_CODE, TransactionContext.targetSystemCode());
        putIfHasText(headers, CpfHeaderNames.CALLER_CHANNEL, TransactionContext.callerChannel());
        putIfHasText(headers, CpfHeaderNames.CALLER_INSTANCE_ID, headerValue(transactionHeader, value -> value.getCallerInstanceId()));
        putIfHasText(headers, CpfHeaderNames.ORIGINAL_CHANNEL, TransactionContext.originalChannel());
        putIfHasText(headers, CpfHeaderNames.CURRENT_CHANNEL, TransactionContext.currentChannel());
        putIfHasText(headers, CpfHeaderNames.TARGET_CHANNEL, TransactionContext.targetChannel());
        putIfHasText(headers, CpfHeaderNames.TARGET_OPERATION_ID,
                outbound ? TransactionContext.targetOperationId() : TransactionContext.observedOperationId());
        putIfHasText(headers, CpfHeaderNames.LOCALE, headerValue(transactionHeader, value -> value.getLocale()));
        putIfHasText(headers, CpfHeaderNames.TIMEZONE, headerValue(transactionHeader, value -> value.getTimezone()));
        putIfHasText(headers, CpfHeaderNames.REQUEST_TYPE, headerValue(transactionHeader, value -> value.getRequestType()));
        putIfHasText(headers, CpfHeaderNames.MEMBER_NO, headerValue(transactionHeader, value -> value.getMemberNo()));
        putIfHasText(headers, CpfHeaderNames.CUSTOMER_NO, headerValue(transactionHeader, value -> value.getCustomerNo()));
        putIfHasText(headers, CpfHeaderNames.USER_ID, headerValue(transactionHeader, value -> value.getUserId()));
        putIfHasText(headers, CpfHeaderNames.OPERATOR_ID, headerValue(transactionHeader, value -> value.getOperatorId()));
        putIfHasText(headers, CpfHeaderNames.TENANT_ID, headerValue(transactionHeader, value -> value.getTenantId()));
        putIfHasText(headers, CpfHeaderNames.ORGANIZATION_CODE, headerValue(transactionHeader, value -> value.getOrganizationCode()));
        putIfHasText(headers, CpfHeaderNames.BRANCH_CODE, headerValue(transactionHeader, value -> value.getBranchCode()));
        putIfHasText(headers, CpfHeaderNames.SCREEN_ID, headerValue(transactionHeader, value -> value.getScreenId()));
        putIfHasText(headers, CpfHeaderNames.DEVICE_ID, headerValue(transactionHeader, value -> value.getDeviceId()));
        putIfHasText(headers, CpfHeaderNames.CLIENT_REQUEST_TIME, headerValue(transactionHeader, value -> value.getClientRequestTime()));
        putIfHasText(headers, CpfHeaderNames.CLIENT_TIMEZONE, headerValue(transactionHeader, value -> value.getClientTimezone()));
        putIfHasText(headers, CpfHeaderNames.REQUEST_TIMESTAMP, headerValue(transactionHeader, value -> value.getRequestTimestamp()));
    }

    private static void appendNetworkHeaders(Map<String, String> headers, TransactionHeader transactionHeader) {
        putIfHasText(headers, CpfHeaderNames.CLIENT_IP, headerValue(transactionHeader, value -> value.getClientIp()));
        putIfHasText(headers, CpfHeaderNames.REAL_IP, headerValue(transactionHeader, value -> value.getRealIp()));
        putIfHasText(headers, CpfHeaderNames.FORWARDED_FOR, headerValue(transactionHeader, value -> value.getForwardedFor()));
        putIfHasText(headers, CpfHeaderNames.FORWARDED, headerValue(transactionHeader, value -> value.getForwarded()));
        putIfHasText(headers, CpfHeaderNames.COUNTRY_CODE, headerValue(transactionHeader, value -> value.getClientCountryCode()));
        putIfHasText(headers, CpfHeaderNames.CLIENT_REGION_CODE, headerValue(transactionHeader, value -> value.getClientRegionCode()));
        putIfHasText(headers, CpfHeaderNames.USER_AGENT, headerValue(transactionHeader, value -> value.getUserAgent()));
        putIfHasText(headers, "CPF-Was-Id", headerValue(transactionHeader, value -> value.getWasId()));
        putIfHasText(headers, CpfHeaderNames.RESERVED_FIELD_1, headerValue(transactionHeader, value -> value.getReservedField1()));
        putIfHasText(headers, CpfHeaderNames.RESERVED_FIELD_2, headerValue(transactionHeader, value -> value.getReservedField2()));
        putIfHasText(headers, CpfHeaderNames.RESERVED_FIELD_3, headerValue(transactionHeader, value -> value.getReservedField3()));
        putIfHasText(headers, CpfHeaderNames.RESERVED_FIELD_4, headerValue(transactionHeader, value -> value.getReservedField4()));
        putIfHasText(headers, CpfHeaderNames.RESERVED_FIELD_5, headerValue(transactionHeader, value -> value.getReservedField5()));
    }

    private static void appendExtensionHeaders(Map<String, String> headers, TransactionHeader transactionHeader) {
        if (transactionHeader == null || transactionHeader.getExtensionHeaders() == null) {
            return;
        }
        for (Map.Entry<String, String> entry : transactionHeader.getExtensionHeaders().entrySet()) {
            if (CpfExtensionHeaderPolicy.isAllowedExtensionHeader(entry.getKey())) {
                putIfHasText(headers, entry.getKey(), entry.getValue());
            }
        }
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


    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    @FunctionalInterface
    private interface HeaderValueReader {
        String read(TransactionHeader header);
    }
}

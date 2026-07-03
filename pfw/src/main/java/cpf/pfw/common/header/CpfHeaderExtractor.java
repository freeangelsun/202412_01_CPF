package cpf.pfw.common.header;

import cpf.pfw.common.logging.TransactionHeader;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * HTTP 요청에서 CPF 표준 헤더를 추출하고 내부 거래 헤더 객체로 변환합니다.
 *
 * <p>클라이언트 IP는 전달 헤더를 직접 신뢰하지 않고 {@link CpfTrustedProxyPolicy}를 통해 해석합니다.
 * 원문 전달 헤더는 감사/ADM 표시를 위해 별도 필드에 보관하고, 거래 기준 IP는 해석 결과만 사용합니다.</p>
 */
public final class CpfHeaderExtractor {
    private CpfHeaderExtractor() {
    }

    public static Map<String, String> extractInboundHeaders(HttpServletRequest request) {
        Map<String, String> headers = new LinkedHashMap<>();
        if (request == null) {
            return headers;
        }
        for (CpfHeaderSpec spec : CpfHeaderSpecs.all()) {
            putIfHasText(headers, spec.name(), CpfHeaderMasker.mask(spec.name(), request.getHeader(spec.name())));
        }
        for (Map.Entry<String, String> entry : extractExtensionHeaders(request).entrySet()) {
            putIfAbsentHasText(headers, entry.getKey(), CpfHeaderMasker.mask(entry.getKey(), entry.getValue()));
        }
        putIfHasText(headers, CpfHeaderNames.ROOT_TRANSACTION_ID,
                CpfHeaderMasker.mask(CpfHeaderNames.ROOT_TRANSACTION_ID, request.getHeader(CpfHeaderNames.ROOT_TRANSACTION_ID)));
        putIfHasText(headers, CpfHeaderNames.TRANSACTION_SEGMENT_ID,
                CpfHeaderMasker.mask(CpfHeaderNames.TRANSACTION_SEGMENT_ID, request.getHeader(CpfHeaderNames.TRANSACTION_SEGMENT_ID)));
        putIfHasText(headers, CpfHeaderNames.PARENT_TRANSACTION_SEGMENT_ID,
                CpfHeaderMasker.mask(CpfHeaderNames.PARENT_TRANSACTION_SEGMENT_ID, request.getHeader(CpfHeaderNames.PARENT_TRANSACTION_SEGMENT_ID)));
        putIfHasText(headers, CpfHeaderNames.TRANSACTION_CALL_DEPTH,
                CpfHeaderMasker.mask(CpfHeaderNames.TRANSACTION_CALL_DEPTH, request.getHeader(CpfHeaderNames.TRANSACTION_CALL_DEPTH)));
        return headers;
    }

    public static TransactionHeader toTransactionHeader(HttpServletRequest request, String wasId) {
        return TransactionHeader.builder()
                .parentTransactionId(header(request, CpfHeaderNames.PARENT_TRANSACTION_ID))
                .originalTransactionId(header(request, CpfHeaderNames.ORIGINAL_TRANSACTION_ID))
                .rootTransactionGlobalId(header(request, CpfHeaderNames.ROOT_TRANSACTION_ID))
                .transactionSegmentId(header(request, CpfHeaderNames.TRANSACTION_SEGMENT_ID))
                .parentSegmentId(header(request, CpfHeaderNames.PARENT_TRANSACTION_SEGMENT_ID))
                .callDepth(header(request, CpfHeaderNames.TRANSACTION_CALL_DEPTH))
                .requestId(header(request, CpfHeaderNames.REQUEST_ID))
                .externalRequestId(header(request, CpfHeaderNames.EXTERNAL_REQUEST_ID))
                .apiVersion(header(request, CpfHeaderNames.API_VERSION))
                .clientAppId(header(request, CpfHeaderNames.CLIENT_APP_ID))
                .clientVersion(header(request, CpfHeaderNames.CLIENT_VERSION))
                .callerService(header(request, CpfHeaderNames.CALLER_SERVICE))
                .callerInstanceId(header(request, CpfHeaderNames.CALLER_INSTANCE_ID))
                .correlationId(header(request, CpfHeaderNames.CORRELATION_ID))
                .idempotencyKey(firstText(
                        header(request, CpfHeaderNames.IDEMPOTENCY_KEY),
                        header(request, CpfHeaderNames.IDEMPOTENCY_KEY_ALIAS)))
                .locale(header(request, CpfHeaderNames.LOCALE))
                .timezone(header(request, CpfHeaderNames.TIMEZONE))
                .requestType(header(request, CpfHeaderNames.REQUEST_TYPE))
                .originalChannelCode(header(request, CpfHeaderNames.ORIGINAL_CHANNEL_CODE))
                .channelCode(header(request, CpfHeaderNames.CHANNEL_CODE))
                .channelDetailCode(header(request, CpfHeaderNames.CHANNEL_DETAIL_CODE))
                .memberNo(header(request, CpfHeaderNames.MEMBER_NO))
                .customerNo(header(request, CpfHeaderNames.CUSTOMER_NO))
                .userId(header(request, CpfHeaderNames.USER_ID))
                .operatorId(header(request, CpfHeaderNames.OPERATOR_ID))
                .tenantId(header(request, CpfHeaderNames.TENANT_ID))
                .organizationCode(header(request, CpfHeaderNames.ORGANIZATION_CODE))
                .branchCode(header(request, CpfHeaderNames.BRANCH_CODE))
                .screenId(header(request, CpfHeaderNames.SCREEN_ID))
                .deviceId(header(request, CpfHeaderNames.DEVICE_ID))
                .clientRequestTime(header(request, CpfHeaderNames.CLIENT_REQUEST_TIME))
                .clientIp(CpfTrustedProxyPolicy.resolveClientIp(request))
                .forwardedFor(header(request, CpfHeaderNames.FORWARDED_FOR))
                .forwarded(header(request, CpfHeaderNames.FORWARDED))
                .realIp(header(request, CpfHeaderNames.REAL_IP))
                .clientCountryCode(header(request, CpfHeaderNames.CLIENT_COUNTRY_CODE))
                .clientRegionCode(header(request, CpfHeaderNames.CLIENT_REGION_CODE))
                .clientTimezone(header(request, CpfHeaderNames.CLIENT_TIMEZONE))
                .traceparent(header(request, CpfHeaderNames.TRACEPARENT))
                .tracestate(header(request, CpfHeaderNames.TRACESTATE))
                .userAgent(header(request, CpfHeaderNames.USER_AGENT))
                .requestTimestamp(header(request, CpfHeaderNames.REQUEST_TIMESTAMP))
                .reservedField1(header(request, CpfHeaderNames.RESERVED_FIELD_1))
                .reservedField2(header(request, CpfHeaderNames.RESERVED_FIELD_2))
                .reservedField3(header(request, CpfHeaderNames.RESERVED_FIELD_3))
                .reservedField4(header(request, CpfHeaderNames.RESERVED_FIELD_4))
                .reservedField5(header(request, CpfHeaderNames.RESERVED_FIELD_5))
                .extensionHeaders(extractExtensionHeaders(request))
                .wasId(wasId)
                .build();
    }

    public static Map<String, String> extractExtensionHeaders(HttpServletRequest request) {
        Map<String, String> headers = new LinkedHashMap<>();
        if (request == null) {
            return headers;
        }
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames != null && headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            if (!CpfExtensionHeaderPolicy.isAllowedExtensionHeader(name)) {
                continue;
            }
            putIfAbsentHasText(headers, name, request.getHeader(name));
        }
        return headers;
    }

    private static String header(HttpServletRequest request, String name) {
        return request != null ? request.getHeader(name) : null;
    }

    private static void putIfHasText(Map<String, String> headers, String name, String value) {
        if (hasText(value)) {
            headers.put(name, value);
        }
    }

    private static void putIfAbsentHasText(Map<String, String> headers, String name, String value) {
        if (!hasText(value) || containsHeaderIgnoreCase(headers, name)) {
            return;
        }
        headers.put(name, value);
    }

    private static boolean containsHeaderIgnoreCase(Map<String, String> headers, String name) {
        String normalized = name.toLowerCase(Locale.ROOT);
        return headers.keySet().stream().anyMatch(key -> key.toLowerCase(Locale.ROOT).equals(normalized));
    }

    private static String firstText(String first, String second) {
        return hasText(first) ? first : second;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

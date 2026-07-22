package com.cpf.core.common.header;

import com.cpf.core.common.logging.TransactionContext;
import com.cpf.core.common.logging.TransactionHeader;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 현재 거래 컨텍스트에서 업무 코드가 바꿀 수 있는 헤더만 제한적으로 보정하는 API입니다.
 *
 * <p>거래 ID, trace/span, 인증 토큰, 사용자/운영자 식별자처럼 추적성과 보안에 영향을 주는 헤더는
 * 업무 코드에서 변경할 수 없습니다. 업무 보정은 채널 상세, 고객/회원/테넌트/조직/지점처럼
 * 인증 주체를 바꾸지 않는 보조 식별자에 한정합니다.</p>
 */
public final class CpfHeaderMutator {
    private static final Set<String> RESTRICTED_HEADERS = Set.of(
            lower(CpfHeaderNames.TRANSACTION_ID),
            lower(CpfHeaderNames.PARENT_TRANSACTION_ID),
            lower(CpfHeaderNames.ORIGINAL_TRANSACTION_ID),
            lower(CpfHeaderNames.TRACE_ID),
            lower(CpfHeaderNames.SPAN_ID),
            lower(CpfHeaderNames.PARENT_SPAN_ID),
            lower(CpfHeaderNames.USER_ID),
            lower(CpfHeaderNames.OPERATOR_ID),
            lower(CpfHeaderNames.CORRELATION_ID),
            lower(CpfHeaderNames.IDEMPOTENCY_KEY),
            lower(CpfHeaderNames.IDEMPOTENCY_KEY_ALIAS),
            lower(CpfHeaderNames.AUTHORIZATION),
            lower(CpfHeaderNames.API_KEY),
            lower(CpfHeaderNames.REQUEST_SIGNATURE),
            lower(CpfHeaderNames.NONCE)
    );

    private CpfHeaderMutator() {
    }

    public static TransactionHeader put(String headerName, String value) {
        TransactionHeader current = TransactionContext.currentHeader();
        TransactionHeader updated = withAllowedHeader(current, headerName, value);
        TransactionContext.replaceCurrentHeader(updated);
        return updated;
    }

    public static TransactionHeader withAllowedHeader(String headerName, String value) {
        return withAllowedHeader(TransactionContext.currentHeader(), headerName, value);
    }

    public static TransactionHeader withAllowedHeader(TransactionHeader source, String headerName, String value) {
        if (headerName == null || headerName.isBlank()) {
            throw new IllegalArgumentException("변경할 헤더명이 필요합니다.");
        }
        if (RESTRICTED_HEADERS.contains(lower(headerName))) {
            throw new IllegalArgumentException("시스템 추적 또는 민감 헤더는 업무 코드에서 변경할 수 없습니다. headerName=" + headerName);
        }
        if (CpfExtensionHeaderPolicy.isExtensionHeader(headerName)) {
            CpfExtensionHeaderPolicy.requireAllowedExtensionHeader(headerName);
            return withExtensionHeader(source, headerName, value);
        }

        TransactionHeader.TransactionHeaderBuilder builder = source != null
                ? source.toBuilder()
                : TransactionHeader.builder();
        String normalized = lower(headerName);
        if (normalized.equals(lower(CpfHeaderNames.CHANNEL_DETAIL_CODE))) {
            return builder.channelDetailCode(value).build();
        }
        if (normalized.equals(lower(CpfHeaderNames.CUSTOMER_NO))) {
            return builder.customerNo(value).build();
        }
        if (normalized.equals(lower(CpfHeaderNames.MEMBER_NO))) {
            return builder.memberNo(value).build();
        }
        if (normalized.equals(lower(CpfHeaderNames.TENANT_ID))) {
            return builder.tenantId(value).build();
        }
        if (normalized.equals(lower(CpfHeaderNames.ORGANIZATION_CODE))) {
            return builder.organizationCode(value).build();
        }
        if (normalized.equals(lower(CpfHeaderNames.BRANCH_CODE))) {
            return builder.branchCode(value).build();
        }
        throw new IllegalArgumentException("업무 코드에서 보정하도록 등록되지 않은 헤더입니다. headerName=" + headerName);
    }

    private static TransactionHeader withExtensionHeader(TransactionHeader source, String headerName, String value) {
        TransactionHeader.TransactionHeaderBuilder builder = source != null
                ? source.toBuilder()
                : TransactionHeader.builder();
        Map<String, String> extensionHeaders = new LinkedHashMap<>();
        if (source != null && source.getExtensionHeaders() != null) {
            extensionHeaders.putAll(source.getExtensionHeaders());
        }
        removeIgnoreCase(extensionHeaders, headerName);
        if (value != null && !value.isBlank()) {
            extensionHeaders.put(headerName, value);
        }
        return builder.extensionHeaders(Map.copyOf(extensionHeaders)).build();
    }

    private static void removeIgnoreCase(Map<String, String> headers, String headerName) {
        String normalized = lower(headerName);
        headers.keySet().removeIf(name -> lower(name).equals(normalized));
    }

    private static String lower(String value) {
        return value.toLowerCase(Locale.ROOT);
    }
}

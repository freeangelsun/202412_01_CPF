package cpf.pfw.common.header;

import cpf.pfw.common.logging.TransactionContext;
import cpf.pfw.common.logging.TransactionHeader;

import java.util.Locale;
import java.util.Set;

/**
 * 현재 거래 컨텍스트에서 업무 코드가 바꿀 수 있는 헤더만 제한적으로 보정하는 API입니다.
 *
 * <p>거래 ID, trace/span, 인증 토큰처럼 추적성과 보안에 영향을 주는 헤더는 업무 코드에서 변경할 수 없습니다.
 * 채널 상세, 사용자 식별자, 멱등 키처럼 업무 처리 중 보강될 수 있는 값만 허용합니다.</p>
 */
public final class CpfHeaderMutator {
    private static final Set<String> RESTRICTED_HEADERS = Set.of(
            lower(CpfHeaderNames.TRANSACTION_ID),
            lower(CpfHeaderNames.PARENT_TRANSACTION_ID),
            lower(CpfHeaderNames.ORIGINAL_TRANSACTION_ID),
            lower(CpfHeaderNames.TRACE_ID),
            lower(CpfHeaderNames.SPAN_ID),
            lower(CpfHeaderNames.PARENT_SPAN_ID),
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

        TransactionHeader.TransactionHeaderBuilder builder = source != null
                ? source.toBuilder()
                : TransactionHeader.builder();
        String normalized = lower(headerName);
        if (normalized.equals(lower(CpfHeaderNames.CHANNEL_DETAIL_CODE))) {
            return builder.channelDetailCode(value).build();
        }
        if (normalized.equals(lower(CpfHeaderNames.USER_ID))) {
            return builder.userId(value).build();
        }
        if (normalized.equals(lower(CpfHeaderNames.OPERATOR_ID))) {
            return builder.operatorId(value).build();
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
        if (normalized.equals(lower(CpfHeaderNames.CORRELATION_ID))) {
            return builder.correlationId(value).build();
        }
        if (normalized.equals(lower(CpfHeaderNames.IDEMPOTENCY_KEY))) {
            return builder.idempotencyKey(value).build();
        }
        if (normalized.equals(lower(CpfHeaderNames.CLIENT_COUNTRY_CODE))) {
            return builder.clientCountryCode(value).build();
        }
        if (normalized.equals(lower(CpfHeaderNames.CLIENT_REGION_CODE))) {
            return builder.clientRegionCode(value).build();
        }
        if (normalized.equals(lower(CpfHeaderNames.CLIENT_TIMEZONE))) {
            return builder.clientTimezone(value).build();
        }
        throw new IllegalArgumentException("업무 코드에서 보정하도록 등록되지 않은 헤더입니다. headerName=" + headerName);
    }

    private static String lower(String value) {
        return value.toLowerCase(Locale.ROOT);
    }
}

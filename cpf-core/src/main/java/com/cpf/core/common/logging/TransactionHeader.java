package com.cpf.core.common.logging;

import lombok.Builder;
import lombok.Value;

import java.util.Map;

/**
 * 요청에서 수집한 CPF 표준 거래 헤더입니다.
 *
 * <p>인증 토큰, API key, 원문 서명처럼 원문 로그 저장이 금지된 값은 이 객체에 담지 않습니다.
 * 거래 로그와 하위 서비스 전파는 이 객체와 {@link TransactionContext}를 기준으로 수행합니다.</p>
 */
@Value
@Builder(toBuilder = true)
public class TransactionHeader {
    String parentTransactionId;
    String originalTransactionId;
    String rootTransactionGlobalId;
    String transactionSegmentId;
    String parentSegmentId;
    String callDepth;
    String requestId;
    String externalRequestId;
    String apiVersion;
    String clientAppId;
    String clientVersion;
    String callerService;
    String callerInstanceId;
    String correlationId;
    String idempotencyKey;
    String locale;
    String timezone;
    String requestType;
    String originalChannelCode;
    String channelCode;
    String channelDetailCode;
    String memberNo;
    String customerNo;
    String userId;
    String operatorId;
    String tenantId;
    String organizationCode;
    String branchCode;
    String screenId;
    String deviceId;
    String clientRequestTime;
    String clientIp;
    String forwardedFor;
    String forwarded;
    String realIp;
    String clientCountryCode;
    String clientRegionCode;
    String clientTimezone;
    String traceparent;
    String tracestate;
    String userAgent;
    String requestTimestamp;
    String reservedField1;
    String reservedField2;
    String reservedField3;
    String reservedField4;
    String reservedField5;
    @Builder.Default
    Map<String, String> extensionHeaders = Map.of();
    String wasId;
}

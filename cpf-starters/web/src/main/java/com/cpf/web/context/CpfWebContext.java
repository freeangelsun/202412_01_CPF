package com.cpf.web.context;

/**
 * Web/Online Owner가 소유하는 요청 상호작용 Context입니다.
 *
 * <p>Core {@code CpfContext}가 보유하지 않는 HTTP/Web 전용 의미만 포함합니다. 외부 요청에서 받은 값은
 * Inbound Adapter 검증을 통과한 뒤에만 이 Context에 기록하며 Authorization/API Key/Cookie/Token/Signature
 * 같은 Secret 원문은 저장하지 않습니다.</p>
 */
public record CpfWebContext(
        String requestId,
        String externalRequestId,
        String apiVersion,
        String channelCode,
        String clientApp,
        String clientVersion,
        String screenId,
        String deviceId,
        String locale,
        String clientTimezone,
        String resolvedClientIp,
        String userAgent,
        String traceparent,
        String tracestate,
        CpfHttpIngressTrust trustLevel) {
    public CpfWebContext {
        if (trustLevel == null) trustLevel = CpfHttpIngressTrust.UNTRUSTED_EXTERNAL;
    }

    /** 기존 InteractionContext 6개 필드 생성자 Consumer의 source migration 호환 생성자입니다. */
    public CpfWebContext(String requestId,String externalRequestId,String channelCode,String clientIp,String traceparent,String tracestate) {
        this(requestId,externalRequestId,null,channelCode,null,null,null,null,null,null,clientIp,null,traceparent,tracestate,
                CpfHttpIngressTrust.UNTRUSTED_EXTERNAL);
    }
}

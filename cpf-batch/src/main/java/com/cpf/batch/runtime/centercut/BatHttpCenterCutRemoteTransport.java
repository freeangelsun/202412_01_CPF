package com.cpf.batch.runtime.centercut;

import com.cpf.core.api.servicecall.CpfServiceTarget;
import org.springframework.web.client.RestClient;

import java.util.Objects;

/**
 * 분리 WAS Generated Domain의 Center-Cut endpoint를 호출하는 BAT 기본 HTTP transport입니다.
 * 대상 선택/health/retry/circuit/UNKNOWN 판단은 CpfServiceCallEngine이 담당하고 이 클래스는 전송만 담당합니다.
 */
public final class BatHttpCenterCutRemoteTransport implements BatCenterCutRemoteTransport {
    private final RestClient.Builder builder;

    public BatHttpCenterCutRemoteTransport(RestClient.Builder builder) {
        this.builder = Objects.requireNonNull(builder, "RestClient.Builder");
    }

    @Override
    public String exchange(CpfServiceTarget target, BatCenterCutRemoteRequest request) {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(request, "request");
        RestClient client = builder.clone().baseUrl(target.baseUrl()).build();
        return client.post()
                .uri(request.requestPath())
                .header("X-Cpf-Transaction-Id", safe(request.transactionId()))
                .header("X-Cpf-Parent-Segment-Id", safe(request.parentSegmentId()))
                .header("X-Cpf-Transaction-Segment-Id", safe(request.transactionSegmentId()))
                .body(request)
                .retrieve()
                .body(String.class);
    }

    private static String safe(String value) { return value == null ? "" : value; }
}

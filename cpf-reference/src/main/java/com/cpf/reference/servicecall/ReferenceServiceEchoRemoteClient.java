package com.cpf.reference.servicecall;

import com.cpf.core.common.http.CpfWebClient;
import com.cpf.core.common.servicecall.CpfServiceCallOptions;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * REF 로컬 중립 시뮬레이터 호출의 HTTP 세부정보를 캡슐화하는 adapter입니다.
 */
@Component
public class ReferenceServiceEchoRemoteClient implements ReferenceServiceEchoClient {
    private final CpfWebClient webClient;

    /**
     * remote adapter를 생성합니다.
     *
     * @param webClient CPF 서비스 호출 경계
     */
    public ReferenceServiceEchoRemoteClient(CpfWebClient webClient) {
        this.webClient = Objects.requireNonNull(webClient, "webClient는 필수입니다.");
    }

    /**
     * 중앙 정책을 유지하며 REF 자체 중립 응답을 조회합니다.
     *
     * @param request 업무 요청
     * @param options named policy 옵션
     * @return typed 중립 응답
     */
    @Override
    public ReferenceServiceEchoResponse execute(
            ReferenceServiceEchoRequest request,
            CpfServiceCallOptions options) {
        Objects.requireNonNull(request, "request는 필수입니다.");
        Objects.requireNonNull(options, "options는 필수입니다.");
        Map<?, ?> result = webClient.get(
                "REF-EXTERNAL-SIMULATOR",
                uri -> uri.path("/api/reference/external-simulator/response")
                        .queryParam("externalKey", request.requestKey())
                        .queryParam("status", 200)
                        .queryParam("delayMillis", 0)
                        .build(),
                Map.class);
        return new ReferenceServiceEchoResponse(
                text(result, "externalKey", request.requestKey()),
                text(result, "status", "UNKNOWN"),
                text(result, "processedAt", ""));
    }

    private String text(Map<?, ?> source, String key, String fallback) {
        Object value = source == null ? null : source.get(key);
        return value == null ? fallback : String.valueOf(value);
    }
}

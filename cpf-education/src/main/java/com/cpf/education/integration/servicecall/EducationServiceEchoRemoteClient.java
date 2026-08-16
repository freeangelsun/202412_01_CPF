package com.cpf.education.integration.servicecall;

import com.cpf.integration.http.api.CpfHttpClient;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/** EDU 로컬 중립 시뮬레이터 호출의 HTTP 세부정보를 공개 CPF HTTP 경계로 캡슐화하는 adapter입니다. */
@Component
public class EducationServiceEchoRemoteClient implements EducationServiceEchoClient {
    private final CpfHttpClient httpClient;

    public EducationServiceEchoRemoteClient(CpfHttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient는 필수입니다.");
    }

    @Override
    public EducationServiceEchoResponse execute(EducationServiceEchoRequest request) {
        Objects.requireNonNull(request, "request는 필수입니다.");
        Map<?, ?> result = httpClient.get(
                "EDU-EXTERNAL-SIMULATOR",
                uri -> uri.path("/api/education/external-simulator/response")
                        .queryParam("externalKey", request.requestKey())
                        .queryParam("status", 200)
                        .queryParam("delayMillis", 0)
                        .build(),
                Map.class);
        return new EducationServiceEchoResponse(
                text(result, "externalKey", request.requestKey()),
                text(result, "status", "UNKNOWN"),
                text(result, "processedAt", ""));
    }

    private String text(Map<?, ?> source, String key, String fallback) {
        Object value = source == null ? null : source.get(key);
        return value == null ? fallback : String.valueOf(value);
    }
}

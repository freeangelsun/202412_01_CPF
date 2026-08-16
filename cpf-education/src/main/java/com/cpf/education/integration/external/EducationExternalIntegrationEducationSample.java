package com.cpf.education.integration.external;

import com.cpf.integration.http.api.CpfHttpClient;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/** 외부 장애 시뮬레이터를 CPF 공개 HTTP/ServiceCall 경계로 실행하는 교육 샘플입니다. */
@Component
public class EducationExternalIntegrationEducationSample {
    private final CpfHttpClient httpClient;

    public EducationExternalIntegrationEducationSample(CpfHttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient는 필수입니다.");
    }

    /** call 작업을 CPF 표준 계약에 따라 수행한다. */
    public Map<String, Object> call(String externalKey, int status, long delayMillis) {
        @SuppressWarnings("unchecked")
        Map<String, Object> result = httpClient.get(
                "EDU-EXTERNAL-SIMULATOR",
                uri -> uri.path("/api/education/external-simulator/response")
                        .queryParam("status", status)
                        .queryParam("delayMillis", delayMillis)
                        .queryParam("externalKey", externalKey)
                        .build(),
                Map.class);
        return result;
    }
}

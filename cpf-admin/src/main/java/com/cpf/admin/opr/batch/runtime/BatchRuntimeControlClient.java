package com.cpf.admin.opr.batch.runtime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/** ADM은 BAT DB를 직접 조회하지 않고 BAT Control Server Owner API만 호출합니다. */
@Component
public class BatchRuntimeControlClient {
    private final RestClient client;

    public BatchRuntimeControlClient(RestClient.Builder builder,
                                     @Value("${cpf.batch.control.base-url:http://127.0.0.1:8180}") String baseUrl) {
        this.client = builder.baseUrl(baseUrl).build();
    }

    public List<Map<String, Object>> instances(long staleAfterSeconds) {
        List<Map<String, Object>> body = client.get()
                .uri(uri -> uri.path("/api/v1/batch/runtime/instances")
                        .queryParam("staleAfterSeconds", staleAfterSeconds).build())
                .retrieve().body(new ParameterizedTypeReference<>() {});
        return body == null ? List.of() : body;
    }

    public Map<String, Object> view(String view) {
        Map<String, Object> body = client.get()
                .uri("/api/v1/batch/views/{view}", view)
                .retrieve().body(new ParameterizedTypeReference<>() {});
        return body == null ? Map.of("view", view, "items", List.of()) : body;
    }

    public Map<String, Object> createPlan(Map<String, Object> request) {
        Map<String, Object> body = client.post().uri("/api/v1/batch/deployment-plans")
                .body(request).retrieve().body(new ParameterizedTypeReference<>() {});
        return body == null ? Map.of() : body;
    }

    public List<Map<String, Object>> jobDefinitions(String jobId, String state, int limit) {
        List<Map<String, Object>> body = client.get()
                .uri(uri -> uri.path("/api/v1/batch/job-definitions")
                        .queryParamIfPresent("jobId", java.util.Optional.ofNullable(jobId).filter(v -> !v.isBlank()))
                        .queryParamIfPresent("state", java.util.Optional.ofNullable(state).filter(v -> !v.isBlank()))
                        .queryParam("limit", Math.max(1, Math.min(limit, 1000))).build())
                .retrieve().body(new ParameterizedTypeReference<>() {});
        return body == null ? List.of() : body;
    }

    public Map<String, Object> validateJobDefinition(Map<String, Object> request) {
        Map<String, Object> body = client.post().uri("/api/v1/batch/job-definitions/validate")
                .body(request).retrieve().body(new ParameterizedTypeReference<>() {});
        return body == null ? Map.of() : body;
    }

    public Map<String, Object> saveJobDefinition(Map<String, Object> request) {
        Map<String, Object> body = client.post().uri("/api/v1/batch/job-definitions/drafts")
                .body(request).retrieve().body(new ParameterizedTypeReference<>() {});
        return body == null ? Map.of() : body;
    }

    public Map<String, Object> transitionJobDefinition(String jobId, long version, Map<String, Object> request) {
        Map<String, Object> body = client.post().uri("/api/v1/batch/job-definitions/{jobId}/versions/{version}/transition", jobId, version)
                .body(request).retrieve().body(new ParameterizedTypeReference<>() {});
        return body == null ? Map.of() : body;
    }
}

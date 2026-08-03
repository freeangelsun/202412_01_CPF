package com.cpf.core.common.servicecall;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * CPF 서비스 호출 엔진에 전달하는 표준 요청 모델입니다.
 *
 * <p>업무 모듈은 하위 서비스 호출 시 서비스 ID, endpoint code, HTTP method, path,
 * timeout/retry 정책 힌트를 이 객체로 전달합니다. 실제 HTTP 호출 구현은 어댑터가 담당하고,
 * 엔진은 표준 레지스트리 조회, 라우팅, 이력 기록의 공통 규칙을 책임집니다.</p>
 */
public record ServiceCallRequest(
        String serviceId,
        String endpointCode,
        String instanceId,
        String httpMethod,
        String requestPath,
        Integer timeoutMillis,
        Integer retryCount,
        Map<String, String> headers,
        Map<String, Object> attributes) {

    public ServiceCallRequest {
        headers = headers == null ? Map.of() : Map.copyOf(headers);
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    public static Builder builder(String serviceId) {
        return new Builder(serviceId);
    }

    public static final class Builder {
        private final String serviceId;
        private String endpointCode;
        private String instanceId;
        private String httpMethod = "GET";
        private String requestPath = "/";
        private Integer timeoutMillis;
        private Integer retryCount;
        private final Map<String, String> headers = new LinkedHashMap<>();
        private final Map<String, Object> attributes = new LinkedHashMap<>();

        private Builder(String serviceId) {
            this.serviceId = serviceId;
        }

        public Builder endpointCode(String endpointCode) {
            this.endpointCode = endpointCode;
            return this;
        }

        public Builder instanceId(String instanceId) {
            this.instanceId = instanceId;
            return this;
        }

        public Builder httpMethod(String httpMethod) {
            this.httpMethod = httpMethod;
            return this;
        }

        public Builder requestPath(String requestPath) {
            this.requestPath = requestPath;
            return this;
        }

        public Builder timeoutMillis(Integer timeoutMillis) {
            this.timeoutMillis = timeoutMillis;
            return this;
        }

        public Builder retryCount(Integer retryCount) {
            this.retryCount = retryCount;
            return this;
        }

        public Builder header(String name, String value) {
            if (name != null && value != null) {
                this.headers.put(name, value);
            }
            return this;
        }

        public Builder attribute(String name, Object value) {
            if (name != null && value != null) {
                this.attributes.put(name, value);
            }
            return this;
        }

        public ServiceCallRequest build() {
            return new ServiceCallRequest(
                    serviceId,
                    endpointCode,
                    instanceId,
                    httpMethod,
                    requestPath,
                    timeoutMillis,
                    retryCount,
                    headers,
                    attributes);
        }
    }
}

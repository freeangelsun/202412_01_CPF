package com.cpf.batch.worker;

import com.cpf.batch.api.BatchApprovedExecutorSnapshot;
import com.cpf.core.api.broker.CpfBrokerClient;
import com.cpf.core.api.broker.CpfBrokerPublishRequest;
import com.cpf.core.api.broker.CpfBrokerPublishResult;
import com.cpf.core.api.servicecall.CpfServiceCaller;
import com.cpf.core.api.servicecall.CpfServiceRequest;
import com.cpf.core.api.servicecall.CpfServiceResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.*;

/**
 * Published Job Definition의 SERVICE_CALL/MESSAGE/PROTOCOL 실행 Adapter Registry입니다.
 *
 * <p>설치되지 않은 Capability를 성공으로 위장하지 않습니다. SERVICE_CALL은 CPF Typed Service
 * Caller를, MESSAGE_TRIGGER는 CPF Broker Outbox Client를, HTTP Protocol은 검증된 IP에 고정된 전송기를 사용합니다.</p>
 */
@Component
public class BatchRuntimeExecutorRegistry {
    private final ObjectProvider<CpfServiceCaller> serviceCaller;
    private final ObjectProvider<CpfBrokerClient> brokerClient;
    private final HttpClient httpClient;
    private final ObjectMapper canonicalJson;
    private final WorkerOperationalProperties operationalProperties;
    private final BatchOutboundHttpPolicy outboundPolicy;
    private final PinnedBatchHttpTransport outboundTransport;

    @Autowired
    public BatchRuntimeExecutorRegistry(
            ObjectProvider<CpfServiceCaller> serviceCaller,
            ObjectProvider<CpfBrokerClient> brokerClient,
            ObjectMapper objectMapper,
            WorkerOperationalProperties operationalProperties) {
        this.serviceCaller = serviceCaller;
        this.brokerClient = brokerClient;
        this.canonicalJson = objectMapper.copy()
                .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.operationalProperties = operationalProperties;
        operationalProperties.getOutboundHttp().validate();
        this.outboundPolicy = new BatchOutboundHttpPolicy(operationalProperties.getOutboundHttp());
        this.outboundTransport = new PinnedBatchHttpTransport(operationalProperties.getOutboundHttp());
    }

    /** Test/standalone Source compatibility. Production Spring wiring injects the managed mapper. */
    public BatchRuntimeExecutorRegistry(
            ObjectProvider<CpfServiceCaller> serviceCaller,
            ObjectProvider<CpfBrokerClient> brokerClient) {
        this(serviceCaller, brokerClient, new ObjectMapper(), new WorkerOperationalProperties());
    }


    public ExecutionResult execute(
            BatchApprovedExecutorSnapshot definition,
            Map<String, Object> parameters,
            long executionId,
            String transactionId,
            String segmentId) throws Exception {
        return switch (definition.executorType()) {
            case SERVICE_CALL -> executeServiceCall(definition, parameters);
            case MESSAGE_TRIGGER -> executeMessage(definition, parameters, executionId, transactionId, segmentId);
            case PROTOCOL_ADAPTER -> executeProtocol(definition, parameters);
            default -> throw new IllegalArgumentException(
                    "External Registry does not own executor: " + definition.executorType());
        };
    }

    private ExecutionResult executeServiceCall(BatchApprovedExecutorSnapshot definition, Map<String, Object> parameters) {
        CpfServiceCaller caller = serviceCaller.getIfAvailable();
        if (caller == null) {
            return ExecutionResult.failed("CAPABILITY_UNAVAILABLE",
                    "CpfServiceCaller capability is not installed", false);
        }
        ServiceReference ref = ServiceReference.parse(definition.executorReference());
        String method = text(parameters.get("httpMethod"), "POST").toUpperCase(Locale.ROOT);
        String path = text(parameters.get("requestPath"), "/");
        String body = jsonBody(parameters.get("body"));
        CpfServiceRequest request = CpfServiceRequest.builder(ref.serviceId())
                .endpointCode(ref.endpointCode())
                .httpMethod(method)
                .requestPath(path)
                .timeoutMillis(Math.toIntExact(Math.min(Integer.MAX_VALUE,
                        definition.timeoutSeconds() * 1_000L)))
                .retryCount(Math.max(0, definition.maxAttempts() - 1))
                .attribute("batchJobId", definition.jobId())
                .attribute("batchDefinitionVersion", definition.definitionVersion())
                .build();
        CpfServiceResult<String> result = caller.invoke(request, target -> {
            URI uri = target.baseUrl().endsWith("/") && path.startsWith("/")
                    ? URI.create(target.baseUrl().substring(0, target.baseUrl().length() - 1) + path)
                    : URI.create(target.baseUrl() + path);
            HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(definition.timeoutSeconds()));
            if ("GET".equals(method) || "DELETE".equals(method)) {
                builder.method(method, HttpRequest.BodyPublishers.noBody());
            } else {
                builder.header("Content-Type", "application/json")
                        .method(method, HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
            }
            try {
                HttpResponse<String> response = httpClient.send(
                        builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    throw new IllegalStateException("SERVICE_HTTP_" + response.statusCode());
                }
                return response.body();
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("SERVICE_CALL_INTERRUPTED", interrupted);
            } catch (Exception failure) {
                throw new IllegalStateException("SERVICE_CALL_TRANSPORT_FAILED", failure);
            }
        });
        if (result.success()) {
            return ExecutionResult.completed(result.responseBody(), result.attemptCount());
        }
        if (result.unknown()) {
            return ExecutionResult.failed(
                    blankTo(result.failureCode(), "UNKNOWN_RESULT"),
                    blankTo(result.failureMessage(), "Service call result is unknown"),
                    true);
        }
        return ExecutionResult.failed(
                blankTo(result.failureCode(), "SERVICE_CALL_FAILED"),
                blankTo(result.failureMessage(), "Service call failed"),
                false);
    }

    private ExecutionResult executeMessage(
            BatchApprovedExecutorSnapshot definition,
            Map<String, Object> parameters,
            long executionId,
            String transactionId,
            String segmentId) {
        CpfBrokerClient client = brokerClient.getIfAvailable();
        if (client == null) {
            return ExecutionResult.failed("CAPABILITY_UNAVAILABLE",
                    "CpfBrokerClient capability is not installed", false);
        }
        String topic = definition.executorReference().startsWith("MESSAGE:")
                ? definition.executorReference().substring("MESSAGE:".length())
                : definition.executorReference();
        if (topic.isBlank()) {
            return ExecutionResult.failed("INVALID_MESSAGE_REFERENCE", "Message topic is empty", false);
        }
        String messageId = definition.jobId() + "-" + executionId;
        byte[] payload = jsonBody(parameters.getOrDefault("payload", parameters))
                .getBytes(StandardCharsets.UTF_8);
        CpfBrokerPublishResult result = client.enqueue(new CpfBrokerPublishRequest(
                messageId, topic, text(parameters.get("key"), messageId), payload,
                "application/json", transactionId, segmentId, "cpf-batch",
                text(parameters.get("consumerModule"), ""), messageId, Map.of(),
                Map.of("jobId", definition.jobId(),
                        "definitionVersion", Long.toString(definition.definitionVersion()))));
        String status = result.status().toUpperCase(Locale.ROOT);
        if (Set.of("ENQUEUED", "ACCEPTED", "SUCCESS", "PUBLISHED").contains(status)) {
            return ExecutionResult.completed(blankTo(result.detail(), result.messageId()), 1);
        }
        boolean unknown = "UNKNOWN".equals(status) || "PENDING".equals(status);
        return ExecutionResult.failed("BROKER_" + status,
                blankTo(result.detail(), "Broker enqueue failed"), unknown);
    }

    private ExecutionResult executeProtocol(
            BatchApprovedExecutorSnapshot definition,
            Map<String, Object> parameters) throws Exception {
        String reference = definition.executorReference();
        String uriText;
        if (reference.startsWith("PROTOCOL:HTTP:")) {
            uriText = reference.substring("PROTOCOL:HTTP:".length());
        } else if (reference.startsWith("HTTP:")) {
            uriText = reference.substring("HTTP:".length());
        } else {
            return ExecutionResult.failed("CAPABILITY_UNAVAILABLE",
                    "Protocol Adapter is not installed for reference: " + reference, false);
        }
        URI uri = URI.create(uriText);
        String method = text(parameters.get("httpMethod"), "POST").toUpperCase(Locale.ROOT);
        if (!operationalProperties.getOutboundHttp().getAllowedMethods().contains(method)) {
            return ExecutionResult.failed("PROTOCOL_METHOD_DENIED", method, false);
        }
        byte[] body = ("GET".equals(method) || "DELETE".equals(method))
                ? new byte[0]
                : jsonBody(parameters.getOrDefault("body", parameters)).getBytes(StandardCharsets.UTF_8);
        BatchOutboundHttpPolicy.ApprovedTarget target = outboundPolicy.approve(uri, body.length);
        String idempotencyKey = text(parameters.get("idempotencyKey"), stableProtocolKey(definition, method, uri, body));
        String reconcileKey = text(parameters.get("reconcileKey"), idempotencyKey);
        Map<String, String> headers = requestHeaders(parameters);
        headers.putIfAbsent("Content-Type", text(parameters.get("contentType"), "application/json"));
        headers.put("X-Cpf-Idempotency-Key", idempotencyKey);
        headers.put("X-Cpf-Reconcile-Key", reconcileKey);
        int maxAttempts = Math.max(1, Math.min(definition.maxAttempts(),
                operationalProperties.getOutboundHttp().getMaxAttempts()));
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                PinnedBatchHttpTransport.Response response = outboundTransport.exchange(target, method, body, headers);
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    return ExecutionResult.completed(response.body(), attempt);
                }
                if (response.statusCode() >= 500 && attempt < maxAttempts) {
                    pauseProtocol(attempt);
                    continue;
                }
                return ExecutionResult.failed("PROTOCOL_HTTP_" + response.statusCode(),
                        mask(response.body()), false, attempt);
            } catch (java.net.SocketTimeoutException timeout) {
                if (attempt < maxAttempts) { pauseProtocol(attempt); continue; }
                return ExecutionResult.failed("PROTOCOL_TIMEOUT_UNKNOWN",
                        "Protocol result is unknown; reconcileKey=" + reconcileKey, true, attempt);
            } catch (java.io.IOException transportFailure) {
                if (attempt < maxAttempts) { pauseProtocol(attempt); continue; }
                return ExecutionResult.failed("PROTOCOL_TRANSPORT_UNKNOWN",
                        "Protocol result is unknown; reconcileKey=" + reconcileKey, true, attempt);
            } catch (SecurityException denied) {
                return ExecutionResult.failed("PROTOCOL_POLICY_DENIED", denied.getMessage(), false, attempt);
            }
        }
        return ExecutionResult.failed("PROTOCOL_UNKNOWN", "Protocol result is unknown", true, maxAttempts);
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> requestHeaders(Map<String, Object> parameters) {
        Object value = parameters.get("headers");
        if (value == null) return new LinkedHashMap<>();
        if (!(value instanceof Map<?, ?> raw)) {
            throw new SecurityException("PROTOCOL_HEADERS_INVALID");
        }
        Map<String, String> headers = new LinkedHashMap<>();
        raw.forEach((name, headerValue) -> {
            String key = Objects.toString(name, "").trim();
            String text = Objects.toString(headerValue, "");
            if (key.isEmpty() || key.indexOf('\r') >= 0 || key.indexOf('\n') >= 0
                    || text.indexOf('\r') >= 0 || text.indexOf('\n') >= 0) {
                throw new SecurityException("BATCH_OUTBOUND_HEADER_INJECTION_DENIED");
            }
            String normalized = key.toLowerCase(Locale.ROOT);
            if (!operationalProperties.getOutboundHttp().getAllowedRequestHeaders().contains(normalized)) {
                throw new SecurityException("BATCH_OUTBOUND_HEADER_DENIED:" + normalized);
            }
            headers.put(key, text);
        });
        return headers;
    }

    private void pauseProtocol(int attempt) {
        long base = operationalProperties.getOutboundHttp().getRetryBackoffMillis();
        long delay = Math.min(60_000L, base * (1L << Math.min(10, Math.max(0, attempt - 1))));
        if (delay <= 0) return;
        try {
            Thread.sleep(delay);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("PROTOCOL_RETRY_INTERRUPTED", interrupted);
        }
    }

    private static String stableProtocolKey(BatchApprovedExecutorSnapshot definition, String method, URI uri, byte[] body) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(definition.jobId().getBytes(StandardCharsets.UTF_8));
            digest.update(Long.toString(definition.definitionVersion()).getBytes(StandardCharsets.UTF_8));
            digest.update(method.getBytes(StandardCharsets.UTF_8));
            digest.update(uri.normalize().toASCIIString().getBytes(StandardCharsets.UTF_8));
            digest.update(body);
            return "cpf-batch-" + HexFormat.of().formatHex(digest.digest());
        } catch (Exception impossible) {
            throw new IllegalStateException("PROTOCOL_IDEMPOTENCY_KEY_FAILED", impossible);
        }
    }

    private static String mask(String value) {
        if (value == null) return "";
        return value.replaceAll("(?i)(authorization|token|secret|password)\\s*[:=]\\s*[^,}\\s]+", "$1=***");
    }

    /**
     * Map#toString 형태의 유사 JSON을 금지하고, 모든 외부 Payload를 동일 Canonical JSON으로 직렬화합니다.
     * String 입력도 JSON 문법을 다시 파싱하므로 single quote, Java Map 표현식, trailing garbage를 허용하지 않습니다.
     */
    String jsonBody(Object value) {
        try {
            if (value == null) {
                return "{}";
            }
            Object normalized = value;
            if (value instanceof String string) {
                if (string.isBlank()) {
                    return "{}";
                }
                JsonNode parsed = canonicalJson.readTree(string);
                if (parsed == null) {
                    throw new IllegalArgumentException("JSON payload is empty");
                }
                normalized = parsed;
            }
            return canonicalJson.writeValueAsString(normalized);
        } catch (JsonProcessingException invalidJson) {
            throw new IllegalArgumentException("Payload is not valid JSON", invalidJson);
        }
    }

    private static String text(Object value, String fallback) {
        String result = Objects.toString(value, "").trim();
        return result.isEmpty() ? fallback : result;
    }

    private static String blankTo(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private record ServiceReference(String serviceId, String endpointCode) {
        static ServiceReference parse(String reference) {
            if (reference == null || !reference.startsWith("SERVICE:")) {
                throw new IllegalArgumentException("SERVICE_CALL requires SERVICE:<serviceId>[:endpointCode]");
            }
            String[] parts = reference.substring("SERVICE:".length()).split(":", 2);
            if (parts[0].isBlank()) throw new IllegalArgumentException("serviceId is empty");
            return new ServiceReference(parts[0], parts.length > 1 && !parts[1].isBlank() ? parts[1] : null);
        }
    }

    public record ExecutionResult(
            String status,
            String code,
            String message,
            boolean unknownResult,
            int attemptCount) {
        public static ExecutionResult completed(String message, Integer attempts) {
            return new ExecutionResult("COMPLETED", "OK", message, false,
                    attempts == null ? 1 : Math.max(1, attempts));
        }
        public static ExecutionResult failed(String code, String message, boolean unknown) {
            return failed(code, message, unknown, 1);
        }
        public static ExecutionResult failed(String code, String message, boolean unknown, int attempts) {
            return new ExecutionResult(unknown ? "UNKNOWN_RESULT" : "FAILED",
                    blankTo(code, unknown ? "UNKNOWN_RESULT" : "FAILED"),
                    blankTo(message, "Execution failed"), unknown, Math.max(1, attempts));
        }
    }
}

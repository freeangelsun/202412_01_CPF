package com.cpf.admin.opr.gateway;

import com.cpf.admin.opr.context.AdmAuthenticatedOperatorContext;
import com.cpf.core.api.gateway.CpfGatewayControlHeaders;
import com.cpf.core.api.gateway.CpfGatewayControlSigner;
import com.cpf.core.api.gateway.CpfGatewayRegistryPort;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URLEncoder;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

/** 분리 WAS의 Gateway Owner 내부 API를 동일 Typed Port로 제공하는 ADM Remote Adapter입니다. */
public final class RemoteCpfGatewayRegistryAdapter implements CpfGatewayRegistryPort {
    private static final String ROOT = "/internal/v1/gateway/registry";
    private final WebClient client;
    private final AdmAuthenticatedOperatorContext actorContext;
    private final ObjectMapper mapper;
    private final String sharedSecret;
    private final String keyId;
    private final String audience;
    private final Duration overallTimeout;

    public RemoteCpfGatewayRegistryAdapter(
            WebClient.Builder builder,
            AdmAuthenticatedOperatorContext actorContext,
            ObjectMapper mapper,
            String baseUrl,
            String sharedSecret,
            String keyId,
            String audience,
            Duration overallTimeout) {
        this.client = builder.baseUrl(required(baseUrl, "baseUrl")).build();
        this.actorContext = actorContext;
        this.mapper = mapper;
        this.sharedSecret = required(sharedSecret, "sharedSecret");
        this.keyId = required(keyId, "keyId");
        this.audience = required(audience, "audience");
        this.overallTimeout = Objects.requireNonNull(overallTimeout, "overallTimeout");
        if (this.sharedSecret.length() < 32) {
            throw new IllegalArgumentException("Gateway Control sharedSecret은 32자 이상이어야 합니다.");
        }
        if (overallTimeout.isZero() || overallTimeout.isNegative() || overallTimeout.compareTo(Duration.ofMinutes(2)) > 0) {
            throw new IllegalArgumentException("Gateway Control overallTimeout은 1ms~2분 범위여야 합니다.");
        }
    }

    @Override
    public List<ServerGroup> findServerGroups(String environmentCode, String serviceId, String status, int limit) {
        String target = ROOT + "/server-groups" + query(
                "environmentCode", environmentCode, "serviceId", serviceId,
                "status", status, "limit", Integer.toString(limit));
        return list(invoke(HttpMethod.GET, target, null), ServerGroup.class);
    }

    @Override
    public List<GroupMember> findMembers(String serverGroupId) {
        return list(invoke(HttpMethod.GET, ROOT + "/server-groups/" + encode(serverGroupId) + "/members", null),
                GroupMember.class);
    }

    @Override
    public List<GatewayBinding> findBindings(String environmentCode, String routeId, String status, int limit) {
        String target = ROOT + "/bindings" + query(
                "environmentCode", environmentCode, "routeId", routeId,
                "status", status, "limit", Integer.toString(limit));
        return list(invoke(HttpMethod.GET, target, null), GatewayBinding.class);
    }

    @Override
    public List<ApplyStatus> findApplyStatuses(String bindingId, int limit) {
        return list(invoke(HttpMethod.GET, ROOT + "/bindings/" + encode(bindingId)
                + "/apply-status" + query("limit", Integer.toString(limit)), null), ApplyStatus.class);
    }

    @Override
    public List<ConnectionTestResult> findConnectionTests(String bindingId, int limit) {
        return list(invoke(HttpMethod.GET, ROOT + "/bindings/" + encode(bindingId)
                + "/connection-tests" + query("limit", Integer.toString(limit)), null), ConnectionTestResult.class);
    }

    @Override
    public MutationResult saveServerGroup(ServerGroupCommand command) {
        return convert(invoke(HttpMethod.POST, ROOT + "/server-groups", command), MutationResult.class);
    }

    @Override
    public MutationResult saveBinding(GatewayBindingCommand command) {
        return convert(invoke(HttpMethod.POST, ROOT + "/bindings", command), MutationResult.class);
    }

    @Override
    public MutationResult changeBindingState(BindingStateCommand command) {
        return convert(invoke(HttpMethod.POST, ROOT + "/bindings/" + encode(command.bindingId()) + "/state", command),
                MutationResult.class);
    }

    @Override
    public void deleteServerGroup(String serverGroupId, DeleteCommand command) {
        invoke(HttpMethod.DELETE, ROOT + "/server-groups/" + encode(serverGroupId), command);
    }

    @Override
    public void deleteBinding(String bindingId, DeleteCommand command) {
        invoke(HttpMethod.DELETE, ROOT + "/bindings/" + encode(bindingId), command);
    }

    @Override
    public ApplyStatus acknowledge(ApplyAckCommand command) {
        return convert(invoke(HttpMethod.POST, ROOT + "/apply-ack", command), ApplyStatus.class);
    }

    @Override
    public ConnectionTestResult recordConnectionTest(ConnectionTestCommand command) {
        return convert(invoke(HttpMethod.POST, ROOT + "/connection-test-results", command), ConnectionTestResult.class);
    }

    @Override
    public ConnectionTestOperation requestConnectionTest(ConnectionTestRequest command) {
        return convert(invoke(HttpMethod.POST, ROOT + "/connection-test-operations", command),
                ConnectionTestOperation.class);
    }

    @Override
    public ConnectionTestOperation findConnectionTestOperation(String operationId) {
        return convert(invoke(HttpMethod.GET, ROOT + "/connection-test-operations/" + encode(operationId), null),
                ConnectionTestOperation.class);
    }

    @Override
    public ConnectionTestOperation cancelConnectionTest(ConnectionTestCancel command) {
        return convert(invoke(HttpMethod.POST, ROOT + "/connection-test-operations/"
                + encode(command.operationId()) + "/cancel", command), ConnectionTestOperation.class);
    }

    @Override
    public ConnectionTestOperation revalidateConnectionTest(ConnectionTestRevalidation command) {
        return convert(invoke(HttpMethod.POST, ROOT + "/connection-test-operations/"
                + encode(command.sourceOperationId()) + "/revalidate", command), ConnectionTestOperation.class);
    }

    @Override
    public List<ConnectionTestOperation> claimConnectionTests(String gatewayInstanceId, int limit) {
        return list(invoke(HttpMethod.POST, ROOT + "/connection-test-operations/claim",
                new ClaimConnectionTests(gatewayInstanceId, limit)), ConnectionTestOperation.class);
    }

    @Override
    public ConnectionTestOperation completeConnectionTest(ConnectionTestCompletion command) {
        return convert(invoke(HttpMethod.POST, ROOT + "/connection-test-operations/"
                + encode(command.operationId()) + "/complete", command), ConnectionTestOperation.class);
    }

    @Override
    public List<HealthProbeTarget> claimHealthProbes(String gatewayInstanceId, int limit, long leaseSeconds) {
        return list(invoke(HttpMethod.POST, ROOT + "/health-probes/claim",
                new ClaimHealthProbes(gatewayInstanceId, limit, leaseSeconds)), HealthProbeTarget.class);
    }

    @Override
    public HealthProbeTarget claimHealthProbe(
            String serverGroupId, String instanceId, String gatewayInstanceId, long leaseSeconds) {
        Object value = invoke(HttpMethod.POST, ROOT + "/health-probes/claim-one",
                new ClaimHealthProbe(serverGroupId, instanceId, gatewayInstanceId, leaseSeconds));
        return value == null ? null : convert(value, HealthProbeTarget.class);
    }

    @Override
    public void reportHealth(HealthProbeResult command) {
        invoke(HttpMethod.POST, ROOT + "/health-probes/report", command);
    }

    @Override
    public OperationsSnapshot operationsSnapshot() {
        return convert(invoke(HttpMethod.GET, ROOT + "/operations/snapshot", null), OperationsSnapshot.class);
    }

    @Override
    public List<OperationsEvent> operationsEvents(String afterEventId, int limit) {
        String target = ROOT + "/operations/events" + query(
                "afterEventId", afterEventId, "limit", Integer.toString(limit));
        return list(invoke(HttpMethod.GET, target, null), OperationsEvent.class);
    }

    private Object invoke(HttpMethod method, String requestTarget, Object body) {
        String operator = required(actorContext.currentOperatorId(), "authenticated operator");
        long timestamp = System.currentTimeMillis();
        String nonce = UUID.randomUUID().toString();
        byte[] bodyBytes = serialize(body);
        String contentType = body == null ? "" : MediaType.APPLICATION_JSON_VALUE;
        String bodySha = CpfGatewayControlSigner.sha256(bodyBytes);
        String signature = CpfGatewayControlSigner.sign(
                sharedSecret, method.name(), requestTarget, contentType, bodySha,
                "ADM", operator, timestamp, nonce, audience, keyId);

        WebClient.RequestBodySpec request = client.method(method).uri(requestTarget).headers(headers -> {
            headers.set(CpfGatewayControlHeaders.CALLER_SERVICE, "ADM");
            headers.set(CpfGatewayControlHeaders.OPERATOR_ID, operator);
            headers.set(CpfGatewayControlHeaders.TIMESTAMP, Long.toString(timestamp));
            headers.set(CpfGatewayControlHeaders.NONCE, nonce);
            headers.set(CpfGatewayControlHeaders.CONTENT_SHA256, bodySha);
            headers.set(CpfGatewayControlHeaders.AUDIENCE, audience);
            headers.set(CpfGatewayControlHeaders.KEY_ID, keyId);
            headers.set(CpfGatewayControlHeaders.SIGNATURE, signature);
            if (body != null) headers.setContentType(MediaType.APPLICATION_JSON);
        });
        WebClient.RequestHeadersSpec<?> prepared = body == null ? request : request.bodyValue(bodyBytes);
        try {
            return prepared.exchangeToMono(response -> {
                if (response.statusCode().is2xxSuccessful()) {
                    if (response.statusCode().value() == 204) return reactor.core.publisher.Mono.empty();
                    return response.bodyToMono(byte[].class).map(this::deserialize);
                }
                return response.bodyToMono(String.class).defaultIfEmpty("").flatMap(message -> {
                    int status = response.statusCode().value();
                    CpfGatewayRemoteCallException.ResultState state = status >= 400 && status < 500
                            ? CpfGatewayRemoteCallException.ResultState.REJECTED
                            : mutation(method)
                                    ? CpfGatewayRemoteCallException.ResultState.UNKNOWN_RESULT
                                    : CpfGatewayRemoteCallException.ResultState.FAILED;
                    return reactor.core.publisher.Mono.error(new CpfGatewayRemoteCallException(
                            state, status, state == CpfGatewayRemoteCallException.ResultState.UNKNOWN_RESULT,
                            "Gateway Owner call failed: " + sanitize(message), null));
                });
            }).timeout(overallTimeout).block(overallTimeout.plusMillis(100));
        } catch (CpfGatewayRemoteCallException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            Throwable root = rootCause(ex);
            boolean timeout = root instanceof TimeoutException
                    || root.getClass().getSimpleName().toLowerCase(java.util.Locale.ROOT).contains("timeout");
            CpfGatewayRemoteCallException.ResultState state = mutation(method)
                    ? CpfGatewayRemoteCallException.ResultState.UNKNOWN_RESULT
                    : CpfGatewayRemoteCallException.ResultState.FAILED;
            throw new CpfGatewayRemoteCallException(
                    state, null, mutation(method),
                    timeout ? "Gateway Owner 응답 시간이 초과되었습니다." : "Gateway Owner 연결에 실패했습니다.", ex);
        }
    }

    private byte[] serialize(Object body) {
        if (body == null) return new byte[0];
        try { return mapper.writeValueAsBytes(body); }
        catch (java.io.IOException ex) { throw new IllegalArgumentException("Gateway Control body 직렬화에 실패했습니다.", ex); }
    }

    private Object deserialize(byte[] body) {
        if (body == null || body.length == 0) return null;
        try { return mapper.readValue(body, Object.class); }
        catch (java.io.IOException ex) { throw new IllegalStateException("Gateway Owner 응답 역직렬화에 실패했습니다.", ex); }
    }

    private static boolean mutation(HttpMethod method) {
        return !(HttpMethod.GET.equals(method) || HttpMethod.HEAD.equals(method) || HttpMethod.OPTIONS.equals(method));
    }

    private static Throwable rootCause(Throwable failure) {
        Throwable current = failure;
        while (current.getCause() != null && current.getCause() != current) current = current.getCause();
        return current;
    }

    private <T> T convert(Object value, Class<T> type) {
        if (value == null) throw new IllegalStateException(type.getSimpleName() + " 응답이 비어 있습니다.");
        return mapper.convertValue(value, type);
    }

    private <T> List<T> list(Object value, Class<T> elementType) {
        if (value == null) return List.of();
        JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, elementType);
        return List.copyOf(mapper.convertValue(value, type));
    }

    private static String query(String... pairs) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < pairs.length; i += 2) {
            String value = pairs[i + 1];
            if (value == null || value.isBlank()) continue;
            result.append(result.isEmpty() ? '?' : '&')
                    .append(encode(pairs[i])).append('=').append(encode(value));
        }
        return result.toString();
    }

    private static String encode(String value) {
        return URLEncoder.encode(required(value, "value"), StandardCharsets.UTF_8);
    }

    private static String sanitize(String value) {
        String result = value == null ? "" : value.replaceAll("(?i)(password|token|secret)\\s*[:=]\\s*[^,}\\s]+", "$1=***");
        return result.length() > 1_000 ? result.substring(0, 1_000) : result;
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        return value.trim();
    }

    private record ClaimConnectionTests(String gatewayInstanceId, int limit) {}
    private record ClaimHealthProbes(String gatewayInstanceId, int limit, long leaseSeconds) {}
    private record ClaimHealthProbe(String serverGroupId, String instanceId, String gatewayInstanceId, long leaseSeconds) {}
}

package com.cpf.core.api.gateway;

import java.util.Objects;

/**
 * CPF Gateway가 사용하는 Versioned External Route 계약입니다.
 *
 * <p>기존 8개 필드 생성자는 Source Compatibility를 위해 비활성 Draft로만 유지합니다. 신규 Route는 Server Group,
 * Ingress/Target Protocol, Timeout Budget, Retry 안전성, 보안·Health·Failover 정책을 명시해야 합니다.</p>
 */
public record CpfGatewayRoute(
        String standardExecutionId,
        String serviceId,
        String httpMethod,
        String endpoint,
        String operationId,
        String requiredPermission,
        boolean auditReasonRequired,
        String routeVersion,
        String routeId,
        String environmentCode,
        String hostPattern,
        String pathPattern,
        String apiVersion,
        String serverGroupId,
        CpfGatewayProtocol ingressProtocol,
        CpfGatewayProtocol targetProtocol,
        String tlsPolicyId,
        String authenticationPolicyId,
        String authorizationPolicyId,
        String headerPolicyId,
        String rateLimitPolicyId,
        String healthPolicyId,
        int connectTimeoutMs,
        int responseTimeoutMs,
        int overallTimeoutMs,
        int maxRetryCount,
        boolean idempotent,
        String failoverGroupId,
        boolean enabled,
        long expectedVersion) {

    public CpfGatewayRoute(
            String standardExecutionId, String serviceId, String httpMethod, String endpoint,
            String operationId, String requiredPermission, boolean auditReasonRequired, String routeVersion) {
        this(standardExecutionId, serviceId, httpMethod, endpoint, operationId, requiredPermission,
                auditReasonRequired, routeVersion, standardExecutionId, "DEFAULT", "*", endpoint, routeVersion,
                "", CpfGatewayProtocol.HTTP, CpfGatewayProtocol.HTTP, "", "", "", "", "", "",
                3_000, 10_000, 15_000, 0, false, "", false, 0L);
    }

    public CpfGatewayRoute {
        routeId = required(routeId, "routeId");
        serviceId = required(serviceId, "serviceId");
        pathPattern = required(pathPattern, "pathPattern");
        ingressProtocol = Objects.requireNonNullElse(ingressProtocol, CpfGatewayProtocol.HTTP);
        targetProtocol = Objects.requireNonNullElse(targetProtocol, CpfGatewayProtocol.HTTP);
        environmentCode = blankTo(environmentCode, "DEFAULT");
        hostPattern = blankTo(hostPattern, "*");
        apiVersion = blankTo(apiVersion, "v1");
        routeVersion = blankTo(routeVersion, "1");
        serverGroupId = Objects.requireNonNullElse(serverGroupId, "").trim();
        tlsPolicyId = clean(tlsPolicyId); authenticationPolicyId = clean(authenticationPolicyId);
        authorizationPolicyId = clean(authorizationPolicyId); headerPolicyId = clean(headerPolicyId);
        rateLimitPolicyId = clean(rateLimitPolicyId); healthPolicyId = clean(healthPolicyId);
        failoverGroupId = clean(failoverGroupId);
        if (connectTimeoutMs <= 0 || responseTimeoutMs <= 0 || overallTimeoutMs <= 0) {
            throw new IllegalArgumentException("Timeout budget must be positive");
        }
        if (overallTimeoutMs < connectTimeoutMs || overallTimeoutMs < responseTimeoutMs) {
            throw new IllegalArgumentException("Overall timeout must cover connect/response timeout");
        }
        if (maxRetryCount < 0) throw new IllegalArgumentException("maxRetryCount must be >= 0");
        if (maxRetryCount > 0 && !idempotent) throw new IllegalArgumentException("Retry requires idempotent route");
        if (ingressProtocol.tls() && tlsPolicyId.isBlank()) throw new IllegalArgumentException("TLS ingress requires tlsPolicyId");
        if (enabled && serverGroupId.isBlank()) throw new IllegalArgumentException("Enabled route requires serverGroupId");
    }

    /** Target upstream에 전달할 Path Template입니다. endpoint record component는 Source Compatibility용 별칭입니다. */
    public String targetPath() { return endpoint; }

    public String routeKey() {
        return environmentCode + '|' + hostPattern + '|' + pathPattern + '|' + blankTo(httpMethod, "*") + '|' + apiVersion;
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        return value.trim();
    }
    private static String clean(String value) { return Objects.requireNonNullElse(value, "").trim(); }
    private static String blankTo(String value, String fallback) { return value == null || value.isBlank() ? fallback : value.trim(); }
}

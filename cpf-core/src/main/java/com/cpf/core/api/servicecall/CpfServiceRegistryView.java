package com.cpf.core.api.servicecall;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Service Registry의 공개 조회 모델입니다.
 *
 * <p>DB 컬럼명이나 {@code Map<String,Object>}를 Controller 밖으로 노출하지 않고,
 * ADM·Gateway·외부 운영 Client가 동일한 Versioned 계약을 사용하게 합니다.</p>
 */
public final class CpfServiceRegistryView {
    private CpfServiceRegistryView() {}

    public record Service(
            String serviceId,
            String serviceName,
            String serviceType,
            String ownerModuleCode,
            String description,
            boolean enabled,
            long version,
            OffsetDateTime updatedAt) {
        public static Service from(Map<String, Object> row) {
            return new Service(text(row, "service_id", "serviceId"), text(row, "service_name", "serviceName"),
                    text(row, "service_type", "serviceType"), text(row, "owner_module_code", "ownerModuleCode"),
                    text(row, "description"), yes(row, "use_yn", "useYn"), number(row, "row_version", "version"),
                    instant(row, "updated_at", "updatedAt"));
        }
    }

    public record Endpoint(
            String endpointCode,
            String serviceId,
            String endpointName,
            String endpointType,
            String baseUrl,
            String contextPath,
            int defaultTimeoutMs,
            int defaultRetryCount,
            boolean enabled,
            long version,
            OffsetDateTime updatedAt) {
        public static Endpoint from(Map<String, Object> row) {
            return new Endpoint(text(row, "endpoint_code", "endpointCode"), text(row, "service_id", "serviceId"),
                    text(row, "endpoint_name", "endpointName"), text(row, "endpoint_type", "endpointType"),
                    text(row, "base_url", "baseUrl"), text(row, "context_path", "contextPath"),
                    integer(row, "default_timeout_ms", "defaultTimeoutMs"),
                    integer(row, "default_retry_count", "defaultRetryCount"), yes(row, "use_yn", "useYn"),
                    number(row, "row_version", "version"), instant(row, "updated_at", "updatedAt"));
        }
    }

    public record Instance(
            String instanceId,
            String serviceId,
            String endpointCode,
            String instanceName,
            String baseUrl,
            String hostName,
            int port,
            String environmentCode,
            String zoneCode,
            String cellCode,
            String status,
            int weight,
            int priority,
            boolean active,
            boolean maintenance,
            boolean draining,
            long version,
            OffsetDateTime lastHeartbeatAt,
            OffsetDateTime updatedAt) {
        public static Instance from(Map<String, Object> row) {
            return new Instance(text(row, "instance_id", "instanceId"), text(row, "service_id", "serviceId"),
                    text(row, "endpoint_code", "endpointCode"), text(row, "instance_name", "instanceName"),
                    text(row, "base_url", "baseUrl"), text(row, "host_name", "hostName"), integer(row, "port_no", "portNo"),
                    text(row, "environment_code", "environmentCode"), text(row, "zone_code", "zoneCode"),
                    text(row, "cell_code", "cellCode"), text(row, "instance_status", "status"),
                    integer(row, "weight"), integer(row, "priority_no", "priorityNo"), yes(row, "active_yn", "activeYn"),
                    yes(row, "maintenance_yn", "maintenanceYn"), yes(row, "drain_yn", "drainYn"),
                    number(row, "row_version", "version"), instant(row, "last_heartbeat_at", "lastHeartbeatAt"),
                    instant(row, "updated_at", "updatedAt"));
        }
    }

    public record Health(
            String healthId,
            String serviceId,
            String endpointCode,
            String instanceId,
            String status,
            Integer protocolStatus,
            Long responseTimeMs,
            String failureMessage,
            OffsetDateTime checkedAt) {
        public static Health from(Map<String, Object> row) {
            return new Health(text(row, "health_id", "healthId"), text(row, "service_id", "serviceId"),
                    text(row, "endpoint_code", "endpointCode"), text(row, "instance_id", "instanceId"),
                    text(row, "health_status", "status"), nullableInteger(row, "http_status", "protocolStatus"),
                    nullableLong(row, "response_time_ms", "responseTimeMs"), text(row, "failure_message", "failureMessage"),
                    instant(row, "checked_at", "checkedAt"));
        }
    }

    public record RoutingPolicy(
            String policyId,
            String serviceId,
            String endpointCode,
            String routingMode,
            String loadBalanceType,
            boolean failoverEnabled,
            boolean healthRequired,
            int priority,
            boolean active) {
        public static RoutingPolicy from(Map<String, Object> row) {
            return new RoutingPolicy(text(row, "policy_id", "policyId"), text(row, "service_id", "serviceId"),
                    text(row, "endpoint_code", "endpointCode"), text(row, "routing_mode", "routingMode"),
                    text(row, "load_balance_type", "loadBalanceType"), yes(row, "failover_enabled_yn", "failoverEnabledYn"),
                    yes(row, "health_check_required_yn", "healthCheckRequiredYn"), integer(row, "priority"),
                    yes(row, "active_yn", "activeYn"));
        }
    }

    public record CircuitState(
            String serviceId,
            String endpointCode,
            String instanceId,
            String state,
            int failureCount,
            OffsetDateTime openedAt,
            OffsetDateTime updatedAt) {
        public static CircuitState from(Map<String, Object> row) {
            return new CircuitState(text(row, "service_id", "serviceId"), text(row, "endpoint_code", "endpointCode"),
                    text(row, "instance_id", "instanceId"), text(row, "circuit_state", "state"),
                    integer(row, "failure_count", "failureCount"), instant(row, "opened_at", "openedAt"),
                    instant(row, "updated_at", "updatedAt"));
        }
    }

    public record CallHistory(
            String callId,
            String transactionId,
            String traceId,
            String serviceId,
            String endpointCode,
            String instanceId,
            String method,
            String requestPath,
            String status,
            Integer protocolStatus,
            long durationMs,
            int retryCount,
            String failureCode,
            String failureMessage,
            OffsetDateTime createdAt) {
        public static CallHistory from(Map<String, Object> row) {
            return new CallHistory(text(row, "call_id", "callId"), text(row, "transaction_id", "transactionId"),
                    text(row, "trace_id", "traceId"), text(row, "service_id", "serviceId"),
                    text(row, "endpoint_code", "endpointCode"), text(row, "instance_id", "instanceId"),
                    text(row, "http_method", "method"), text(row, "request_path", "requestPath"),
                    text(row, "call_status", "status"), nullableInteger(row, "http_status", "protocolStatus"),
                    number(row, "duration_ms", "durationMs"), integer(row, "retry_count", "retryCount"),
                    text(row, "failure_code", "failureCode"), text(row, "failure_message", "failureMessage"),
                    instant(row, "created_at", "createdAt"));
        }
    }

    public record MutationResult(
            String resourceType,
            String resourceId,
            String operationId,
            String status,
            long version,
            OffsetDateTime changedAt) {
        public static MutationResult from(String resourceType, String resourceId, Map<String, Object> row) {
            return new MutationResult(resourceType, resourceId, text(row, "operation_id", "operationId"),
                    blankTo(text(row, "status", "result"), "APPLIED"), number(row, "row_version", "version"),
                    blankInstant(instant(row, "updated_at", "changedAt")));
        }
    }

    private static String text(Map<String, Object> row, String... keys) {
        Object value = value(row, keys);
        return value == null ? "" : String.valueOf(value);
    }

    private static boolean yes(Map<String, Object> row, String... keys) {
        String value = text(row, keys).trim().toUpperCase(Locale.ROOT);
        return "Y".equals(value) || "TRUE".equals(value) || "1".equals(value) || "ENABLED".equals(value);
    }

    private static int integer(Map<String, Object> row, String... keys) {
        Long value = nullableLong(row, keys);
        return value == null ? 0 : Math.toIntExact(value);
    }

    private static Integer nullableInteger(Map<String, Object> row, String... keys) {
        Long value = nullableLong(row, keys);
        return value == null ? null : Math.toIntExact(value);
    }

    private static long number(Map<String, Object> row, String... keys) {
        Long value = nullableLong(row, keys);
        return value == null ? 0L : value;
    }

    private static Long nullableLong(Map<String, Object> row, String... keys) {
        Object value = value(row, keys);
        if (value == null || String.valueOf(value).isBlank()) return null;
        if (value instanceof Number number) return number.longValue();
        return Long.parseLong(String.valueOf(value));
    }

    private static OffsetDateTime instant(Map<String, Object> row, String... keys) {
        Object value = value(row, keys);
        if (value == null || String.valueOf(value).isBlank()) return null;
        if (value instanceof OffsetDateTime offset) return offset;
        if (value instanceof java.sql.Timestamp timestamp) return timestamp.toInstant().atOffset(java.time.ZoneOffset.UTC);
        try { return OffsetDateTime.parse(String.valueOf(value)); } catch (RuntimeException ignored) { return null; }
    }

    private static Object value(Map<String, Object> row, String... keys) {
        if (row == null) return null;
        for (String key : keys) {
            if (row.containsKey(key)) return row.get(key);
            String upper = key.toUpperCase(Locale.ROOT);
            if (row.containsKey(upper)) return row.get(upper);
        }
        return null;
    }

    private static String blankTo(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static OffsetDateTime blankInstant(OffsetDateTime value) {
        return Objects.requireNonNullElseGet(value, OffsetDateTime::now);
    }
}

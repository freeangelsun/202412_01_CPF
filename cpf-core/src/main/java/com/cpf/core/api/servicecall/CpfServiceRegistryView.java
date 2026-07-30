package com.cpf.core.api.servicecall;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Service Registry의 엄격한 공개 조회 모델입니다.
 *
 * <p>필수 DB 컬럼 누락, 해석 불가 시각·숫자·Code를 성공/현재시각/0으로 보정하지 않습니다.
 * Owner Adapter가 이 모델로 Decode한 뒤에만 Public Port로 반환할 수 있습니다.</p>
 */
public final class CpfServiceRegistryView {
    private CpfServiceRegistryView() {}

    public record Service(
            String serviceId, String serviceName, String serviceType, String ownerModuleCode,
            String description, boolean enabled, long version, OffsetDateTime updatedAt) {
        public static Service from(Map<String, Object> row) {
            return new Service(
                    requiredText(row, "service_id", "serviceId"),
                    requiredText(row, "service_name", "serviceName"),
                    requiredCode(row, Set.of("INTERNAL", "EXTERNAL", "PLATFORM", "MONITOR_ONLY"),
                            "service_type", "serviceType"),
                    requiredText(row, "owner_module_code", "ownerModuleCode"),
                    optionalText(row, "description"),
                    requiredYesNo(row, "use_yn", "useYn"),
                    requiredLong(row, "row_version", "version"),
                    requiredInstant(row, "updated_at", "updatedAt"));
        }
    }

    public record Endpoint(
            String endpointCode, String serviceId, String endpointName, String endpointType,
            String baseUrl, String contextPath, int defaultTimeoutMs, int defaultRetryCount,
            boolean enabled, long version, OffsetDateTime updatedAt) {
        public static Endpoint from(Map<String, Object> row) {
            return new Endpoint(
                    requiredText(row, "endpoint_code", "endpointCode"),
                    requiredText(row, "service_id", "serviceId"),
                    requiredText(row, "endpoint_name", "endpointName"),
                    requiredCode(row, Set.of("HTTP", "HTTPS", "GRPC", "TCP", "WEBSOCKET", "SSE", "MONITOR_ONLY"),
                            "endpoint_type", "endpointType"),
                    requiredText(row, "base_url", "baseUrl"),
                    optionalText(row, "context_path", "contextPath"),
                    requiredInt(row, "default_timeout_ms", "defaultTimeoutMs"),
                    requiredInt(row, "default_retry_count", "defaultRetryCount"),
                    requiredYesNo(row, "use_yn", "useYn"),
                    requiredLong(row, "row_version", "version"),
                    requiredInstant(row, "updated_at", "updatedAt"));
        }
    }

    public record Instance(
            String instanceId, String serviceId, String endpointCode, String instanceName,
            String baseUrl, String hostName, Integer port, String environmentCode,
            String zoneCode, String cellCode, String status, int weight, int priority,
            boolean active, boolean maintenance, boolean draining, long version,
            OffsetDateTime lastHeartbeatAt, OffsetDateTime updatedAt) {
        public static Instance from(Map<String, Object> row) {
            return new Instance(
                    requiredText(row, "instance_id", "instanceId"),
                    requiredText(row, "service_id", "serviceId"),
                    requiredText(row, "endpoint_code", "endpointCode"),
                    requiredText(row, "instance_name", "instanceName"),
                    requiredText(row, "base_url", "baseUrl"),
                    optionalText(row, "host_name", "hostName"),
                    optionalInt(row, "port_no", "portNo"),
                    requiredText(row, "environment_code", "environmentCode"),
                    optionalText(row, "zone_code", "zoneCode"),
                    optionalText(row, "cell_code", "cellCode"),
                    requiredCode(row, Set.of("UP", "DOWN", "DEGRADED", "UNKNOWN", "DRAINING",
                            "DISABLED", "MAINTENANCE", "STALE", "RECOVERING"),
                            "instance_status", "status"),
                    requiredInt(row, "weight"),
                    requiredInt(row, "priority_no", "priorityNo"),
                    requiredYesNo(row, "active_yn", "activeYn"),
                    requiredYesNo(row, "maintenance_yn", "maintenanceYn"),
                    requiredYesNo(row, "drain_yn", "drainYn"),
                    requiredLong(row, "row_version", "version"),
                    optionalInstant(row, "last_heartbeat_at", "lastHeartbeatAt"),
                    requiredInstant(row, "updated_at", "updatedAt"));
        }
    }

    public record Health(
            String healthId, String serviceId, String endpointCode, String instanceId,
            String status, Integer protocolStatus, Long responseTimeMs, String failureMessage,
            OffsetDateTime checkedAt) {
        public static Health from(Map<String, Object> row) {
            return new Health(
                    requiredText(row, "health_id", "healthId"),
                    requiredText(row, "service_id", "serviceId"),
                    requiredText(row, "endpoint_code", "endpointCode"),
                    requiredText(row, "instance_id", "instanceId"),
                    requiredCode(row, Set.of("UP", "DOWN", "DEGRADED", "UNKNOWN", "STALE", "RECOVERING"),
                            "health_status", "status"),
                    optionalInt(row, "http_status", "protocolStatus"),
                    optionalLong(row, "response_time_ms", "responseTimeMs"),
                    optionalText(row, "failure_message", "failureMessage"),
                    requiredInstant(row, "checked_at", "checkedAt"));
        }
    }

    public record RoutingPolicy(
            String policyId, String serviceId, String endpointCode, String routingMode,
            String loadBalanceType, boolean failoverEnabled, boolean healthRequired,
            int priority, boolean active) {
        public static RoutingPolicy from(Map<String, Object> row) {
            return new RoutingPolicy(
                    requiredText(row, "policy_id", "policyId"),
                    requiredText(row, "service_id", "serviceId"),
                    requiredText(row, "endpoint_code", "endpointCode"),
                    requiredCode(row, Set.of("LOCAL", "REMOTE", "AUTO", "MONITOR_ONLY"),
                            "routing_mode", "routingMode"),
                    requiredCode(row, Set.of("ROUND_ROBIN", "WEIGHTED", "RENDEZVOUS_HASH",
                            "PRIORITY_FAILOVER", "LEAST_LOAD"), "load_balance_type", "loadBalanceType"),
                    requiredYesNo(row, "failover_enabled_yn", "failoverEnabledYn"),
                    requiredYesNo(row, "health_check_required_yn", "healthCheckRequiredYn"),
                    requiredInt(row, "priority"),
                    requiredYesNo(row, "active_yn", "activeYn"));
        }
    }

    public record CircuitState(
            String serviceId, String endpointCode, String instanceId, String state,
            int failureCount, OffsetDateTime openedAt, OffsetDateTime updatedAt) {
        public static CircuitState from(Map<String, Object> row) {
            return new CircuitState(
                    requiredText(row, "service_id", "serviceId"),
                    requiredText(row, "endpoint_code", "endpointCode"),
                    requiredText(row, "instance_id", "instanceId"),
                    requiredCode(row, Set.of("CLOSED", "OPEN", "HALF_OPEN"),
                            "circuit_state", "state"),
                    requiredInt(row, "failure_count", "failureCount"),
                    optionalInstant(row, "opened_at", "openedAt"),
                    requiredInstant(row, "updated_at", "updatedAt"));
        }
    }

    public record CallHistory(
            String callId, String transactionId, String traceId, String serviceId,
            String endpointCode, String instanceId, String method, String requestPath,
            String status, Integer protocolStatus, long durationMs, int retryCount,
            String failureCode, String failureMessage, OffsetDateTime createdAt) {
        public static CallHistory from(Map<String, Object> row) {
            return new CallHistory(
                    requiredText(row, "call_id", "callId"),
                    requiredText(row, "transaction_id", "transactionId"),
                    optionalText(row, "trace_id", "traceId"),
                    requiredText(row, "service_id", "serviceId"),
                    requiredText(row, "endpoint_code", "endpointCode"),
                    optionalText(row, "instance_id", "instanceId"),
                    requiredText(row, "http_method", "method"),
                    requiredText(row, "request_path", "requestPath"),
                    requiredCode(row, Set.of("SUCCESS", "FAILED", "TIMEOUT", "UNKNOWN", "REJECTED"),
                            "call_status", "status"),
                    optionalInt(row, "http_status", "protocolStatus"),
                    requiredLong(row, "duration_ms", "durationMs"),
                    requiredInt(row, "retry_count", "retryCount"),
                    optionalText(row, "failure_code", "failureCode"),
                    optionalText(row, "failure_message", "failureMessage"),
                    requiredInstant(row, "created_at", "createdAt"));
        }
    }

    public record MutationResult(
            String resourceType, String resourceId, String operationId,
            String status, long version, OffsetDateTime changedAt) {
        public MutationResult {
            resourceType = requireValue(resourceType, "resourceType");
            resourceId = requireValue(resourceId, "resourceId");
            operationId = requireValue(operationId, "operationId");
            status = requireValue(status, "status").toUpperCase(Locale.ROOT);
            if (!Set.of("APPLIED", "RETIRED", "DELETED", "NO_CHANGE").contains(status)) {
                throw new IllegalArgumentException("Unsupported mutation status: " + status);
            }
            if (version < 0) throw new IllegalArgumentException("version must be non-negative");
            changedAt = Objects.requireNonNull(changedAt, "changedAt");
        }

        public static MutationResult applied(
                String resourceType, String resourceId, String operationId, Map<String, Object> row) {
            return new MutationResult(resourceType, resourceId, operationId, "APPLIED",
                    requiredLong(row, "row_version", "version"),
                    requiredInstant(row, "updated_at", "changedAt"));
        }

        public static MutationResult stateChanged(
                String resourceId, String operationId, String status, Map<String, Object> row) {
            return new MutationResult("INSTANCE", resourceId, operationId, status,
                    requiredLong(row, "row_version", "version"),
                    requiredInstant(row, "updated_at", "changedAt"));
        }
    }

    private static String requiredCode(Map<String, Object> row, Set<String> allowed, String... keys) {
        String value = requiredText(row, keys).toUpperCase(Locale.ROOT);
        if (!allowed.contains(value)) {
            throw new ContractException("Unsupported code " + value + " for " + String.join("/", keys));
        }
        return value;
    }

    private static String requiredText(Map<String, Object> row, String... keys) {
        Object value = value(row, true, keys);
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) throw new ContractException("Blank required field: " + String.join("/", keys));
        return text;
    }

    private static String optionalText(Map<String, Object> row, String... keys) {
        Object value = value(row, false, keys);
        return value == null ? "" : String.valueOf(value);
    }

    private static boolean requiredYesNo(Map<String, Object> row, String... keys) {
        String value = requiredText(row, keys).toUpperCase(Locale.ROOT);
        if ("Y".equals(value) || "TRUE".equals(value) || "1".equals(value)) return true;
        if ("N".equals(value) || "FALSE".equals(value) || "0".equals(value)) return false;
        throw new ContractException("Invalid boolean code: " + value);
    }

    private static int requiredInt(Map<String, Object> row, String... keys) {
        return Math.toIntExact(requiredLong(row, keys));
    }

    private static Integer optionalInt(Map<String, Object> row, String... keys) {
        Long value = optionalLong(row, keys);
        return value == null ? null : Math.toIntExact(value);
    }

    private static long requiredLong(Map<String, Object> row, String... keys) {
        Object value = value(row, true, keys);
        return parseLong(value, keys);
    }

    private static Long optionalLong(Map<String, Object> row, String... keys) {
        Object value = value(row, false, keys);
        return value == null || String.valueOf(value).isBlank() ? null : parseLong(value, keys);
    }

    private static long parseLong(Object value, String... keys) {
        try {
            if (value instanceof Number number) return number.longValue();
            return Long.parseLong(String.valueOf(value));
        } catch (RuntimeException failure) {
            throw new ContractException("Invalid number field: " + String.join("/", keys), failure);
        }
    }

    private static OffsetDateTime requiredInstant(Map<String, Object> row, String... keys) {
        Object value = value(row, true, keys);
        OffsetDateTime result = parseInstant(value, keys);
        if (result == null) throw new ContractException("Blank required instant: " + String.join("/", keys));
        return result;
    }

    private static OffsetDateTime optionalInstant(Map<String, Object> row, String... keys) {
        Object value = value(row, false, keys);
        return value == null || String.valueOf(value).isBlank() ? null : parseInstant(value, keys);
    }

    private static OffsetDateTime parseInstant(Object value, String... keys) {
        try {
            if (value instanceof OffsetDateTime offset) return offset;
            if (value instanceof java.sql.Timestamp timestamp) {
                return timestamp.toInstant().atOffset(ZoneOffset.UTC);
            }
            if (value instanceof java.time.LocalDateTime local) return local.atOffset(ZoneOffset.UTC);
            return OffsetDateTime.parse(String.valueOf(value));
        } catch (RuntimeException failure) {
            throw new ContractException("Invalid instant field: " + String.join("/", keys), failure);
        }
    }

    private static Object value(Map<String, Object> row, boolean required, String... keys) {
        if (row == null) {
            if (required) throw new ContractException("Registry row is null");
            return null;
        }
        for (String key : keys) {
            if (row.containsKey(key)) return row.get(key);
            String upper = key.toUpperCase(Locale.ROOT);
            if (row.containsKey(upper)) return row.get(upper);
        }
        if (required) throw new ContractException("Missing required field: " + String.join("/", keys));
        return null;
    }

    private static String requireValue(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        return value.trim();
    }

    public static final class ContractException extends IllegalStateException {
        public ContractException(String message) { super(message); }
        public ContractException(String message, Throwable cause) { super(message, cause); }
    }
}

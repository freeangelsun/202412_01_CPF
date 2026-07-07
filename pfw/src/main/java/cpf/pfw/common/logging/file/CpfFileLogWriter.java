package cpf.pfw.common.logging.file;

import cpf.pfw.common.logging.SensitiveDataMasker;
import cpf.pfw.common.logging.ServerInstanceIdentity;
import cpf.pfw.common.logging.TransactionContext;
import cpf.pfw.common.logging.TransactionLogRecord;
import cpf.pfw.common.logging.policy.LogPolicyDecision;
import cpf.pfw.common.logging.segment.TransactionSegmentContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * CPF 구조화 파일 로그를 공통 규격으로 기록합니다.
 *
 * <p>DB 로그는 운영 조회와 통계의 기준이고, 파일 로그는 장애 상황에서 인스턴스별로 빠르게 grep할 수 있는
 * 보조 증적입니다. 모든 모듈은 {@code logs/{moduleCode}/cpf-{moduleCode}-{logType}.log} 규칙을 공유합니다.</p>
 */
@Component
public class CpfFileLogWriter {
    private static final Logger log = LoggerFactory.getLogger(CpfFileLogWriter.class);
    private static final String DEFAULT_PATTERN = "cpf-{moduleCode}-{logType}.log";

    private final Environment environment;

    public CpfFileLogWriter(Environment environment) {
        this.environment = environment;
    }

    /**
     * 온라인 거래 AOP가 수집한 요약 정보를 transaction/error 파일 로그로 남깁니다.
     */
    public void writeTransaction(TransactionLogRecord record, Map<String, String> details, LogPolicyDecision policy) {
        if (record == null || !enabled("transaction")) {
            return;
        }
        String logType = "FAILURE".equalsIgnoreCase(record.getLogType()) ? "error" : "transaction";
        if (!enabled(logType)) {
            return;
        }

        Map<String, Object> event = baseEvent(record.getModuleId(), logType, policy, details);
        event.put("eventType", "ONLINE_TRANSACTION");
        event.put("transactionGlobalId", record.getTransactionId());
        event.put("transactionSegmentId", firstText(detail(details, "transactionSegment.id"), record.getSpanId()));
        event.put("parentSegmentId", firstText(detail(details, "parentSegment.id"), record.getParentSpanId()));
        event.put("transactionRole", "MAIN");
        event.put("direction", "INBOUND");
        event.put("apiPath", record.getUri());
        event.put("httpMethod", record.getHttpMethod());
        event.put("status", record.getLogType());
        event.put("durationMs", record.getDurationMs());
        event.put("failureCode", record.getErrorCode());
        event.put("failureMessageMasked", mask(record.getErrorMessage()));
        event.put("responseCode", record.getResponseCode());
        event.put("httpStatus", record.getHttpStatus());
        event.put("businessTransactionId", record.getBusinessTransactionId());
        event.put("traceId", record.getTraceId());
        event.put("spanId", record.getSpanId());
        event.put("requestHeadersMasked", detail(details, "resolvedHeaders"));
        event.put("responseHeadersMasked", detail(details, "responseHeaders"));
        append(record.getModuleId(), logType, event);
    }

    /**
     * WebClient/외부연계 처리 흐름을 integration 파일 로그로 남깁니다.
     */
    public void writeIntegration(
            String sourceModuleCode,
            String targetModuleCode,
            String direction,
            String httpMethod,
            String apiPath,
            Integer httpStatus,
            String status,
            Long durationMs,
            String failureCode,
            String failureMessage,
            Map<String, Object> attributes) {

        if (!enabled("integration")) {
            return;
        }

        Map<String, Object> event = baseEvent(sourceModuleCode, "integration", null, null);
        event.put("eventType", attributeText(attributes, "eventType", "INTEGRATION"));
        event.put("sourceModuleCode", normalizeModuleCode(sourceModuleCode));
        event.put("targetModuleCode", normalizeModuleCode(targetModuleCode));
        event.put("transactionGlobalId", TransactionContext.getOrCreateTransactionId());
        event.put("transactionSegmentId", firstText(TransactionSegmentContext.currentSegmentId(), TransactionContext.currentSpanId()));
        event.put("parentSegmentId", TransactionContext.currentParentSpanId());
        event.put("transactionRole", "EXTERNAL");
        event.put("direction", defaultText(direction, "OUTBOUND"));
        event.put("apiPath", apiPath);
        event.put("httpMethod", httpMethod);
        event.put("status", defaultText(status, "SUCCESS"));
        event.put("durationMs", durationMs);
        event.put("failureCode", failureCode);
        event.put("failureMessageMasked", mask(failureMessage));
        event.put("httpStatus", httpStatus);

        if (attributes != null) {
            attributes.forEach((key, value) -> event.put(key, sanitizeValue(key, value)));
        }
        append(sourceModuleCode, "integration", event);
    }

    public Map<String, Object> newBaseEvent(String moduleCode, String logType) {
        return baseEvent(moduleCode, logType, null, null);
    }

    public void writeEvent(String moduleCode, String logType, Map<String, Object> event) {
        if (event == null) {
            return;
        }
        append(moduleCode, logType, sanitizeMap(event));
    }

    private Map<String, Object> baseEvent(
            String moduleCode,
            String logType,
            LogPolicyDecision policy,
            Map<String, String> details) {

        ServerInstanceIdentity.Identity identity = ServerInstanceIdentity.current();
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("timestamp", Instant.now().toString());
        event.put("level", policy != null ? defaultText(policy.fileLogLevel(), "INFO") : defaultText(TransactionContext.currentDynamicLogLevel(), "INFO"));
        event.put("logType", logType);
        event.put("moduleCode", normalizeModuleCode(moduleCode));
        event.put("sourceModuleCode", normalizeModuleCode(moduleCode));
        event.put("targetModuleCode", null);
        event.put("traceBoostPolicyId", traceBoostPolicyId(policy, details));
        event.put("logLevelApplied", policy != null ? policy.fileLogLevel() : TransactionContext.currentDynamicLogLevel());
        event.put("serverId", environment.getProperty("cpf.framework.was-id", normalizeModuleCode(moduleCode).toLowerCase(Locale.ROOT) + "AP01"));
        event.put("instanceId", identity.serverInstanceId());
        event.put("serverInstanceId", identity.serverInstanceId());
        event.put("hostName", identity.hostName());
        event.put("hostIp", hostIp());
        event.put("port", environment.getProperty("server.port", "N/A"));
        event.put("processId", identity.processId());
        event.put("threadName", identity.threadName());
        event.put("containerId", environment.getProperty("HOSTNAME", "N/A"));
        event.put("podName", environment.getProperty("POD_NAME", "N/A"));
        event.put("profile", String.join(",", environment.getActiveProfiles()));
        event.put("appVersion", environment.getProperty("cpf.app.version", "local"));
        event.put("buildVersion", environment.getProperty("cpf.build.version", "local"));
        event.put("jvmName", ManagementFactory.getRuntimeMXBean().getName());
        return event;
    }

    private void append(String moduleCode, String logType, Map<String, Object> event) {
        if (!enabled("file") || !enabled(logType)) {
            return;
        }
        try {
            Path logPath = resolveLogPath(moduleCode, logType);
            Files.createDirectories(logPath.getParent());
            Files.writeString(
                    logPath,
                    toJson(event) + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException ex) {
            // 파일 로그 실패가 업무 응답 실패로 전파되지 않도록 표준 로그만 남깁니다.
            log.warn("CPF file log write failed. moduleCode={}, logType={}, error={}", moduleCode, logType, ex.getMessage());
        }
    }

    private Path resolveLogPath(String moduleCode, String logType) {
        String normalizedModule = normalizeModuleCode(moduleCode).toLowerCase(Locale.ROOT);
        String normalizedType = normalizeLogType(logType);
        String pattern = environment.getProperty("cpf.logging.file.file-pattern", DEFAULT_PATTERN)
                .replace("{moduleCode}", normalizedModule)
                .replace("{logType}", normalizedType);
        String basePath = environment.getProperty("cpf.logging.file.base-path", "logs");
        return Path.of(basePath).resolve(normalizedModule).resolve(pattern).normalize();
    }

    private boolean enabled(String key) {
        String normalizedKey = key == null ? "" : key.trim().toLowerCase(Locale.ROOT);
        if ("file".equals(normalizedKey)) {
            return environment.getProperty("cpf.logging.file.enabled", Boolean.class, true);
        }
        return environment.getProperty("cpf.logging.file." + normalizedKey + "-enabled", Boolean.class, true);
    }

    private String moduleCode() {
        String configuredModuleId = environment.getProperty("cpf.framework.module-id");
        if (hasText(configuredModuleId)) {
            return normalizeModuleCode(configuredModuleId);
        }

        String appName = environment.getProperty("spring.application.name");
        if (hasText(appName)) {
            return normalizeModuleCode(appName.replace("cpf-", ""));
        }
        return "PFW";
    }

    private String normalizeModuleCode(String moduleCode) {
        String value = hasText(moduleCode) ? moduleCode : moduleCode();
        value = value.replace("cpf-", "");
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeLogType(String logType) {
        String value = defaultText(logType, "application");
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String traceBoostPolicyId(LogPolicyDecision policy, Map<String, String> details) {
        if (policy != null && policy.overrideId() != null) {
            return String.valueOf(policy.overrideId());
        }
        String dynamicRuleId = detail(details, "dynamicLog.rule.id");
        if (hasText(dynamicRuleId)) {
            return dynamicRuleId;
        }
        return policy != null && policy.policyId() != null ? String.valueOf(policy.policyId()) : null;
    }

    private String detail(Map<String, String> details, String key) {
        return details != null ? details.get(key) : null;
    }

    private String mask(String value) {
        return value == null ? null : SensitiveDataMasker.mask(value);
    }

    private Map<String, Object> sanitizeMap(Map<String, Object> source) {
        Map<String, Object> sanitized = new LinkedHashMap<>();
        source.forEach((key, value) -> sanitized.put(key, sanitizeValue(key, value)));
        return sanitized;
    }

    private Object sanitizeValue(Object key, Object value) {
        if (value == null || value instanceof Number || value instanceof Boolean) {
            return value;
        }
        if (isSensitiveKey(key)) {
            return "***";
        }
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> sanitized = new LinkedHashMap<>();
            map.forEach((nestedKey, nestedValue) -> sanitized.put(String.valueOf(nestedKey), sanitizeValue(nestedKey, nestedValue)));
            return sanitized;
        }
        if (value instanceof Iterable<?> iterable) {
            List<Object> sanitized = new ArrayList<>();
            iterable.forEach(item -> sanitized.add(sanitizeValue(null, item)));
            return sanitized;
        }
        return mask(String.valueOf(value));
    }

    private boolean isSensitiveKey(Object key) {
        if (key == null) {
            return false;
        }
        String normalized = String.valueOf(key)
                .replace("-", "")
                .replace("_", "")
                .toLowerCase(Locale.ROOT);
        return normalized.contains("password")
                || normalized.contains("passwd")
                || normalized.contains("pwd")
                || normalized.contains("token")
                || normalized.contains("authorization")
                || normalized.contains("apikey")
                || normalized.contains("cookie")
                || normalized.contains("secret")
                || normalized.contains("credential")
                || normalized.contains("signature")
                || normalized.contains("accountno")
                || normalized.contains("cardno")
                || normalized.contains("rrn")
                || normalized.contains("ssn")
                || normalized.contains("otp")
                || normalized.contains("pin");
    }

    private String attributeText(Map<String, Object> attributes, String key, String fallback) {
        if (attributes == null || !attributes.containsKey(key) || attributes.get(key) == null) {
            return fallback;
        }
        String value = String.valueOf(attributes.get(key));
        return hasText(value) ? value : fallback;
    }

    private String hostIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception ex) {
            return "unknown";
        }
    }

    private String toJson(Map<String, Object> event) {
        StringBuilder builder = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : event.entrySet()) {
            if (!first) {
                builder.append(',');
            }
            first = false;
            builder.append('"').append(escape(entry.getKey())).append('"').append(':');
            appendJsonValue(builder, entry.getValue());
        }
        builder.append('}');
        return builder.toString();
    }

    private void appendJsonValue(StringBuilder builder, Object value) {
        if (value == null) {
            builder.append("null");
        } else if (value instanceof Number || value instanceof Boolean) {
            builder.append(value);
        } else {
            builder.append('"').append(escape(String.valueOf(value))).append('"');
        }
    }

    private String escape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    private String defaultText(String value, String fallback) {
        return hasText(value) ? value : fallback;
    }

    private String firstText(String first, String second) {
        return hasText(first) ? first : second;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

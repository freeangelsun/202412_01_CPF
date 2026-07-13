package cpf.pfw.common.logging.file;

import cpf.pfw.common.logging.SensitiveDataMasker;
import cpf.pfw.common.logging.ServerInstanceIdentity;
import cpf.pfw.common.logging.TransactionContext;
import cpf.pfw.common.logging.TransactionLogRecord;
import cpf.pfw.common.logging.CpfTransactionContextAnomalyMonitor;
import cpf.pfw.common.logging.policy.LogPolicyDecision;
import cpf.pfw.common.logging.segment.TransactionSegmentContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.StandardCopyOption;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * CPF 구조화 파일 로그를 공통 규격으로 기록합니다.
 *
 * <p>DB 로그는 운영 조회와 통계의 기준이고, 파일 로그는 장애 상황에서 인스턴스별로 빠르게 검색할 수 있는
 * 보조 증적입니다. 모든 실행 모듈은
 * {@code ${CPF_LOG_ROOT}/{moduleCode}/cpf-{moduleCode}-{logType}-{instanceId}.{yyyy-MM-dd}.log}
 * 규칙을 공유합니다.</p>
 */
@Component
public class CpfFileLogWriter {
    private static final Logger log = LoggerFactory.getLogger(CpfFileLogWriter.class);
    private static final String DEFAULT_PATTERN = "cpf-{moduleCode}-{logType}-{instanceId}.{date}.log";
    private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final Environment environment;
    private final Clock clock;
    private final ZoneId logZoneId;
    private final Map<Path, Object> fileLocks = new ConcurrentHashMap<>();
    private final Set<String> retentionChecked = ConcurrentHashMap.newKeySet();

    @Autowired
    public CpfFileLogWriter(Environment environment) {
        this(environment, Clock.system(resolveZoneId(environment)));
    }

    /**
     * 일자 전환 테스트와 재현 가능한 로그 생성을 위해 Clock을 주입합니다.
     */
    public CpfFileLogWriter(Environment environment, Clock clock) {
        this.environment = environment;
        this.logZoneId = resolveZoneId(environment);
        this.clock = clock.withZone(logZoneId);
        initializeLogRoot();
    }

    /**
     * 온라인 거래 AOP가 수집한 요약 정보를 transaction/error 파일 로그로 남깁니다.
     */
    public void writeTransaction(TransactionLogRecord record, Map<String, String> details, LogPolicyDecision policy) {
        if (record == null || !enabled("transaction")) {
            return;
        }
        if (!hasText(record.getTransactionId())) {
            long missingCount = CpfTransactionContextAnomalyMonitor.recordMissing("CpfFileLogWriter.writeTransaction");
            Map<String, Object> anomaly = baseEvent(record.getModuleId(), "error", policy, details);
            anomaly.put("eventType", "CONTEXT_MISSING");
            anomaly.put("status", "ERROR");
            anomaly.put("boundary", "ONLINE_TRANSACTION");
            anomaly.put("missingContextCount", missingCount);
            append(record.getModuleId(), "error", anomaly);
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

    /**
     * BAT JobInstance처럼 일반 파일명과 다른 표준 경로가 필요한 로그를 안전하게 기록합니다.
     * 상대 경로만 허용하며 로그 root 이탈은 차단합니다.
     */
    public void writeEventAtRelativePath(Path relativePath, Map<String, Object> event) {
        if (relativePath == null || event == null || !enabled("file")) {
            return;
        }
        Path root = logRoot();
        Path normalizedRelative = relativePath.normalize();
        if (normalizedRelative.isAbsolute() || normalizedRelative.startsWith("..")) {
            throw new IllegalArgumentException("로그 상대 경로가 CPF_LOG_ROOT를 벗어날 수 없습니다.");
        }
        Path target = root.resolve(normalizedRelative).normalize();
        if (!target.startsWith(root)) {
            throw new IllegalArgumentException("로그 경로가 CPF_LOG_ROOT를 벗어났습니다.");
        }
        appendToPath(target, sanitizeMap(event));
    }

    public Path logRoot() {
        return Path.of(environment.getProperty("cpf.logging.file.base-path", "./logs"))
                .toAbsolutePath()
                .normalize();
    }

    public ZoneId logZoneId() {
        return logZoneId;
    }

    public LocalDate currentLogDate() {
        return LocalDate.now(clock);
    }

    private void initializeLogRoot() {
        if (!enabled("file")) {
            return;
        }
        try {
            Path moduleRoot = logRoot().resolve(moduleCode().toLowerCase(Locale.ROOT));
            Files.createDirectories(moduleRoot);
            if (!Files.isDirectory(moduleRoot) || !Files.isWritable(moduleRoot)) {
                throw new IOException("로그 디렉터리에 쓸 수 없습니다: " + moduleRoot);
            }
        } catch (Exception ex) {
            boolean failFast = environment.getProperty(
                    "cpf.logging.file.initialization-fail-fast",
                    Boolean.class,
                    false);
            if (failFast) {
                throw new IllegalStateException("CPF 로그 root 초기화에 실패했습니다.", ex);
            }
            log.warn("CPF log root initialization failed. error={}", ex.getMessage());
        }
    }

    private Map<String, Object> baseEvent(
            String moduleCode,
            String logType,
            LogPolicyDecision policy,
            Map<String, String> details) {

        ServerInstanceIdentity.Identity identity = ServerInstanceIdentity.current();
        Map<String, Object> event = new LinkedHashMap<>();
        OffsetDateTime now = OffsetDateTime.now(clock);
        event.put("timestamp", now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        event.put("timezone", logZoneId.getId());
        event.put("businessDate", now.toLocalDate().format(DateTimeFormatter.BASIC_ISO_DATE));
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
        appendToPath(resolveLogPath(moduleCode, logType), event);
    }

    private void appendToPath(Path logPath, Map<String, Object> event) {
        try {
            Files.createDirectories(logPath.getParent());
            Object lock = fileLocks.computeIfAbsent(logPath.getParent(), ignored -> new Object());
            synchronized (lock) {
                restoreCompressedLog(logPath);
                Files.writeString(
                        logPath,
                        toJson(event) + System.lineSeparator(),
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND);
                applyRetentionOnce(logPath.getParent(), logPath);
            }
        } catch (IOException ex) {
            // 파일 로그 실패가 업무 응답 실패로 전파되지 않도록 표준 로그만 남깁니다.
            log.warn("CPF file log write failed. path={}, error={}", logPath, ex.getMessage());
        }
    }

    private Path resolveLogPath(String moduleCode, String logType) {
        String normalizedModule = normalizeModuleCode(moduleCode).toLowerCase(Locale.ROOT);
        String normalizedType = normalizeLogType(logType);
        String instanceId = sanitizePathToken(ServerInstanceIdentity.current().serverInstanceId(), "local-01");
        String date = currentLogDate().format(FILE_DATE_FORMATTER);
        String pattern = environment.getProperty("cpf.logging.file.file-pattern", DEFAULT_PATTERN)
                .replace("{moduleCode}", normalizedModule)
                .replace("{logType}", normalizedType)
                .replace("{instanceId}", instanceId)
                .replace("{date}", date);
        Path path = logRoot().resolve(normalizedModule).resolve(pattern).normalize();
        if (!path.startsWith(logRoot())) {
            throw new IllegalArgumentException("로그 파일 패턴이 CPF_LOG_ROOT를 벗어났습니다.");
        }
        return path;
    }

    private void applyRetentionOnce(Path directory, Path activeLogPath) throws IOException {
        int maxHistoryDays = Math.max(1,
                environment.getProperty("cpf.logging.file.max-history-days", Integer.class, 30));
        String key = directory.toAbsolutePath().normalize() + "|" + currentLogDate();
        if (!retentionChecked.add(key) || !Files.isDirectory(directory)) {
            return;
        }
        Instant cutoff = currentLogDate()
                .minusDays(maxHistoryDays)
                .atStartOfDay(logZoneId)
                .toInstant();
        List<Path> candidates;
        try (var files = Files.list(directory)) {
            candidates = files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().matches(".*\\.log(?:\\.gz)?$"))
                    .toList();
        }
        for (Path candidate : candidates) {
            if (Files.getLastModifiedTime(candidate).toInstant().isBefore(cutoff)) {
                Files.deleteIfExists(candidate);
                continue;
            }
            if (archiveCompressionEnabled(directory)
                    && candidate.getFileName().toString().endsWith(".log")
                    && !candidate.equals(activeLogPath)) {
                compressLog(candidate);
            }
        }
    }

    private boolean archiveCompressionEnabled(Path directory) {
        boolean configured = environment.getProperty(
                "cpf.logging.file.archive-compress-enabled",
                Boolean.class,
                true);
        return configured && directory.getParent() != null && directory.getParent().equals(logRoot());
    }

    private void restoreCompressedLog(Path logPath) throws IOException {
        Path gzipPath = logPath.resolveSibling(logPath.getFileName() + ".gz");
        if (!Files.isRegularFile(gzipPath)) {
            return;
        }
        Path restorePath = logPath.resolveSibling(logPath.getFileName() + ".restore.tmp");
        try {
            try (InputStream input = new GZIPInputStream(Files.newInputStream(gzipPath));
                 OutputStream output = Files.newOutputStream(
                         restorePath,
                         StandardOpenOption.CREATE,
                         StandardOpenOption.TRUNCATE_EXISTING,
                         StandardOpenOption.WRITE)) {
                input.transferTo(output);
                if (Files.isRegularFile(logPath)) {
                    Files.copy(logPath, output);
                }
            }
            Files.move(restorePath, logPath, StandardCopyOption.REPLACE_EXISTING);
            Files.deleteIfExists(gzipPath);
        } finally {
            Files.deleteIfExists(restorePath);
        }
    }

    private void compressLog(Path logPath) throws IOException {
        Path gzipPath = logPath.resolveSibling(logPath.getFileName() + ".gz");
        Path temporaryPath = gzipPath.resolveSibling(gzipPath.getFileName() + ".tmp");
        try {
            try (InputStream input = Files.newInputStream(logPath);
                 OutputStream output = new GZIPOutputStream(Files.newOutputStream(
                         temporaryPath,
                         StandardOpenOption.CREATE,
                         StandardOpenOption.TRUNCATE_EXISTING,
                         StandardOpenOption.WRITE))) {
                input.transferTo(output);
            }
            Files.move(temporaryPath, gzipPath, StandardCopyOption.REPLACE_EXISTING);
            Files.deleteIfExists(logPath);
        } finally {
            Files.deleteIfExists(temporaryPath);
        }
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

    private String sanitizePathToken(String value, String fallback) {
        String resolved = hasText(value) ? value.trim() : fallback;
        String sanitized = resolved.replaceAll("[^A-Za-z0-9._-]", "_");
        return sanitized.isBlank() ? fallback : sanitized;
    }

    private static ZoneId resolveZoneId(Environment environment) {
        String configured = environment.getProperty("cpf.logging.file.timezone", "Asia/Seoul");
        try {
            return ZoneId.of(configured);
        } catch (Exception ex) {
            throw new IllegalArgumentException("cpf.logging.file.timezone이 올바른 ZoneId가 아닙니다: " + configured, ex);
        }
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

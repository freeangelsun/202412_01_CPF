package cpf.pfw.common.logging.file;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * CPF 구조화 파일 로그를 공통 규격으로 기록합니다.
 *
 * <p>DB 로그는 운영 조회와 통계의 기준이고, 파일 로그는 장애 상황에서 인스턴스별로 빠르게 검색할 수 있는
 * 보조 증적입니다. 일반 로그는 환경·실행 모듈·인스턴스 경로로 분리하고, 온라인 거래 로그는
 * {@code transactions/{businessDate}/{transactionId}_{businessDate}.log} 규칙을 사용합니다.</p>
 */
@Component
public class CpfFileLogWriter {
    private static final Logger log = LoggerFactory.getLogger(CpfFileLogWriter.class);
    private final Environment environment;
    private final Clock clock;
    private final ZoneId logZoneId;
    private final CpfLogPathPolicy pathPolicy;
    private final ObjectMapper objectMapper;
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
        this.pathPolicy = new CpfLogPathPolicy(environment);
        this.objectMapper = new ObjectMapper();
        initializeLogRoot();
    }

    /**
     * 온라인 거래 AOP가 수집한 요약 정보를 transaction/error 파일 로그로 남깁니다.
     */
    public void writeTransaction(TransactionLogRecord record, Map<String, String> details, LogPolicyDecision policy) {
        if (record == null || !enabled("transaction")) {
            return;
        }
        if (!hasText(record.getTransactionId()) || !hasText(record.getBusinessTransactionId())) {
            long missingCount = CpfTransactionContextAnomalyMonitor.recordMissing("CpfFileLogWriter.writeTransaction");
            Map<String, Object> anomaly = baseEvent(record.getModuleId(), "error", policy, details);
            anomaly.put("eventType", "CONTEXT_MISSING");
            anomaly.put("status", "ERROR");
            anomaly.put("boundary", "ONLINE_TRANSACTION");
            anomaly.put("missingTransactionGlobalId", !hasText(record.getTransactionId()));
            anomaly.put("missingTransactionId", !hasText(record.getBusinessTransactionId()));
            anomaly.put("missingContextCount", missingCount);
            append(record.getModuleId(), "error", anomaly);
            return;
        }

        Map<String, Object> event = baseEvent(record.getModuleId(), "transaction", policy, details);
        event.put("eventType", "ONLINE_TRANSACTION");
        event.put("transactionId", record.getBusinessTransactionId());
        event.put("transactionGlobalId", record.getTransactionId());
        event.put("segmentId", firstText(detail(details, "transactionSegment.id"), record.getSpanId()));
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
        LocalDate transactionBusinessDate = record.getStartTime() != null
                ? record.getStartTime().toLocalDate()
                : defaultBusinessDate();
        appendTransaction(record.getBusinessTransactionId(), transactionBusinessDate, event);
        if ("FAILURE".equalsIgnoreCase(record.getLogType()) && enabled("error")) {
            Map<String, Object> errorEvent = new LinkedHashMap<>(event);
            errorEvent.put("logType", "error");
            append(record.getModuleId(), "error", errorEvent);
        }
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
        String transactionGlobalId = TransactionContext.currentTransactionId();
        String transactionId = firstText(
                attributeText(attributes, "transactionId", null),
                attributeText(attributes, "businessTransactionId", null),
                TransactionContext.currentBusinessTransactionId());
        event.put("transactionId", transactionId);
        event.put("transactionGlobalId", transactionGlobalId);
        event.put("segmentId", firstText(TransactionSegmentContext.currentSegmentId(), TransactionContext.currentSpanId()));
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
        if (hasText(transactionId) && hasText(transactionGlobalId)) {
            appendTransaction(transactionId, defaultBusinessDate(), event);
        } else {
            CpfTransactionContextAnomalyMonitor.recordMissing("CpfFileLogWriter.writeIntegration");
        }
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
     * BAT JobInstance처럼 환경 공용 논리 경로가 필요한 로그를 안전하게 기록합니다.
     * 전달 경로는 환경 root 아래의 상대경로로만 해석합니다.
     */
    public void writeEventAtRelativePath(Path relativePath, Map<String, Object> event) {
        if (relativePath == null || event == null || !enabled("file")) {
            return;
        }
        appendToPath(pathPolicy.batchJobLogPath(relativePath), sanitizeMap(event));
    }

    public Path logRoot() {
        return pathPolicy.logRoot();
    }

    public Path instanceRoot() {
        return pathPolicy.instanceRoot();
    }

    public String environmentCode() {
        return pathPolicy.environmentCode();
    }

    public String runtimeModuleCode() {
        return pathPolicy.runtimeModuleCode();
    }

    public String instanceId() {
        return pathPolicy.instanceId();
    }

    public Path recoveryPath(Path relativePath) {
        return pathPolicy.recoveryPath(relativePath);
    }

    public Path batchJobLogPath(Path relativePath) {
        return pathPolicy.batchJobLogPath(relativePath);
    }

    public Path relativeToLogRoot(Path path) {
        return pathPolicy.relativeToLogRoot(path);
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
            Path root = logRoot();
            Files.createDirectories(root);
            Path probe = Files.createTempFile(root, ".cpf-log-write-probe-", ".tmp");
            Files.deleteIfExists(probe);
            if (!Files.isDirectory(root) || !Files.isWritable(root)) {
                throw new IOException("로그 디렉터리에 쓸 수 없습니다: " + root);
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
        event.put("environment", pathPolicy.environmentCode());
        event.put("runtimeModuleCode", pathPolicy.runtimeModuleCode());
        event.put("level", policy != null ? defaultText(policy.fileLogLevel(), "INFO") : defaultText(TransactionContext.currentDynamicLogLevel(), "INFO"));
        event.put("logType", logType);
        event.put("moduleCode", normalizeModuleCode(moduleCode));
        event.put("sourceModuleCode", normalizeModuleCode(moduleCode));
        event.put("targetModuleCode", null);
        event.put("traceBoostPolicyId", traceBoostPolicyId(policy, details));
        event.put("logLevelApplied", policy != null ? policy.fileLogLevel() : TransactionContext.currentDynamicLogLevel());
        event.put("serverId", environment.getProperty("cpf.framework.was-id", pathPolicy.instanceId()));
        event.put("instanceId", pathPolicy.instanceId());
        event.put("serverInstanceId", pathPolicy.instanceId());
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

    private void appendTransaction(String transactionId, LocalDate businessDate, Map<String, Object> event) {
        if (!enabled("file") || !enabled("transaction")) {
            return;
        }
        appendToPath(pathPolicy.transactionLogPath(transactionId, businessDate), sanitizeMap(event));
    }

    private LocalDate defaultBusinessDate() {
        LocalDate contextDate = TransactionContext.currentBusinessDate();
        return contextDate != null ? contextDate : currentLogDate();
    }

    private void appendToPath(Path logPath, Map<String, Object> event) {
        try {
            Files.createDirectories(logPath.getParent());
            Object lock = fileLocks.computeIfAbsent(logPath, ignored -> new Object());
            synchronized (lock) {
                restoreCompressedLog(logPath);
                Files.writeString(
                        logPath,
                        toJson(event) + System.lineSeparator(),
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND);
                applyRetentionOnce(logPath);
            }
        } catch (IOException ex) {
            // 파일 로그 실패가 업무 응답 실패로 전파되지 않도록 표준 로그만 남깁니다.
            log.warn("CPF file log write failed. path={}, error={}", logPath, ex.getMessage());
        }
    }

    private Path resolveLogPath(String moduleCode, String logType) {
        return pathPolicy.generalLogPath(moduleCode, normalizeLogType(logType), currentLogDate());
    }

    private void applyRetentionOnce(Path activeLogPath) throws IOException {
        int maxHistoryDays = Math.max(1,
                environment.getProperty("cpf.logging.file.max-history-days", Integer.class, 30));
        String key = pathPolicy.instanceRoot() + "|" + currentLogDate();
        if (!retentionChecked.add(key) || !Files.isDirectory(logRoot())) {
            return;
        }
        Instant cutoff = currentLogDate()
                .minusDays(maxHistoryDays)
                .atStartOfDay(logZoneId)
                .toInstant();
        List<Path> candidates;
        try (var files = Files.walk(logRoot())) {
            candidates = files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().matches(".*\\.log(?:\\.gz)?$"))
                    .toList();
        }
        for (Path candidate : candidates) {
            if (Files.getLastModifiedTime(candidate).toInstant().isBefore(cutoff)) {
                Files.deleteIfExists(candidate);
                continue;
            }
            if (archiveCompressionEnabled()
                    && candidate.getFileName().toString().endsWith(".log")
                    && !candidate.equals(activeLogPath)
                    && Files.getLastModifiedTime(candidate).toInstant().isBefore(
                    currentLogDate().atStartOfDay(logZoneId).toInstant())) {
                compressLog(candidate);
            }
        }
        applyTotalSizeCap(activeLogPath);
    }

    private boolean archiveCompressionEnabled() {
        return environment.getProperty(
                "cpf.logging.file.archive-compress-enabled",
                Boolean.class,
                true);
    }

    private void applyTotalSizeCap(Path activeLogPath) throws IOException {
        long capBytes = parseSize(environment.getProperty("cpf.logging.file.total-size-cap", "2GB"));
        if (capBytes < 1) {
            return;
        }
        List<Path> files;
        try (var stream = Files.walk(logRoot())) {
            files = stream.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().matches(".*\\.log(?:\\.gz)?$"))
                    .sorted(Comparator.comparing(this::lastModified))
                    .toList();
        }
        long total = 0L;
        for (Path file : files) {
            total += Files.size(file);
        }
        for (Path file : files) {
            if (total <= capBytes) {
                break;
            }
            if (file.equals(activeLogPath)) {
                continue;
            }
            long size = Files.size(file);
            if (Files.deleteIfExists(file)) {
                total -= size;
            }
        }
    }

    private Instant lastModified(Path path) {
        try {
            return Files.getLastModifiedTime(path).toInstant();
        } catch (IOException ex) {
            return Instant.MAX;
        }
    }

    private long parseSize(String value) {
        if (!hasText(value)) {
            return 0L;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        long multiplier = 1L;
        if (normalized.endsWith("KB")) {
            multiplier = 1024L;
            normalized = normalized.substring(0, normalized.length() - 2);
        } else if (normalized.endsWith("MB")) {
            multiplier = 1024L * 1024L;
            normalized = normalized.substring(0, normalized.length() - 2);
        } else if (normalized.endsWith("GB")) {
            multiplier = 1024L * 1024L * 1024L;
            normalized = normalized.substring(0, normalized.length() - 2);
        } else if (normalized.endsWith("B")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        try {
            return Math.multiplyExact(Long.parseLong(normalized.trim()), multiplier);
        } catch (ArithmeticException | NumberFormatException ex) {
            throw new IllegalArgumentException("cpf.logging.file.total-size-cap 형식이 올바르지 않습니다: " + value, ex);
        }
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
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("CPF 구조화 로그 JSON 직렬화에 실패했습니다.", ex);
        }
    }

    private String defaultText(String value, String fallback) {
        return hasText(value) ? value : fallback;
    }

    private String firstText(String first, String second) {
        return hasText(first) ? first : second;
    }

    private String firstText(String first, String second, String third) {
        return hasText(first) ? first : (hasText(second) ? second : third);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

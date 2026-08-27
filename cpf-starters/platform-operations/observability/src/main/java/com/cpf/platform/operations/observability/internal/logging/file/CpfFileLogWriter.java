package com.cpf.platform.operations.observability.internal.logging.file;

import com.cpf.platform.operations.observability.api.logging.CpfIntegrationLogPort;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cpf.security.api.CpfMaskingRuntime;
import com.cpf.platform.operations.observability.internal.logging.TransactionContext;
import com.cpf.platform.operations.observability.spi.logging.TransactionLogRecord;
import com.cpf.platform.operations.observability.internal.logging.CpfTransactionContextAnomalyMonitor;
import com.cpf.platform.operations.observability.api.logging.CpfFileLogRuntimeStatus;
import com.cpf.platform.operations.observability.api.logging.policy.LogPolicyDecision;
import com.cpf.platform.operations.observability.internal.logging.segment.TransactionSegmentContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * CPF 구조화 파일 로그를 공통 규격으로 기록합니다.
 *
 * <p>DB 로그는 운영 조회와 통계의 기준이고, 파일 로그는 장애 상황에서 인스턴스별로 빠르게 검색할 수 있는
 * 보조 증적입니다. 일반 로그는 환경·실행 모듈·인스턴스 경로로 분리하고, 온라인 거래 로그는
 * {@code transactions/{businessDate}/{transactionId}_{businessDate}.log} 규칙을 사용합니다. 하나의 글로벌 거래에 속한 local/remote/integration/retry segment가 같은 파일 추적 키를 사용하므로 DB timeline과 파일 증적을 동일 키로 교차 조회할 수 있습니다.</p>
 */
@Component
public final class CpfFileLogWriter implements CpfFileLogRuntimeStatus, CpfIntegrationLogPort, AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(CpfFileLogWriter.class);
    private static final Pattern ISO_LOG_DATE_PATTERN = Pattern.compile("(?<!\\d)(\\d{4}-\\d{2}-\\d{2})(?!\\d)");
    private static final Pattern BASIC_LOG_DATE_PATTERN = Pattern.compile("(?<!\\d)(\\d{8})(?!\\d)");
    private final Environment environment;
    private final Clock clock;
    private final ZoneId logZoneId;
    private final CpfLogPathPolicy pathPolicy;
    private final ObjectMapper objectMapper;
    private final CpfFileLogRecoverySpool recoverySpool;
    private final Map<Path, FileLockEntry> fileLocks = new ConcurrentHashMap<>();
    private final AtomicLong nextRetentionCheckEpochMillis = new AtomicLong(Long.MIN_VALUE);
    private final AtomicLong retentionRunCount = new AtomicLong();
    private final AtomicLong retentionSkipCount = new AtomicLong();
    private final AtomicLong compressedFileCount = new AtomicLong();
    private final AtomicLong deletedFileCount = new AtomicLong();
    private final AtomicLong retentionFailureCount = new AtomicLong();
    private final AtomicLong processLockTimeoutCount = new AtomicLong();
    private final AtomicLong writeFailureCount = new AtomicLong();
    private final AtomicReference<Instant> lastRetentionStartedAt = new AtomicReference<>();
    private final AtomicReference<Instant> lastRetentionCompletedAt = new AtomicReference<>();
    private final AtomicReference<String> lastRetentionFailureType = new AtomicReference<>();
    private final AtomicReference<String> lastWriteFailureType = new AtomicReference<>();

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
        this.recoverySpool = new CpfFileLogRecoverySpool(environment, this.clock, this::appendRecoveredRecord);
        this.recoverySpool.replayAvailable();
    }

    /**
     * 온라인 거래 AOP가 수집한 요약 정보를 transaction/error 파일 로그로 남깁니다.
     */
    public void writeTransaction(TransactionLogRecord record, Map<String, String> details, LogPolicyDecision policy) {
        writePreparedTransaction(prepareTransaction(record, details, policy));
    }

    PreparedTransactionLog prepareTransaction(
            TransactionLogRecord record,
            Map<String, String> details,
            LogPolicyDecision policy) {
        if (record == null || !enabled("transaction")) {
            return PreparedTransactionLog.empty();
        }
        List<PreparedFileLogEvent> prepared = new ArrayList<>(2);
        if (!hasText(record.getTransactionId()) || !hasText(record.getStandardExecutionId())) {
            long missingCount = CpfTransactionContextAnomalyMonitor.recordMissing("CpfFileLogWriter.writeTransaction");
            Map<String, Object> anomaly = baseEvent(record.getModuleId(), "error", policy, details);
            anomaly.put("eventType", "CONTEXT_MISSING");
            anomaly.put("status", "ERROR");
            anomaly.put("boundary", "ONLINE_TRANSACTION");
            anomaly.put("missingTransactionId", !hasText(record.getTransactionId()));
            anomaly.put("missingStandardExecutionId", !hasText(record.getStandardExecutionId()));
            anomaly.put("missingContextCount", missingCount);
            prepared.add(PreparedFileLogEvent.module(record.getModuleId(), "error", immutableEvent(anomaly)));
            return new PreparedTransactionLog(List.copyOf(prepared));
        }

        Map<String, Object> event = baseEvent(record.getModuleId(), "transaction", policy, details);
        event.put("eventType", "ONLINE_TRANSACTION");
        event.put("transactionId", record.getTransactionId());
        event.put("standardExecutionId", record.getStandardExecutionId());
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
        event.put("traceId", record.getTraceId());
        event.put("spanId", record.getSpanId());
        event.put("requestHeadersMasked", detail(details, "resolvedHeaders"));
        event.put("responseHeadersMasked", detail(details, "responseHeaders"));
        LocalDate transactionBusinessDate = record.getStartTime() != null
                ? record.getStartTime().toLocalDate()
                : defaultBusinessDate();
        prepared.add(PreparedFileLogEvent.transaction(
                record.getTransactionId(), transactionBusinessDate, immutableEvent(event)));
        if ("FAILURE".equalsIgnoreCase(record.getLogType()) && enabled("error")) {
            Map<String, Object> errorEvent = new LinkedHashMap<>(event);
            errorEvent.put("logType", "error");
            prepared.add(PreparedFileLogEvent.module(
                    record.getModuleId(), "error", immutableEvent(errorEvent)));
        }
        return new PreparedTransactionLog(List.copyOf(prepared));
    }

    void writePreparedTransaction(PreparedTransactionLog prepared) {
        writePreparedTransactionWithOutcome(prepared);
    }

    boolean writePreparedTransactionWithOutcome(PreparedTransactionLog prepared) {
        if (prepared == null) return true;
        boolean successful = true;
        for (PreparedFileLogEvent item : prepared.events()) {
            boolean itemSuccessful = item.target() == PreparedTarget.TRANSACTION
                    ? appendTransactionWithOutcome(item.transactionId(), item.businessDate(), item.event())
                    : appendWithOutcome(item.moduleCode(), item.logType(), item.event());
            successful &= itemSuccessful;
        }
        return successful;
    }

    private static Map<String, Object> immutableEvent(Map<String, Object> source) {
        return java.util.Collections.unmodifiableMap(new LinkedHashMap<>(source));
    }

    record PreparedTransactionLog(List<PreparedFileLogEvent> events) {
        PreparedTransactionLog {
            events = events == null ? List.of() : List.copyOf(events);
        }

        static PreparedTransactionLog empty() {
            return new PreparedTransactionLog(List.of());
        }

        boolean emptyValue() {
            return events.isEmpty();
        }
    }

    record PreparedFileLogEvent(
            PreparedTarget target,
            String moduleCode,
            String logType,
            String transactionId,
            LocalDate businessDate,
            Map<String, Object> event) {
        PreparedFileLogEvent {
            target = java.util.Objects.requireNonNull(target, "target");
            event = immutableEvent(java.util.Objects.requireNonNull(event, "event"));
        }

        static PreparedFileLogEvent module(String moduleCode, String logType, Map<String, Object> event) {
            return new PreparedFileLogEvent(PreparedTarget.MODULE, moduleCode, logType, null, null, event);
        }

        static PreparedFileLogEvent transaction(
                String transactionId,
                LocalDate businessDate,
                Map<String, Object> event) {
            return new PreparedFileLogEvent(
                    PreparedTarget.TRANSACTION, null, null, transactionId, businessDate, event);
        }
    }

    enum PreparedTarget {
        MODULE,
        TRANSACTION
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
        String transactionId = TransactionContext.currentTransactionId();
        String standardExecutionId = firstText(
                attributeText(attributes, "standardExecutionId", null),
                attributeText(attributes, "businessTransactionId", null),
                TransactionContext.currentStandardExecutionId());
        event.put("transactionId", transactionId);
        event.put("standardExecutionId", standardExecutionId);
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
        if (hasText(transactionId)) {
            appendTransaction(transactionId, defaultBusinessDate(), event);
        } else {
            CpfTransactionContextAnomalyMonitor.recordMissing("CpfFileLogWriter.writeIntegration");
        }
    }

    public Map<String, Object> newBaseEvent(String moduleCode, String logType) {
        return baseEvent(moduleCode, logType, null, null);
    }

    public void writeEvent(String moduleCode, String logType, Map<String, Object> event) {
        writeEventWithOutcome(moduleCode, logType, event);
    }

    public boolean writeEventWithOutcome(String moduleCode, String logType, Map<String, Object> event) {
        if (event == null) return true;
        return appendWithOutcome(moduleCode, logType, sanitizeMap(event));
    }

    /**
     * BAT JobInstance처럼 환경 공용 논리 경로가 필요한 로그를 안전하게 기록합니다.
     * 전달 경로는 환경 root 아래의 상대경로로만 해석합니다.
     */
    public void writeEventAtRelativePath(Path relativePath, Map<String, Object> event) {
        writeEventAtRelativePathWithOutcome(relativePath, event);
    }

    public boolean writeEventAtRelativePathWithOutcome(Path relativePath, Map<String, Object> event) {
        if (relativePath == null || event == null || !enabled("file")) return true;
        return appendToPath(pathPolicy.batchJobLogPath(relativePath), sanitizeMap(event));
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

    /** Visible for runtime diagnostics and leak regression tests. */
    int retainedRetentionScheduleCount() {
        return nextRetentionCheckEpochMillis.get() == Long.MIN_VALUE ? 0 : 1;
    }

    public LocalDate currentLogDate() {
        return LocalDate.now(clock);
    }

    @Override
    public FileLogRuntimeSnapshot fileLogRuntimeSnapshot() {
        long runs = retentionRunCount.get();
        long failures = retentionFailureCount.get();
        long lockTimeouts = processLockTimeoutCount.get();
        long writeFailures = writeFailureCount.get();
        RetentionState state;
        if (runs == 0L) {
            state = failures > 0L || lockTimeouts > 0L || writeFailures > 0L
                    ? RetentionState.DOWN : RetentionState.NEVER_RUN;
        } else if (failures > 0L || lockTimeouts > 0L || writeFailures > 0L) {
            state = RetentionState.DEGRADED;
        } else {
            state = RetentionState.HEALTHY;
        }
        return new FileLogRuntimeSnapshot(
                runs,
                retentionSkipCount.get(),
                compressedFileCount.get(),
                deletedFileCount.get(),
                failures,
                lockTimeouts,
                lastRetentionStartedAt.get(),
                lastRetentionCompletedAt.get(),
                lastRetentionFailureType.get(),
                state);
    }

    @Override
    public FileWriteDiagnostics fileWriteDiagnostics() {
        return new FileWriteDiagnostics(
                writeFailureCount.get(), lastWriteFailureType.get(), clock.instant());
    }


    @Override
    public FileRecoveryDiagnostics fileRecoveryDiagnostics() {
        CpfFileLogRecoverySpool.Diagnostics d = recoverySpool.diagnostics();
        return new FileRecoveryDiagnostics(d.pending(), d.enqueued(), d.replayed(), d.deduplicated(),
                d.quarantined(), d.terminalLoss(), d.capturedAt());
    }

    /**
     * Best-effort shutdown drain. Pending records remain durably spooled if the original
     * destination is still unavailable, so process termination never converts a retryable
     * write failure into a false success.
     */
    @Override
    public void close() {
        recoverySpool.close();
    }

    private void initializeLogRoot() {
        if (!enabled("file")) {
            return;
        }
        try {
            Path root = logRoot();
            createDirectoriesWithSecurePermissions(root);
            Path probe = Files.createTempFile(root, ".cpf-log-write-probe-", ".tmp");
            applyFilePermissions(probe);
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
            log.warn("CPF log root initialization failed. error={}", CpfMaskingRuntime.mask(ex.getMessage(), 512));
        }
    }

    private Map<String, Object> baseEvent(
            String moduleCode,
            String logType,
            LogPolicyDecision policy,
            Map<String, String> details) {

        com.cpf.foundation.runtime.CpfInstanceIdentity.Identity identity = com.cpf.foundation.runtime.CpfInstanceIdentity.current();
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
        // 거래 추적 lineage는 runtime metadata가 아니라 현재 Canonical Context를 우선 정본으로 사용합니다.
        event.put("transactionId", TransactionContext.currentTransactionId());
        event.put("traceId", TransactionContext.currentTraceId());
        event.put("correlationId", TransactionContext.correlationId());
        event.put("executionId", com.cpf.core.api.context.CpfContexts.currentExecutionId());
        event.put("segmentId", firstText(TransactionSegmentContext.currentSegmentId(), TransactionContext.currentSpanId()));
        event.put("originalSystemCode", TransactionContext.originalSystemCode());
        event.put("systemCode", firstText(TransactionContext.currentSystemCode(), detail(details, "runtime.systemCode")));
        event.put("callerSystemCode", TransactionContext.callerSystemCode());
        event.put("targetSystemCode", TransactionContext.targetSystemCode());
        event.put("operationId", TransactionContext.observedOperationId());
        event.put("tenantId", TransactionContext.tenantId());
        // Runtime/System/Capability metadata is produced automatically by the CPF runtime usage/context bridge.
        event.put("domainCode", detail(details, "runtime.domainCode"));
        event.put("application", detail(details, "runtime.application"));
        event.put("module", detail(details, "runtime.module"));
        event.put("starterIds", detail(details, "capability.starters"));
        event.put("capabilityIds", detail(details, "capability.ids"));
        event.put("providers", detail(details, "capability.providers"));
        event.put("operations", detail(details, "capability.operations"));
        return event;
    }

    private void append(String moduleCode, String logType, Map<String, Object> event) {
        appendWithOutcome(moduleCode, logType, event);
    }

    private boolean appendWithOutcome(String moduleCode, String logType, Map<String, Object> event) {
        if (!enabled("file") || !enabled(logType)) return true;
        return appendToPath(resolveLogPath(moduleCode, logType), event);
    }

    private void appendTransaction(String transactionId, LocalDate businessDate, Map<String, Object> event) {
        appendTransactionWithOutcome(transactionId, businessDate, event);
    }

    private boolean appendTransactionWithOutcome(
            String transactionId, LocalDate businessDate, Map<String, Object> event) {
        if (!enabled("file") || !enabled("transaction")) return true;
        return appendToPath(pathPolicy.transactionLogPath(transactionId, businessDate), sanitizeMap(event));
    }

    private LocalDate defaultBusinessDate() {
        LocalDate contextDate = TransactionContext.currentBusinessDate();
        return contextDate != null ? contextDate : currentLogDate();
    }

    private boolean appendToPath(Path logPath, Map<String, Object> event) {
        recoverySpool.replayAvailable();
        try {
            createDirectoriesWithSecurePermissions(logPath.getParent());
            ensureSafeWritableLogPath(logPath);
            FileLockEntry lock = acquireFileLock(logPath);
            ProcessFileLock processGuard = acquireProcessFileLock(logPath);
            try {
                processGuard.ensureValid();
                ensureSafeWritableLogPath(logPath);
                restoreCompressedLog(logPath);
                Files.writeString(
                        logPath,
                        toJson(event) + System.lineSeparator(),
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND,
                        LinkOption.NOFOLLOW_LINKS);
                applyFilePermissions(logPath);
            } finally {
                try {
                    processGuard.close();
                } finally {
                    releaseFileLock(logPath, lock);
                }
            }
        } catch (IOException | RuntimeException ex) {
            writeFailureCount.incrementAndGet();
            lastWriteFailureType.set(ex.getClass().getSimpleName());
            // 파일 로그 실패가 업무 응답 실패로 전파되지 않도록 표준 로그만 남깁니다.
            boolean spooled = recoverySpool.enqueue(logPath, toJson(event));
            log.warn("CPF file log write failed. recoverySpooled={}, path={}, error={}",
                    spooled,
                    CpfMaskingRuntime.mask(String.valueOf(logPath), 512),
                    CpfMaskingRuntime.mask(ex.getMessage(), 512));
            if (!spooled && "audit".equalsIgnoreCase(String.valueOf(event.get("logType")))
                    && environment.getProperty("cpf.logging.file.audit-fail-closed", Boolean.class, false)) {
                throw new IllegalStateException("CPF audit log durable recovery failed", ex);
            }
            return false;
        }
        // Retention obtains candidate-specific locks after the active append lock is released.
        // Retention failure is independently visible and never misreported as an append failure.
        try {
            applyRetentionOnce(logPath);
        } catch (IOException | RuntimeException retentionFailure) {
            retentionFailureCount.incrementAndGet();
            lastRetentionFailureType.set(retentionFailure.getClass().getSimpleName());
            log.warn("CPF file log retention failed. path={}, error={}",
                    CpfMaskingRuntime.mask(String.valueOf(logPath), 512),
                    CpfMaskingRuntime.mask(retentionFailure.getMessage(), 512));
        }
        return true;
    }

    /** Recovery replay reuses all normal writer path/lock/symlink defenses. */
    private boolean appendRecoveredRecord(Path logPath, String recoveredRecord, String checksum) throws Exception {
        createDirectoriesWithSecurePermissions(logPath.getParent());
        ensureSafeWritableLogPath(logPath);
        FileLockEntry lock = acquireFileLock(logPath);
        ProcessFileLock processGuard = acquireProcessFileLock(logPath);
        try {
            processGuard.ensureValid();
            ensureSafeWritableLogPath(logPath);
            restoreCompressedLog(logPath);
            String marker = "\"cpfRecoveryChecksum\":\"" + checksum + "\"";
            if (containsRecoveryMarker(logPath, marker)) {
                throw new CpfFileLogRecoverySpool.DuplicateRecoveryRecord();
            }
            Files.writeString(logPath, recoveredRecord + System.lineSeparator(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND, LinkOption.NOFOLLOW_LINKS);
            applyFilePermissions(logPath);
            return true;
        } finally {
            try { processGuard.close(); } finally { releaseFileLock(logPath, lock); }
        }
    }

    private boolean containsRecoveryMarker(Path logPath, String marker) throws IOException {
        if (!Files.exists(logPath, LinkOption.NOFOLLOW_LINKS)) return false;
        try (var reader = Files.newBufferedReader(logPath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(marker)) return true;
            }
        }
        return false;
    }

    private FileLockEntry acquireFileLock(Path path) {
        Path key = logicalLockKey(path);
        FileLockEntry entry = fileLocks.compute(key, (ignored, current) -> {
            FileLockEntry selected = current == null ? new FileLockEntry() : current;
            selected.references++;
            return selected;
        });
        entry.lock.lock();
        return entry;
    }

    private void releaseFileLock(Path path, FileLockEntry entry) {
        Path key = logicalLockKey(path);
        entry.lock.unlock();
        fileLocks.computeIfPresent(key, (ignored, current) -> {
            if (current != entry) return current;
            current.references--;
            return current.references == 0 && !current.lock.isLocked() && !current.lock.hasQueuedThreads()
                    ? null : current;
        });
    }

    private ProcessFileLock acquireProcessFileLock(Path protectedPath) throws IOException {
        Path normalized = protectedPath.toAbsolutePath().normalize();
        Path lockPath = normalized.resolveSibling("." + normalized.getFileName() + ".cpf-lock");
        return acquireNamedProcessLock(lockPath);
    }

    private ProcessFileLock acquireNamedProcessLock(Path lockPath) throws IOException {
        Path root = logRoot().toAbsolutePath().normalize();
        Path normalized = lockPath.toAbsolutePath().normalize();
        if (!normalized.startsWith(root)) {
            throw new IOException("process lock path escapes CPF_LOG_ROOT: " + normalized);
        }
        createDirectoriesWithSecurePermissions(normalized.getParent());
        if (Files.isSymbolicLink(normalized)) {
            throw new IOException("symbolic process lock files are forbidden: " + normalized);
        }
        FileChannel channel = FileChannel.open(
                normalized,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                LinkOption.NOFOLLOW_LINKS);
        try {
            applyFilePermissions(normalized);
            long timeoutMillis = bounded(
                    environment.getProperty("cpf.logging.file.process-lock-timeout-ms", Long.class, 5_000L),
                    1L, 60_000L, "cpf.logging.file.process-lock-timeout-ms");
            long timeoutNanos = TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
            long started = System.nanoTime();
            while (true) {
                if (Thread.currentThread().isInterrupted()) {
                    InterruptedIOException interrupted = new InterruptedIOException(
                            "interrupted while acquiring CPF file process lock");
                    Thread.currentThread().interrupt();
                    throw interrupted;
                }
                try {
                    FileLock lock = channel.tryLock();
                    if (lock != null) return new ProcessFileLock(channel, lock);
                } catch (OverlappingFileLockException busyInSameJvm) {
                    // Another writer instance in this JVM owns the same OS lock. Retry within the bounded budget.
                }
                if (System.nanoTime() - started >= timeoutNanos) {
                    processLockTimeoutCount.incrementAndGet();
                    throw new IOException("timed out acquiring CPF file process lock: " + normalized);
                }
                LockSupport.parkNanos(Math.min(TimeUnit.MILLISECONDS.toNanos(10), timeoutNanos));
            }
        } catch (IOException | RuntimeException failure) {
            channel.close();
            throw failure;
        }
    }

    private record ProcessFileLock(FileChannel channel, FileLock lock) implements AutoCloseable {
        private ProcessFileLock {
            java.util.Objects.requireNonNull(channel, "channel");
            java.util.Objects.requireNonNull(lock, "lock");
        }

        private void ensureValid() throws IOException {
            if (!channel.isOpen() || !lock.isValid()) {
                throw new IOException("CPF file process lock is not valid");
            }
        }

        @Override
        public void close() throws IOException {
            try {
                lock.release();
            } finally {
                channel.close();
            }
        }
    }

    private Path logicalLockKey(Path path) {
        Path normalized = path.toAbsolutePath().normalize();
        String name = normalized.getFileName().toString();
        if (name.endsWith(".gz")) {
            return normalized.resolveSibling(name.substring(0, name.length() - 3));
        }
        return normalized;
    }

    int retainedLockEntryCount() {
        return fileLocks.size();
    }

    private Path resolveLogPath(String moduleCode, String logType) {
        return pathPolicy.generalLogPath(moduleCode, normalizeLogType(logType), currentLogDate());
    }

    private void createDirectoriesWithSecurePermissions(Path directory) throws IOException {
        Files.createDirectories(directory);
        Path root = logRoot().toAbsolutePath().normalize();
        Path target = directory.toAbsolutePath().normalize();
        if (!target.startsWith(root)) {
            throw new IOException("로그 디렉터리가 CPF_LOG_ROOT를 벗어났습니다: " + target);
        }
        applyDirectoryPermissions(root);
        Path current = root;
        for (Path part : root.relativize(target)) {
            current = current.resolve(part);
            applyDirectoryPermissions(current);
        }
    }

    private void applyDirectoryPermissions(Path directory) throws IOException {
        applyPosixPermissions(
                directory,
                environment.getProperty("cpf.logging.file.directory-permissions", "rwxr-x---"),
                true);
    }

    private void applyFilePermissions(Path file) throws IOException {
        applyPosixPermissions(
                file,
                environment.getProperty("cpf.logging.file.file-permissions", "rw-r-----"),
                false);
    }

    private void applyPosixPermissions(Path path, String configured, boolean directory) throws IOException {
        final Set<PosixFilePermission> permissions;
        try {
            permissions = PosixFilePermissions.fromString(configured);
        } catch (IllegalArgumentException ex) {
            throw new IOException("잘못된 로그 " + (directory ? "디렉터리" : "파일")
                    + " 권한 설정입니다: " + configured, ex);
        }
        boolean worldAccess = permissions.contains(PosixFilePermission.OTHERS_READ)
                || permissions.contains(PosixFilePermission.OTHERS_WRITE)
                || permissions.contains(PosixFilePermission.OTHERS_EXECUTE);
        boolean allowWorldAccess = environment.getProperty(
                "cpf.logging.file.allow-world-access", Boolean.class, false);
        if (worldAccess && !allowWorldAccess) {
            throw new IOException("로그 권한에 others 접근을 허용할 수 없습니다: " + configured);
        }
        PosixFileAttributeView view = Files.getFileAttributeView(path, PosixFileAttributeView.class);
        if (view != null) view.setPermissions(permissions);
    }

    private void ensureSafeWritableLogPath(Path path) throws IOException {
        Path managedRoot = logRoot().toAbsolutePath().normalize();
        Path normalized = path.toAbsolutePath().normalize();
        if (!normalized.startsWith(managedRoot)) {
            throw new IOException("log path escapes CPF_LOG_ROOT");
        }
        Path current = managedRoot;
        for (Path part : managedRoot.relativize(normalized)) {
            current = current.resolve(part);
            if (Files.exists(current, LinkOption.NOFOLLOW_LINKS) && Files.isSymbolicLink(current)) {
                throw new IOException("symbolic log path segments are forbidden");
            }
        }
        Path rootReal = managedRoot.toRealPath();
        Path parentReal = normalized.getParent().toRealPath();
        if (!parentReal.startsWith(rootReal)) {
            throw new IOException("log parent escapes CPF_LOG_ROOT");
        }
    }

    private boolean isSafeManagedLogFile(Path path) {
        try {
            Path managedRoot = logRoot().toAbsolutePath().normalize();
            Path normalized = path.toAbsolutePath().normalize();
            return normalized.startsWith(managedRoot)
                    && !Files.isSymbolicLink(normalized)
                    && Files.isRegularFile(normalized, LinkOption.NOFOLLOW_LINKS);
        } catch (RuntimeException failure) {
            return false;
        }
    }

    private void applyRetentionOnce(Path activeLogPath) throws IOException {
        if (!Files.isDirectory(logRoot()) || !shouldRunRetention() || !withinRetentionMaintenanceWindow()) {
            retentionSkipCount.incrementAndGet();
            return;
        }
        retentionRunCount.incrementAndGet();
        lastRetentionStartedAt.set(clock.instant());
        long deadlineNanos = retentionDeadlineNanos();
        ProcessFileLock retentionGuard = acquireNamedProcessLock(
                pathPolicy.instanceRoot().resolve(".cpf-retention.lock"));
        try {
            retentionGuard.ensureValid();
            int maxHistoryDays = bounded(
                    environment.getProperty("cpf.logging.file.max-history-days", Integer.class, 30),
                    1, 3_650, "cpf.logging.file.max-history-days");
            Instant cutoff = currentLogDate().minusDays(maxHistoryDays).atStartOfDay(logZoneId).toInstant();
            List<Path> candidates;
            try (var files = Files.walk(logRoot())) {
                candidates = files.filter(this::isSafeManagedLogFile)
                        .filter(path -> path.getFileName().toString().matches(".*\\.log(?:\\.gz)?$"))
                        .sorted(Comparator.comparing(this::lastModified))
                        .toList();
            }
            for (Path candidate : candidates) {
                if (retentionDeadlineExceeded(deadlineNanos)) break;
                if (logicalLockKey(candidate).equals(logicalLockKey(activeLogPath))) continue;
                FileLockEntry candidateLock = acquireFileLock(candidate);
                ProcessFileLock candidateGuard = acquireProcessFileLock(candidate);
                try {
                    candidateGuard.ensureValid();
                    if (!isSafeManagedLogFile(candidate)) continue;
                    boolean changed = false;
                    if (logicalLogInstant(candidate).isBefore(cutoff)) {
                        if (Files.deleteIfExists(candidate)) {
                            deletedFileCount.incrementAndGet();
                            changed = true;
                        }
                    } else if (archiveCompressionEnabled()
                            && candidate.getFileName().toString().endsWith(".log")
                            && isPreviousLogDate(candidate)) {
                        compressLog(candidate);
                        changed = true;
                    }
                    if (changed) retentionThrottle();
                } finally {
                    try { candidateGuard.close(); } finally { releaseFileLock(candidate, candidateLock); }
                }
            }
            applyMaxHistoryCount(activeLogPath, deadlineNanos);
            applyTotalSizeCap(activeLogPath, deadlineNanos);
            lastRetentionCompletedAt.set(clock.instant());
        } finally {
            retentionGuard.close();
        }
    }

    private synchronized boolean shouldRunRetention() {
        long intervalMs = bounded(
                environment.getProperty("cpf.logging.file.retention-check-interval-ms", Long.class, 60_000L),
                0L, 86_400_000L, "cpf.logging.file.retention-check-interval-ms");
        long now = clock.millis();
        long nextCheck = nextRetentionCheckEpochMillis.get();
        if (now < nextCheck) {
            return false;
        }
        nextRetentionCheckEpochMillis.set(saturatingAdd(now, intervalMs));
        return true;
    }

    private boolean archiveCompressionEnabled() {
        return environment.getProperty(
                "cpf.logging.file.archive-compress-enabled",
                Boolean.class,
                true);
    }

    private void applyMaxHistoryCount(Path activeLogPath, long deadlineNanos) throws IOException {
        int maxHistory = bounded(environment.getProperty("cpf.logging.file.max-history", Integer.class, 0),
                0, 1_000_000, "cpf.logging.file.max-history");
        if (maxHistory == 0) return;
        List<Path> archives;
        try (var stream = Files.walk(logRoot())) {
            archives = stream.filter(this::isSafeManagedLogFile)
                    .filter(path -> path.getFileName().toString().matches(".*\\.log(?:\\.gz)?$"))
                    .filter(path -> !logicalLockKey(path).equals(logicalLockKey(activeLogPath)))
                    .sorted(Comparator.comparing(this::lastModified).reversed())
                    .toList();
        }
        for (int i = maxHistory; i < archives.size(); i++) {
            if (retentionDeadlineExceeded(deadlineNanos)) return;
            Path file = archives.get(i);
            FileLockEntry candidateLock = acquireFileLock(file);
            ProcessFileLock guard = acquireProcessFileLock(file);
            try {
                guard.ensureValid();
                if (isSafeManagedLogFile(file) && Files.deleteIfExists(file)) {
                    deletedFileCount.incrementAndGet();
                    retentionThrottle();
                }
            } finally {
                try { guard.close(); } finally { releaseFileLock(file, candidateLock); }
            }
        }
    }

    private void applyTotalSizeCap(Path activeLogPath, long deadlineNanos) throws IOException {
        long capBytes = parseSize(environment.getProperty("cpf.logging.file.total-size-cap", "2GB"));
        if (capBytes < 1) return;
        List<Path> files;
        try (var stream = Files.walk(logRoot())) {
            files = stream.filter(this::isSafeManagedLogFile)
                    .filter(path -> path.getFileName().toString().matches(".*\\.log(?:\\.gz)?$"))
                    .sorted(Comparator.comparing(this::lastModified))
                    .toList();
        }
        long total = 0L;
        for (Path file : files) {
            try { total = Math.addExact(total, Files.size(file)); }
            catch (ArithmeticException overflow) { throw new IOException("log size total exceeds supported range", overflow); }
        }
        for (Path file : files) {
            if (total <= capBytes || retentionDeadlineExceeded(deadlineNanos)) break;
            if (logicalLockKey(file).equals(logicalLockKey(activeLogPath))) continue;
            FileLockEntry candidateLock = acquireFileLock(file);
            ProcessFileLock processGuard = acquireProcessFileLock(file);
            try {
                processGuard.ensureValid();
                if (!isSafeManagedLogFile(file)) continue;
                long size = Files.size(file);
                if (Files.deleteIfExists(file)) {
                    total -= size;
                    deletedFileCount.incrementAndGet();
                    retentionThrottle();
                }
            } finally {
                try { processGuard.close(); } finally { releaseFileLock(file, candidateLock); }
            }
        }
    }

    private boolean withinRetentionMaintenanceWindow() {
        String startText = environment.getProperty("cpf.logging.file.maintenance-start", "").trim();
        String endText = environment.getProperty("cpf.logging.file.maintenance-end", "").trim();
        if (startText.isEmpty() && endText.isEmpty()) return true;
        if (startText.isEmpty() || endText.isEmpty()) throw new IllegalArgumentException("file retention maintenance window requires start and end");
        LocalTime start = LocalTime.parse(startText); LocalTime end = LocalTime.parse(endText);
        LocalTime now = clock.instant().atZone(logZoneId).toLocalTime();
        if (start.equals(end)) return true;
        return start.isBefore(end) ? !now.isBefore(start) && now.isBefore(end) : !now.isBefore(start) || now.isBefore(end);
    }

    private long retentionDeadlineNanos() {
        long maxRuntimeMs = bounded(environment.getProperty("cpf.logging.file.retention-max-runtime-ms", Long.class, 30_000L),
                1L, 3_600_000L, "cpf.logging.file.retention-max-runtime-ms");
        long nanos = TimeUnit.MILLISECONDS.toNanos(maxRuntimeMs); long now = System.nanoTime();
        return Long.MAX_VALUE - now < nanos ? Long.MAX_VALUE : now + nanos;
    }
    private static boolean retentionDeadlineExceeded(long deadlineNanos) { return System.nanoTime() - deadlineNanos >= 0; }
    private void retentionThrottle() {
        long throttleMs = bounded(environment.getProperty("cpf.logging.file.retention-throttle-ms", Long.class, 0L),
                0L, 60_000L, "cpf.logging.file.retention-throttle-ms");
        if (throttleMs > 0) LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(throttleMs));
        if (Thread.currentThread().isInterrupted()) throw new IllegalStateException("file retention interrupted");
    }

    private Instant lastModified(Path path) {
        try {
            return Files.getLastModifiedTime(path, LinkOption.NOFOLLOW_LINKS).toInstant();
        } catch (IOException ex) {
            return Instant.MAX;
        }
    }

    /**
     * 로그 파일명에 포함된 업무일자를 우선 사용하고, 표준 일자를 찾지 못한 파일만 수정시각으로 판단합니다.
     */
    private Instant logicalLogInstant(Path path) {
        LocalDate logicalDate = extractLogDate(path);
        return logicalDate != null
                ? logicalDate.atStartOfDay(logZoneId).toInstant()
                : lastModified(path);
    }

    private boolean isPreviousLogDate(Path path) {
        LocalDate logicalDate = extractLogDate(path);
        if (logicalDate != null) {
            return logicalDate.isBefore(currentLogDate());
        }
        return lastModified(path).isBefore(currentLogDate().atStartOfDay(logZoneId).toInstant());
    }

    private LocalDate extractLogDate(Path path) {
        String fileName = path.getFileName().toString();
        Matcher isoMatcher = ISO_LOG_DATE_PATTERN.matcher(fileName);
        if (isoMatcher.find()) {
            try {
                return LocalDate.parse(isoMatcher.group(1), DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (RuntimeException invalidDate) {
                return null;
            }
        }
        Matcher basicMatcher = BASIC_LOG_DATE_PATTERN.matcher(fileName);
        if (basicMatcher.find()) {
            try {
                return LocalDate.parse(basicMatcher.group(1), DateTimeFormatter.BASIC_ISO_DATE);
            } catch (RuntimeException invalidDate) {
                return null;
            }
        }
        return null;
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
            long amount = Long.parseLong(normalized.trim());
            if (amount < 0L) throw new IllegalArgumentException("size cannot be negative");
            return Math.multiplyExact(amount, multiplier);
        } catch (ArithmeticException | NumberFormatException ex) {
            throw new IllegalArgumentException("cpf.logging.file.total-size-cap 형식이 올바르지 않습니다: " + value, ex);
        }
    }

    private void restoreCompressedLog(Path logPath) throws IOException {
        ensureSafeWritableLogPath(logPath);
        Path gzipPath = logPath.resolveSibling(logPath.getFileName() + ".gz");
        if (!Files.exists(gzipPath, LinkOption.NOFOLLOW_LINKS)) return;
        if (!isSafeManagedLogFile(gzipPath)) {
            throw new IOException("compressed log is not a safe managed file");
        }
        Path restorePath = logPath.resolveSibling(logPath.getFileName() + ".restore-"
                + UUID.randomUUID() + ".tmp");
        try {
            try (InputStream input = new GZIPInputStream(Files.newInputStream(gzipPath));
                 OutputStream output = Files.newOutputStream(
                         restorePath,
                         StandardOpenOption.CREATE,
                         StandardOpenOption.TRUNCATE_EXISTING,
                         StandardOpenOption.WRITE,
                         LinkOption.NOFOLLOW_LINKS)) {
                input.transferTo(output);
                if (Files.isRegularFile(logPath)) {
                    Files.copy(logPath, output);
                }
            }
            moveReplacing(restorePath, logPath);
            applyFilePermissions(logPath);
            Files.deleteIfExists(gzipPath);
        } finally {
            Files.deleteIfExists(restorePath);
        }
    }

    private void compressLog(Path logPath) throws IOException {
        if (!isSafeManagedLogFile(logPath)) throw new IOException("log archive source is not safe");
        Path gzipPath = logPath.resolveSibling(logPath.getFileName() + ".gz");
        ensureSafeWritableLogPath(gzipPath);
        Path temporaryPath = gzipPath.resolveSibling(gzipPath.getFileName() + ".tmp-"
                + UUID.randomUUID());
        try {
            try (InputStream input = Files.newInputStream(logPath);
                 OutputStream output = new GZIPOutputStream(Files.newOutputStream(
                         temporaryPath,
                         StandardOpenOption.CREATE,
                         StandardOpenOption.TRUNCATE_EXISTING,
                         StandardOpenOption.WRITE,
                         LinkOption.NOFOLLOW_LINKS))) {
                input.transferTo(output);
            }
            moveReplacing(temporaryPath, gzipPath);
            applyFilePermissions(gzipPath);
            if (Files.deleteIfExists(logPath)) {
                compressedFileCount.incrementAndGet();
            }
        } finally {
            Files.deleteIfExists(temporaryPath);
        }
    }

    private static void moveReplacing(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException unsupported) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static long saturatingAdd(long left, long right) {
        try {
            return Math.addExact(left, right);
        } catch (ArithmeticException overflow) {
            return Long.MAX_VALUE;
        }
    }

    private static int bounded(int value, int minimum, int maximum, String name) {
        if (value < minimum || value > maximum) {
            throw new IllegalArgumentException(name + " must be between " + minimum + " and " + maximum);
        }
        return value;
    }

    private static long bounded(long value, long minimum, long maximum, String name) {
        if (value < minimum || value > maximum) {
            throw new IllegalArgumentException(name + " must be between " + minimum + " and " + maximum);
        }
        return value;
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
        return "CPF";
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
        return value == null ? null : CpfMaskingRuntime.mask(value);
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

    private static final class FileLockEntry {
        private final ReentrantLock lock = new ReentrantLock(true);
        private int references;
    }

}

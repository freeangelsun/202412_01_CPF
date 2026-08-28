package com.cpf.starter.logging.internal;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import com.cpf.common.logging.CpfApplicationLoggingPolicy;
import com.cpf.common.logging.CpfLogFilePolicy;
import com.cpf.common.logging.CpfRuntimeLogMaintenance;
import com.cpf.common.logging.CpfRuntimeLogPathPolicy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

/** Logback 전용 코드를 Internal adapter 경계에 격리하고 Console appender는 그대로 유지합니다. */
public final class CpfRuntimeLoggingLifecycle implements SmartLifecycle {
    private static final Logger log = LoggerFactory.getLogger(CpfRuntimeLoggingLifecycle.class);
    private static final String APPENDER_PREFIX = "CPF_RUNTIME_FILE_";
    private final CpfApplicationLoggingPolicy policy;
    private final Duration maintenanceInterval;
    private final CpfRuntimeLogMaintenance maintenance;
    private final List<RollingFileAppender<ILoggingEvent>> appenders = new ArrayList<>();
    private ScheduledExecutorService scheduler;
    private volatile boolean running;

    public CpfRuntimeLoggingLifecycle(
            CpfApplicationLoggingPolicy policy, Duration maintenanceInterval, Clock clock) {
        this.policy = policy;
        this.maintenanceInterval = maintenanceInterval;
        this.maintenance = new CpfRuntimeLogMaintenance(clock);
    }

    @Override
    public synchronized void start() {
        if (running) return;
        if (!(LoggerFactory.getILoggerFactory() instanceof LoggerContext context)) {
            throw new IllegalStateException("CPF 기본 File Logging은 Logback이 필요합니다. 다른 SLF4J backend를 "
                    + "사용하면 cpf.logging.enabled=false로 설정하고 해당 backend의 file policy를 구성하세요.");
        }
        try {
            Path directory = CpfRuntimeLogPathPolicy.resolveDirectory(
                    policy.root(), policy.applicationName(), policy.instanceId());
            Files.createDirectories(directory.resolve("archive"));
            var rootLogger = context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
            for (var entry : policy.files().entrySet()) {
                if (!entry.getValue().enabled()) continue;
                RollingFileAppender<ILoggingEvent> appender = createAppender(
                        context, directory, entry.getKey(), entry.getValue());
                Appender<ILoggingEvent> previous = rootLogger.getAppender(appender.getName());
                if (previous != null) {
                    rootLogger.detachAppender(previous);
                    previous.stop();
                }
                rootLogger.addAppender(appender);
                appenders.add(appender);
            }
            running = true;
            maintainSafely();
            scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
                Thread thread = new Thread(runnable, "cpf-runtime-log-maintenance");
                thread.setDaemon(true);
                return thread;
            });
            scheduler.scheduleWithFixedDelay(this::maintainSafely,
                    maintenanceInterval.toMillis(), maintenanceInterval.toMillis(), TimeUnit.MILLISECONDS);
            log.info("CPF_RUNTIME_LOGGING_READY application={} instance={} path={} files={}",
                    policy.applicationName(), policy.instanceId(), directory,
                    policy.files().entrySet().stream().filter(entry -> entry.getValue().enabled())
                            .map((java.util.Map.Entry<String, CpfLogFilePolicy> entry) -> entry.getKey()).toList());
        } catch (Exception failure) {
            stop();
            throw new IllegalStateException("CPF Runtime 로그 초기화에 실패했습니다. cpf.logging.root/files와 "
                    + "디렉터리 권한을 확인하세요: " + failure.getMessage(), failure);
        }
    }

    private RollingFileAppender<ILoggingEvent> createAppender(
            LoggerContext context, Path directory, String logicalName, CpfLogFilePolicy file) {
        String appenderName = APPENDER_PREFIX
                + logicalName.toUpperCase(Locale.ROOT).replace('-', '_');
        RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<>();
        appender.setContext(context);
        appender.setName(appenderName);
        appender.setAppend(true);
        appender.setFile(directory.resolve(file.fileName()).toString());

        String stem = file.fileName().substring(0, file.fileName().length() - ".log".length());
        TimeBasedRollingPolicy<ILoggingEvent> rolling = new TimeBasedRollingPolicy<>();
        rolling.setContext(context);
        rolling.setParent(appender);
        rolling.setFileNamePattern(directory.resolve("archive")
                .resolve(stem + ".%d{yyyy-MM-dd}.log").toString());
        rolling.setCleanHistoryOnStart(false);
        rolling.setMaxHistory(0);
        rolling.start();
        appender.setRollingPolicy(rolling);

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%d{ISO8601} level=%level application=" + policy.applicationName()
                + " instance=" + policy.instanceId()
                + " transactionId=%X{transactionId} systemCode=%X{systemCode}"
                + " callerSystemCode=%X{callerSystemCode} targetSystemCode=%X{targetSystemCode}"
                + " operationId=%X{operationId} traceId=%X{traceId} logger=%logger{48}"
                + " msg=\"%replace(%msg){'[\\r\\n]+',' '}\"%n");
        encoder.start();
        appender.setEncoder(encoder);
        if (file.level() != null) {
            ThresholdFilter filter = new ThresholdFilter();
            filter.setContext(context);
            filter.setLevel(Level.toLevel(file.level()).levelStr);
            filter.start();
            appender.addFilter(filter);
        }
        appender.start();
        return appender;
    }

    private void maintainSafely() {
        try {
            var result = maintenance.maintain(policy);
            if (result.skippedBecauseLocked()) {
                log.debug("CPF_RUNTIME_LOG_MAINTENANCE_SKIPPED reason=LOCKED");
            } else if (result.successful()) {
                log.info("CPF_RUNTIME_LOG_MAINTENANCE_SUCCESS scanned={} compressed={} deleted={}",
                        result.scannedFiles(), result.compressedFiles(), result.deletedFiles());
            } else {
                log.error("CPF_RUNTIME_LOG_MAINTENANCE_PARTIAL scanned={} compressed={} deleted={} failures={}",
                        result.scannedFiles(), result.compressedFiles(), result.deletedFiles(), result.failures());
            }
        } catch (Exception failure) {
            log.error("CPF_RUNTIME_LOG_MAINTENANCE_FAILED action=CHECK_CPF_LOG_ROOT_AND_PERMISSIONS", failure);
        }
    }

    @Override
    public synchronized void stop() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
        if (LoggerFactory.getILoggerFactory() instanceof LoggerContext context) {
            var root = context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
            for (RollingFileAppender<ILoggingEvent> appender : appenders) {
                root.detachAppender(appender);
                appender.stop();
            }
        }
        appenders.clear();
        running = false;
    }

    @Override public boolean isRunning() { return running; }
    @Override public boolean isAutoStartup() { return true; }
    @Override public int getPhase() { return Integer.MIN_VALUE + 100; }
}

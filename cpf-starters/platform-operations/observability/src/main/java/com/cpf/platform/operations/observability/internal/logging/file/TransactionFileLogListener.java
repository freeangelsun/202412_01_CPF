package com.cpf.platform.operations.observability.internal.logging.file;

import com.cpf.platform.operations.observability.spi.logging.TransactionLogRecord;
import com.cpf.platform.operations.observability.api.CpfTelemetry;
import com.cpf.platform.operations.observability.api.CpfTraceContext;
import com.cpf.platform.operations.observability.internal.logging.CpfTraceSamplingPolicy;
import com.cpf.platform.operations.observability.internal.logging.CpfTransactionTraceEnricher;
import com.cpf.platform.operations.observability.internal.logging.TransactionLogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 온라인 거래 이벤트를 구조화 파일 로그로 연결합니다.
 */
@Component
public class TransactionFileLogListener {
    private static final Logger log = LoggerFactory.getLogger(TransactionFileLogListener.class);
    private final CpfAsyncFileLogWriter fileLogWriter;
    private volatile CpfTelemetry telemetry = CpfTelemetry.noop();
    private volatile CpfTraceSamplingPolicy traceSamplingPolicy = new CpfTraceSamplingPolicy();

    @Autowired
    public TransactionFileLogListener(CpfAsyncFileLogWriter fileLogWriter) {
        this.fileLogWriter = fileLogWriter;
    }

    @Autowired
    void configureTelemetry(CpfTelemetry telemetry, CpfTraceSamplingPolicy traceSamplingPolicy) {
        this.telemetry = telemetry;
        this.traceSamplingPolicy = traceSamplingPolicy;
    }

    @EventListener
    public void handleTransactionLogEvent(TransactionLogEvent event) {
        if (event == null || event.getRecord() == null) {
            return;
        }
        CpfTraceContext context = CpfTransactionTraceEnricher.enrich(event.getRecord());
        CpfTelemetry.CpfTelemetrySpan span = startFileSpan(context, event.getRecord());
        try {
            CpfAsyncFileLogWriter.PublishResult result =
                    fileLogWriter.publish(event.getRecord(), event.getDetails(), event.getLogPolicy());
            if (result == CpfAsyncFileLogWriter.PublishResult.FAILED
                    || result == CpfAsyncFileLogWriter.PublishResult.REJECTED
                    || result == CpfAsyncFileLogWriter.PublishResult.CLOSED) {
                markSpanError(span, new IllegalStateException("CPF_FILE_LOG_" + result.name()));
            }
        } catch (RuntimeException failure) {
            markSpanError(span, failure);
            log.warn("CPF file-log listener failure was isolated from the business event. failureType={}",
                    failure.getClass().getName());
        } finally {
            closeSpan(span);
        }
    }

    private static void markSpanError(CpfTelemetry.CpfTelemetrySpan span, Throwable failure) {
        try {
            span.error(failure);
        } catch (RuntimeException ignored) {
            // Telemetry is observational and must not replace the file-log boundary failure.
        }
    }

    private static void closeSpan(CpfTelemetry.CpfTelemetrySpan span) {
        try {
            span.close();
        } catch (RuntimeException ignored) {
            // A provider close failure must not escape a successful or already-failed file-log call.
        }
    }

    private CpfTelemetry.CpfTelemetrySpan startFileSpan(
            CpfTraceContext context, com.cpf.platform.operations.observability.spi.logging.TransactionLogRecord record) {
        try {
            boolean success = record.getErrorCode() == null && record.getErrorMessage() == null
                    && (record.getHttpStatus() == null || record.getHttpStatus() < 400);
            if (!traceSamplingPolicy.shouldSample(
                    record.getTransactionId(), record.getStandardExecutionId(), record.getModuleId(), success)) {
                return CpfTelemetry.noop().startSpan("transaction-log.file", "FILE", java.util.Map.of());
            }
            return telemetry.startSpan(context.child(CpfTraceContext.SpanKind.FILE,
                    "transaction-log.file", "FILE_LOG", Math.max(1, context.attempt() + 1), java.util.Map.of()));
        } catch (RuntimeException ignored) {
            return CpfTelemetry.noop().startSpan("transaction-log.file", "FILE", java.util.Map.of());
        }
    }
}

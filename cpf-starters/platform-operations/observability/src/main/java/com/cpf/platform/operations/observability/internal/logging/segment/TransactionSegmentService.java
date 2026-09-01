package com.cpf.platform.operations.observability.internal.logging.segment;

import org.springframework.beans.factory.annotation.Autowired;
import com.cpf.platform.operations.observability.spi.logging.segment.TransactionSegmentRecord;
import com.cpf.foundation.context.header.CpfHeaderAuditLogger;
import com.cpf.platform.operations.observability.internal.logging.header.CpfHeaderPropagator;
import com.cpf.security.api.CpfMaskingRuntime;
import com.cpf.platform.operations.observability.internal.logging.TransactionContext;
import com.cpf.platform.operations.observability.internal.logging.TransactionHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * 복합 거래의 구간 시작/종료를 표준 방식으로 기록합니다.
 */
@Service
public class TransactionSegmentService {
    private static final Logger log = LoggerFactory.getLogger(TransactionSegmentService.class);

    private final TransactionSegmentPersistenceService persistenceService;
    private final Clock clock;

    // 생성자가 둘이면 Spring 은 어느 쪽을 쓸지 정하지 못하고 기본 생성자를 찾다가 기동에
    // 실패한다. 운영 주입 대상 생성자를 명시한다. 나머지는 테스트 seam 이다.
    @Autowired
    public TransactionSegmentService(TransactionSegmentPersistenceService persistenceService,
                                     org.springframework.beans.factory.ObjectProvider<Clock> clockProvider) {
        this.persistenceService = persistenceService;
        this.clock = clockProvider.getIfUnique(Clock::systemUTC);
    }

    TransactionSegmentService(TransactionSegmentPersistenceService persistenceService, Clock cpfStarterClock) {
        this.persistenceService = persistenceService;
        this.clock = java.util.Objects.requireNonNull(cpfStarterClock, "clock");
    }

    public TransactionSegmentScope start(
            TransactionSegmentRole role,
            TransactionSegmentDirection direction,
            String moduleCode,
            String sourceModuleCode,
            String targetModuleCode,
            String apiPath,
            String transactionName) {

        TransactionHeader header = TransactionContext.currentHeader();
        TransactionSegmentContext.TransactionSegmentFrame currentFrame = TransactionSegmentContext.currentFrame();

        String transactionId = TransactionContext.getOrCreateTransactionId();
        String parentSegmentId = firstText(
                currentFrame != null ? currentFrame.transactionSegmentId() : null,
                TransactionSegmentContext.incomingParentSegmentId(header));
        int callDepth = currentFrame != null
                ? currentFrame.callDepth() + 1
                : Math.max(0, TransactionSegmentContext.incomingCallDepth(header) + 1);
        int sequenceNo = TransactionContext.nextSequenceNo();

        TransactionSegmentRecord record = new TransactionSegmentRecord();
        record.setTransactionSegmentId(segmentId(transactionId, sequenceNo));
        record.setTransactionId(transactionId);
        record.setExecutionId(com.cpf.core.api.context.CpfContexts.currentExecutionId());
        record.setParentSegmentId(parentSegmentId);
        record.setTransactionRole(roleName(role));
        record.setModuleCode(normalizeCode(moduleCode, "N/A"));
        record.setSourceModuleCode(normalizeCode(sourceModuleCode, null));
        record.setTargetModuleCode(normalizeCode(targetModuleCode, null));
        record.setDirection(directionName(direction));
        record.setCallDepth(callDepth);
        record.setSequenceNo(sequenceNo);
        record.setApiPath(CpfMaskingRuntime.truncate(apiPath, 500));
        record.setTransactionName(CpfMaskingRuntime.truncate(transactionName, 200));
        record.setStartedAt(LocalDateTime.now(clock));
        record.setStatus(TransactionSegmentStatus.RUNNING.name());
        record.setFailureYn("N");
        record.setRequestHeaderSnapshotMasked(CpfHeaderAuditLogger.toJson(CpfHeaderPropagator.resolvedHeaders()));
        record.setExtensionHeaderSnapshotMasked(header != null
                ? CpfHeaderAuditLogger.toJson(header.getExtensionHeaders())
                : null);
        record.setCustomerNoMasked(maskIdentity(TransactionContext.customerNo()));
        record.setMemberNoMasked(maskIdentity(TransactionContext.memberNo()));
        record.setUserIdMasked(maskIdentity(TransactionContext.userId()));
        record.setOperatorIdMasked(maskIdentity(TransactionContext.operatorId()));
        record.setSystemCode(CpfMaskingRuntime.truncate(TransactionContext.currentSystemCode(), 30));
        record.setOriginalSystemCode(CpfMaskingRuntime.truncate(TransactionContext.originalSystemCode(), 30));
        record.setCallerSystemCode(CpfMaskingRuntime.truncate(TransactionContext.callerSystemCode(), 100));
        record.setTargetSystemCode(CpfMaskingRuntime.truncate(TransactionContext.targetSystemCode(), 32));
        record.setCurrentChannel(TransactionContext.currentChannel());
        record.setOriginalChannel(TransactionContext.originalChannel());
        record.setClientId(CpfMaskingRuntime.truncate(TransactionContext.clientId(), 100));
        record.setCallerChannel(CpfMaskingRuntime.truncate(TransactionContext.callerChannel(), 100));
        record.setTargetChannel(CpfMaskingRuntime.truncate(TransactionContext.targetChannel(), 100));
        record.setTargetOperationId(CpfMaskingRuntime.truncate(TransactionContext.observedOperationId(), 160));
        record.setCreatedBy(requestUser());
        record.setUpdatedBy(record.getCreatedBy());

        try {
            persistenceService.insert(record);
        } catch (RuntimeException ex) {
            log.warn("Failed to persist transaction segment start. transactionId={}, segmentId={}",
                    record.getTransactionId(), record.getTransactionSegmentId(), ex);
        }

        TransactionSegmentContext.push(new TransactionSegmentContext.TransactionSegmentFrame(
                record.getTransactionSegmentId(),
                record.getTransactionId(),
                record.getCallDepth()));
        return new TransactionSegmentScope(this, record);
    }

    public <T> T around(
            TransactionSegmentRole role,
            TransactionSegmentDirection direction,
            String moduleCode,
            String sourceModuleCode,
            String targetModuleCode,
            String apiPath,
            String transactionName,
            Supplier<T> supplier) {
        TransactionSegmentScope scope = start(role, direction, moduleCode, sourceModuleCode, targetModuleCode, apiPath, transactionName);
        try {
            T result = supplier.get();
            scope.success();
            return result;
        } catch (RuntimeException ex) {
            scope.fail(ex.getClass().getSimpleName(), ex.getMessage());
            throw ex;
        }
    }

    void finish(
            TransactionSegmentRecord record,
            TransactionSegmentStatus status,
            String failureCode,
            String failureMessage,
            long durationMs) {
        try {
            record.setEndedAt(LocalDateTime.now(clock));
            record.setDurationMs(Math.max(0L, durationMs));
            record.setStatus(status.name());
            record.setFailureYn(status == TransactionSegmentStatus.SUCCESS ? "N" : "Y");
            record.setFailureCode(CpfMaskingRuntime.truncate(failureCode, 100));
            record.setFailureMessageMasked(CpfMaskingRuntime.mask(failureMessage, 1000));
            record.setResponseHeaderSnapshotMasked(CpfHeaderAuditLogger.toJson(CpfHeaderPropagator.currentSnapshot(TransactionContext.currentHeader()).responseHeaders()));
            record.setUpdatedBy(requestUser());
            persistenceService.updateEnd(record);
        } catch (RuntimeException ex) {
            log.warn("Failed to persist transaction segment end. transactionId={}, segmentId={}",
                    record.getTransactionId(), record.getTransactionSegmentId(), ex);
        } finally {
            TransactionSegmentContext.pop(record.getTransactionSegmentId());
        }
    }

    private String segmentId(String transactionId, int sequenceNo) {
        String suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        return CpfMaskingRuntime.truncate(transactionId + "-SEG-" + String.format("%04d", sequenceNo) + "-" + suffix, 120);
    }

    private String roleName(TransactionSegmentRole role) {
        return role != null ? role.name() : TransactionSegmentRole.SUB.name();
    }

    private String directionName(TransactionSegmentDirection direction) {
        return direction != null ? direction.name() : TransactionSegmentDirection.INTERNAL.name();
    }

    private String normalizeCode(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim().toUpperCase(Locale.ROOT);
    }

    private String requestUser() {
        return firstText(TransactionContext.operatorId(), TransactionContext.userId(), "CPF");
    }

    private String maskIdentity(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        String trimmed = value.trim();
        if (trimmed.length() <= 4) {
            return "***";
        }
        return trimmed.substring(0, 2) + "***" + trimmed.substring(trimmed.length() - 2);
    }

    private String firstText(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }
}

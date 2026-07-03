package cpf.pfw.common.logging.segment;

import cpf.pfw.common.header.CpfHeaderAuditLogger;
import cpf.pfw.common.header.CpfHeaderPropagator;
import cpf.pfw.common.logging.SensitiveDataMasker;
import cpf.pfw.common.logging.TransactionContext;
import cpf.pfw.common.logging.TransactionHeader;
import cpf.pfw.mapper.common.logging.TransactionSegmentMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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

    private final TransactionSegmentMapper mapper;

    public TransactionSegmentService(TransactionSegmentMapper mapper) {
        this.mapper = mapper;
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

        String transactionGlobalId = TransactionContext.getOrCreateTransactionId();
        String rootTransactionGlobalId = firstText(
                TransactionSegmentContext.rootTransactionGlobalId(),
                header != null ? header.getRootTransactionGlobalId() : null,
                TransactionContext.originalTransactionId(),
                transactionGlobalId);
        String parentTransactionGlobalId = firstText(TransactionContext.parentTransactionId(), transactionGlobalId);
        String parentSegmentId = firstText(
                currentFrame != null ? currentFrame.transactionSegmentId() : null,
                TransactionSegmentContext.incomingParentSegmentId(header));
        int callDepth = currentFrame != null
                ? currentFrame.callDepth() + 1
                : Math.max(0, TransactionSegmentContext.incomingCallDepth(header) + 1);
        int sequenceNo = TransactionContext.nextSequenceNo();

        TransactionSegmentRecord record = new TransactionSegmentRecord();
        record.setTransactionSegmentId(segmentId(transactionGlobalId, sequenceNo));
        record.setTransactionGlobalId(transactionGlobalId);
        record.setRootTransactionGlobalId(rootTransactionGlobalId);
        record.setParentTransactionGlobalId(parentTransactionGlobalId);
        record.setParentSegmentId(parentSegmentId);
        record.setTransactionRole(roleName(role));
        record.setModuleCode(normalizeCode(moduleCode, "N/A"));
        record.setSourceModuleCode(normalizeCode(sourceModuleCode, null));
        record.setTargetModuleCode(normalizeCode(targetModuleCode, null));
        record.setDirection(directionName(direction));
        record.setCallDepth(callDepth);
        record.setSequenceNo(sequenceNo);
        record.setApiPath(SensitiveDataMasker.truncate(apiPath, 500));
        record.setTransactionName(SensitiveDataMasker.truncate(transactionName, 200));
        record.setStartedAt(LocalDateTime.now());
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
        record.setChannelCode(TransactionContext.channelCode());
        record.setOriginalChannelCode(TransactionContext.originalChannelCode());
        record.setClientAppId(SensitiveDataMasker.truncate(TransactionContext.clientAppId(), 100));
        record.setCallerService(SensitiveDataMasker.truncate(TransactionContext.callerService(), 100));
        record.setCreatedBy(requestUser());
        record.setUpdatedBy(record.getCreatedBy());

        try {
            mapper.insertSegment(record);
        } catch (RuntimeException ex) {
            log.warn("Failed to persist transaction segment start. transactionGlobalId={}, segmentId={}",
                    record.getTransactionGlobalId(), record.getTransactionSegmentId(), ex);
        }

        TransactionSegmentContext.push(new TransactionSegmentContext.TransactionSegmentFrame(
                record.getTransactionSegmentId(),
                record.getRootTransactionGlobalId(),
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
            record.setEndedAt(LocalDateTime.now());
            record.setDurationMs(Math.max(0L, durationMs));
            record.setStatus(status.name());
            record.setFailureYn(status == TransactionSegmentStatus.SUCCESS ? "N" : "Y");
            record.setFailureCode(SensitiveDataMasker.truncate(failureCode, 100));
            record.setFailureMessageMasked(SensitiveDataMasker.mask(failureMessage, 1000));
            record.setResponseHeaderSnapshotMasked(CpfHeaderAuditLogger.toJson(CpfHeaderPropagator.currentSnapshot(TransactionContext.currentHeader()).responseHeaders()));
            record.setUpdatedBy(requestUser());
            mapper.updateSegmentEnd(record);
        } catch (RuntimeException ex) {
            log.warn("Failed to persist transaction segment end. transactionGlobalId={}, segmentId={}",
                    record.getTransactionGlobalId(), record.getTransactionSegmentId(), ex);
        } finally {
            TransactionSegmentContext.pop(record.getTransactionSegmentId());
        }
    }

    private String segmentId(String transactionGlobalId, int sequenceNo) {
        String suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        return SensitiveDataMasker.truncate(transactionGlobalId + "-SEG-" + String.format("%04d", sequenceNo) + "-" + suffix, 120);
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
        return firstText(TransactionContext.operatorId(), TransactionContext.userId(), "PFW");
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

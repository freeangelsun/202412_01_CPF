package com.cpf.platform.operations.observability.internal.logging;

import com.cpf.platform.operations.observability.api.logging.policy.LogCaptureMode;
import com.cpf.platform.operations.observability.api.logging.policy.LogPolicyDecision;
import com.cpf.security.api.CpfMaskingRuntime;
import com.cpf.platform.operations.observability.spi.logging.TransactionLogRecord;
import com.cpf.platform.operations.observability.spi.logging.CpfTransactionLogPersistencePort;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Objects;

/** Persists transaction summary and masked detail records without mutating caller-owned collections. */
@Service
public class TransactionLogService {
    private final CpfTransactionLogPersistencePort logMapper;

    public TransactionLogService(CpfTransactionLogPersistencePort logMapper) {
        this.logMapper = Objects.requireNonNull(logMapper, "logMapper");
    }

    @Transactional(transactionManager = "cpfTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public void saveTransactionLog(TransactionLogRecord record, Map<String, String> details) {
        saveTransactionLog(record, details, null);
    }

    @Transactional(transactionManager = "cpfTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public void saveTransactionLog(
            TransactionLogRecord record, Map<String, String> details, LogPolicyDecision logPolicy) {
        if (record == null || (logPolicy != null && !logPolicy.dbLogEnabled())) return;
        String eventIdentity = CpfTransactionLogIdentity.ensure(record);
        if (logMapper.existsRecoveryEvent(eventIdentity)) {
            return;
        }

        applyRecordBodyPolicy(record, logPolicy);
        maskRecordAtPersistenceBoundary(record);
        try {
            logMapper.insertTransactionLog(record);
        } catch (DataIntegrityViolationException conflict) {
            if (logMapper.existsRecoveryEvent(eventIdentity)) {
                return;
            }
            throw conflict;
        }

        if (details != null) {
            details.forEach((key, value) -> {
                if (detailAllowed(key, logPolicy)) {
                    insertDetail(record.getLogIdx(), key, value, record.getExecUser());
                }
            });
        }
        if (record.getErrorMessage() != null) {
            insertDetail(record.getLogIdx(), "errorMessage", record.getErrorMessage(), record.getExecUser());
        }
    }

    /**
     * Final DB boundary: producer-side masking is never trusted. Summary columns and free-form
     * payload/error fields are sanitized immediately before the mapper/provider is invoked.
     */
    private void maskRecordAtPersistenceBoundary(TransactionLogRecord record) {
        record.setMemberNo(CpfMaskingRuntime.maskIdentifier(record.getMemberNo()));
        record.setCustomerNo(CpfMaskingRuntime.maskIdentifier(record.getCustomerNo()));
        record.setDeviceId(CpfMaskingRuntime.maskIdentifier(record.getDeviceId()));
        record.setClientIp(CpfMaskingRuntime.maskIdentifier(record.getClientIp()));
        record.setParameters(CpfMaskingRuntime.mask(record.getParameters()));
        record.setRequestBody(CpfMaskingRuntime.mask(record.getRequestBody()));
        record.setResponse(CpfMaskingRuntime.mask(record.getResponse()));
        record.setMessageContent(CpfMaskingRuntime.mask(record.getMessageContent()));
        record.setErrorMessage(CpfMaskingRuntime.mask(record.getErrorMessage()));
        record.setExternalMessage(CpfMaskingRuntime.mask(record.getExternalMessage()));
        record.setInternalMessage(CpfMaskingRuntime.mask(record.getInternalMessage()));
        record.setReservedField1(CpfMaskingRuntime.mask(record.getReservedField1()));
        record.setReservedField2(CpfMaskingRuntime.mask(record.getReservedField2()));
        record.setReservedField3(CpfMaskingRuntime.mask(record.getReservedField3()));
        record.setReservedField4(CpfMaskingRuntime.mask(record.getReservedField4()));
        record.setReservedField5(CpfMaskingRuntime.mask(record.getReservedField5()));
    }

    private void applyRecordBodyPolicy(TransactionLogRecord record, LogPolicyDecision logPolicy) {
        if (logPolicy == null) return;
        if (!logPolicy.requestBodySave()) record.setRequestBody(null);
        if (!logPolicy.responseBodySave() && !"FAILURE".equals(record.getLogType())) record.setResponse(null);
        if (!capturesFullStack(logPolicy)) record.setInternalMessage(null);
    }

    private boolean detailAllowed(String key, LogPolicyDecision logPolicy) {
        if (logPolicy == null || key == null) return true;
        return switch (key) {
            case "requestBody" -> logPolicy.requestBodySave();
            case "response" -> logPolicy.responseBodySave();
            case "error.internalMessage" -> capturesFullStack(logPolicy);
            default -> true;
        };
    }

    private boolean capturesFullStack(LogPolicyDecision logPolicy) {
        return logPolicy != null && logPolicy.errorStackCaptureMode() == LogCaptureMode.FULL_MASKED;
    }

    private void insertDetail(Long logIdx, String detailKey, String detailValue, String auditUser) {
        if (logIdx == null) return;
        logMapper.insertTransactionLogDetail(
                logIdx,
                CpfMaskingRuntime.truncate(detailKey, 100),
                CpfMaskingRuntime.mask(detailValue),
                auditUser);
    }
}

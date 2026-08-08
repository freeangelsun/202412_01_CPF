package com.cpf.core.service.common.logging;

import com.cpf.core.api.logging.policy.LogCaptureMode;
import com.cpf.core.api.logging.policy.LogPolicyDecision;
import com.cpf.core.common.logging.CpfTransactionLogIdentity;
import com.cpf.core.common.logging.SensitiveDataMasker;
import com.cpf.core.common.logging.TransactionLogRecord;
import com.cpf.core.common.logging.spi.CpfTransactionLogPersistencePort;
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
        record.setMemberNo(SensitiveDataMasker.maskIdentifier(record.getMemberNo()));
        record.setCustomerNo(SensitiveDataMasker.maskIdentifier(record.getCustomerNo()));
        record.setDeviceId(SensitiveDataMasker.maskIdentifier(record.getDeviceId()));
        record.setClientIp(SensitiveDataMasker.maskIdentifier(record.getClientIp()));
        record.setParameters(SensitiveDataMasker.mask(record.getParameters()));
        record.setRequestBody(SensitiveDataMasker.mask(record.getRequestBody()));
        record.setResponse(SensitiveDataMasker.mask(record.getResponse()));
        record.setMessageContent(SensitiveDataMasker.mask(record.getMessageContent()));
        record.setErrorMessage(SensitiveDataMasker.mask(record.getErrorMessage()));
        record.setExternalMessage(SensitiveDataMasker.mask(record.getExternalMessage()));
        record.setInternalMessage(SensitiveDataMasker.mask(record.getInternalMessage()));
        record.setReservedField1(SensitiveDataMasker.mask(record.getReservedField1()));
        record.setReservedField2(SensitiveDataMasker.mask(record.getReservedField2()));
        record.setReservedField3(SensitiveDataMasker.mask(record.getReservedField3()));
        record.setReservedField4(SensitiveDataMasker.mask(record.getReservedField4()));
        record.setReservedField5(SensitiveDataMasker.mask(record.getReservedField5()));
    }

    private void applyRecordBodyPolicy(TransactionLogRecord record, LogPolicyDecision logPolicy) {
        if (logPolicy == null) return;
        if (!logPolicy.requestBodySave()) record.setRequestBody(null);
        if (!logPolicy.responseBodySave()) record.setResponse(null);
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
                SensitiveDataMasker.truncate(detailKey, 100),
                SensitiveDataMasker.mask(detailValue),
                auditUser);
    }
}

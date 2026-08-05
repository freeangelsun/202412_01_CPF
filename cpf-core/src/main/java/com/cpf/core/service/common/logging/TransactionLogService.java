package com.cpf.core.service.common.logging;

import com.cpf.core.api.logging.policy.LogCaptureMode;
import com.cpf.core.api.logging.policy.LogPolicyDecision;
import com.cpf.core.common.logging.CpfTransactionLogIdentity;
import com.cpf.core.common.logging.SensitiveDataMasker;
import com.cpf.core.common.logging.TransactionLogRecord;
import com.cpf.core.mapper.common.logging.TransactionLogMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Objects;

/** Persists transaction summary and masked detail records without mutating caller-owned collections. */
@Service
public class TransactionLogService {
    private final TransactionLogMapper logMapper;

    public TransactionLogService(TransactionLogMapper logMapper) {
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

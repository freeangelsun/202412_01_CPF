package com.cpf.core.service.common.logging;

import com.cpf.core.common.logging.SensitiveDataMasker;
import com.cpf.core.common.logging.TransactionLogRecord;
import com.cpf.core.common.logging.policy.LogPolicyDecision;
import com.cpf.core.mapper.common.logging.TransactionLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.util.Map;

/**
 * CPF 거래 요약 로그와 상세 로그를 저장합니다.
 *
 * <p>요약 정보는 {@code cpf_transaction_log}, 요청/응답/오류 같은 본문성 데이터는
 * {@code cpf_transaction_log_detail}에 분리 저장합니다. 로그 정책이 함께 전달되면
 * DB 저장 여부와 본문 저장 여부를 최종 저장 직전에 한 번 더 확인합니다.</p>
 */
@Service
@RequiredArgsConstructor
public class TransactionLogService {

    private final TransactionLogMapper logMapper;

    @Transactional(transactionManager = "cpfTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public void saveTransactionLog(TransactionLogRecord record, Map<String, String> details) {
        saveTransactionLog(record, details, null);
    }

    @Transactional(transactionManager = "cpfTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public void saveTransactionLog(TransactionLogRecord record, Map<String, String> details, LogPolicyDecision logPolicy) {
        if (record == null) {
            return;
        }
        if (logPolicy != null && !logPolicy.dbLogEnabled()) {
            return;
        }
        if (record.getRecoveryEventId() != null
                && !record.getRecoveryEventId().isBlank()
                && logMapper.existsRecoveryEvent(record.getRecoveryEventId())) {
            return;
        }

        applyBodyPolicy(record, details, logPolicy);

        // 요약 로그를 먼저 저장해 상세 로그가 참조할 LOG_IDX를 확보합니다.
        logMapper.insertTransactionLog(record);

        if (details != null) {
            // AOP가 수집한 헤더, 요청/응답, 실행 메타를 상세 로그로 분리 저장합니다.
            details.forEach((key, value) -> insertDetail(record.getLogIdx(), key, value, record.getExecUser()));
        }

        if (record.getErrorMessage() != null) {
            // 오류 메시지는 상세 검색 편의를 위해 명시적인 detail key로 한 번 더 보관합니다.
            insertDetail(record.getLogIdx(), "errorMessage", record.getErrorMessage(), record.getExecUser());
        }
    }

    private void applyBodyPolicy(TransactionLogRecord record, Map<String, String> details, LogPolicyDecision logPolicy) {
        if (logPolicy == null) {
            return;
        }
        if (!logPolicy.requestBodySave()) {
            record.setRequestBody(null);
            if (details != null) {
                details.remove("requestBody");
            }
        }
        if (!logPolicy.responseBodySave()) {
            record.setResponse(null);
            if (details != null) {
                details.remove("response");
            }
        }
        if (!logPolicy.errorStackSave()) {
            record.setInternalMessage(null);
            if (details != null) {
                details.remove("error.internalMessage");
            }
        }
    }

    private void insertDetail(Long logIdx, String detailKey, String detailValue, String auditUser) {
        if (logIdx == null) {
            return;
        }
        logMapper.insertTransactionLogDetail(
                logIdx,
                SensitiveDataMasker.truncate(detailKey, 100),
                SensitiveDataMasker.mask(detailValue),
                auditUser);
    }
}

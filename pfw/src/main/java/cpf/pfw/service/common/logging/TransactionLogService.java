package cpf.pfw.service.common.logging;

import cpf.pfw.common.logging.SensitiveDataMasker;
import cpf.pfw.common.logging.TransactionLogRecord;
import cpf.pfw.mapper.common.logging.TransactionLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * CPF 기능 설명입니다.
 *
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 */
@Service
@RequiredArgsConstructor
public class TransactionLogService {

    private final TransactionLogMapper logMapper;

    @Transactional(transactionManager = "pfwTransactionManager")
    public void saveTransactionLog(TransactionLogRecord record, Map<String, String> details) {
        if (record == null) {
            return;
        }

        // CPF 기능 설명입니다.
        logMapper.insertTransactionLog(record);

        if (details != null) {
            // CPF 기능 설명입니다.
            details.forEach((key, value) -> insertDetail(record.getLogIdx(), key, value, record.getExecUser()));
        }

        if (record.getErrorMessage() != null) {
            // CPF 기능 설명입니다.
            insertDetail(record.getLogIdx(), "errorMessage", record.getErrorMessage(), record.getExecUser());
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


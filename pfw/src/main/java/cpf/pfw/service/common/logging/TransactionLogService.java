package cpf.pfw.service.common.logging;

import cpf.pfw.common.logging.SensitiveDataMasker;
import cpf.pfw.common.logging.TransactionLogRecord;
import cpf.pfw.mapper.common.logging.TransactionLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * PFW 거래 요약 로그와 상세 로그를 저장합니다.
 *
 * <p>요약 정보는 {@code pfw_transaction_log}, 요청/응답/오류 상세는
 * {@code pfw_transaction_log_detail}에 저장합니다. 모든 상세 값은 저장 전에 마스킹과 길이 제한을 적용합니다.</p>
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

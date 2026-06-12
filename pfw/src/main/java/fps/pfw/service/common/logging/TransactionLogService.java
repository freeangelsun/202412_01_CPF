package fps.pfw.service.common.logging;

import fps.pfw.common.logging.SensitiveDataMasker;
import fps.pfw.common.logging.TransactionLogRecord;
import fps.pfw.mapper.common.logging.TransactionLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 프레임워크 거래 로그를 TRAN_LOG와 TRAN_LOG_DTL 테이블에 저장합니다.
 *
 * <p>저장 순서는 TRAN_LOG 요약 로그를 먼저 INSERT해 LOG_IDX를 확보한 뒤,
 * 같은 LOG_IDX로 TRAN_LOG_DTL 상세 로그를 여러 건 INSERT하는 방식입니다.
 * 이렇게 하면 목록 화면은 TRAN_LOG만 빠르게 조회하고,
 * 사용자가 한 건을 선택했을 때만 TRAN_LOG_DTL을 조회할 수 있습니다.</p>
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

        // 요약 로그를 먼저 저장해야 DB가 생성한 LOG_IDX를 상세 로그의 외래키로 사용할 수 있습니다.
        logMapper.insertTransactionLog(record);

        if (details != null) {
            // 상세 로그는 화면 상세 탭에서 사용할 키-값 목록으로 저장합니다.
            details.forEach((key, value) -> insertDetail(record.getLogIdx(), key, value, record.getExecUser()));
        }

        if (record.getErrorMessage() != null) {
            // 오류 메시지는 요약 컬럼에도 남기고, 상세 테이블에도 별도 키로 남겨 화면에서 찾기 쉽게 합니다.
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

package cpf.pfw.service.common.logging;

import cpf.pfw.common.logging.SensitiveDataMasker;
import cpf.pfw.common.logging.TransactionLogRecord;
import cpf.pfw.mapper.common.logging.TransactionLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * ?꾨젅?꾩썙??嫄곕옒 濡쒓렇瑜?pfw_transaction_log? pfw_transaction_log_detail ?뚯씠釉붿뿉 ??ν빀?덈떎.
 *
 * <p>????쒖꽌??pfw_transaction_log ?붿빟 濡쒓렇瑜?쇱? INSERT??LOG_IDX瑜??뺣낫????
 * 媛숈? LOG_IDX濡?pfw_transaction_log_detail ?곸꽭 濡쒓렇瑜??щ윭 嫄?INSERT?섎뒗 諛⑹떇?낅땲??
 * ?대젃寃??섎㈃ ⑸줉 ?붾㈃? pfw_transaction_log留?鍮좊Ⅴ寃?議고쉶?섍퀬,
 * ?ъ슜?먭? ??嫄댁쓣 ?좏깮?덉쓣 ?뚮쭔 pfw_transaction_log_detail??議고쉶?????덉뒿?덈떎.</p>
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

        // ?붿빟 濡쒓렇瑜?쇱? ??ν빐??DB媛 ?앹꽦??LOG_IDX瑜??곸꽭 濡쒓렇???몃옒?ㅻ줈 ?ъ슜?????덉뒿?덈떎.
        logMapper.insertTransactionLog(record);

        if (details != null) {
            // ?곸꽭 濡쒓렇???붾㈃ ?곸꽭 ??뿉???ъ슜????媛?⑸줉?쇰줈 ??ν빀?덈떎.
            details.forEach((key, value) -> insertDetail(record.getLogIdx(), key, value, record.getExecUser()));
        }

        if (record.getErrorMessage() != null) {
            // ?ㅻ쪟 硫붿떆吏???붿빟 而щ읆?먮룄 ?④린怨? ?곸꽭 ?뚯씠釉붿뿉??蹂꾨룄 ?ㅻ줈 ?④꺼 ?붾㈃?먯꽌 李얘린 ?쎄쾶 ?⑸땲??
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


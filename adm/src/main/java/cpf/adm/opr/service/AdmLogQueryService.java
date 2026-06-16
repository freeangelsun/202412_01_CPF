package cpf.adm.opr.service;

import cpf.cmn.utils.TextUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * PFW 嫄곕옒 濡쒓렇 紐⑸줉/?곸꽭 議고쉶 ?쒕퉬?ㅼ엯?덈떎.
 *
 * <p>紐⑸줉? {@code TRAN_LOG} ?붿빟 ?뚯씠釉붾쭔 議고쉶?섍퀬, ?곸꽭???좏깮??{@code LOG_IDX}濡? * {@code TRAN_LOG_DTL}??異붽? 議고쉶?⑸땲?? ?붾㈃ ?깅뒫???꾪빐 ??蹂몃Ц 寃?됱? 蹂꾨룄 寃????μ냼瑜? * 遺숈씠湲??꾧퉴吏 ?곸꽭 議고쉶?먯꽌留??뺤씤?섎뒗 湲곗???沅뚯옣?⑸땲??</p>
 */
@Service
public class AdmLogQueryService {
    private final JdbcTemplate pfwJdbcTemplate;

    public AdmLogQueryService(@Qualifier("pfwJdbcTemplate") JdbcTemplate pfwJdbcTemplate) {
        this.pfwJdbcTemplate = pfwJdbcTemplate;
    }

    /**
     * 嫄곕옒 濡쒓렇 紐⑸줉??議고쉶?⑸땲??
     *
     * @param transactionId             湲濡쒕쾶 嫄곕옒ID
     * @param businessTransactionId     ?낅Т 嫄곕옒ID
     * @param memberNo                  ?뚯썝踰덊샇
     * @param customerNo                怨좉컼踰덊샇
     * @param limit                     議고쉶 嫄댁닔
     * @return 嫄곕옒 濡쒓렇 紐⑸줉
     */
    public List<Map<String, Object>> findLogs(
            String transactionId,
            String businessTransactionId,
            String memberNo,
            String customerNo,
            int limit) {
        int resolvedLimit = limit <= 0 ? 50 : Math.min(limit, 500);
        String sql = """
                SELECT
                    LOG_IDX,
                    TRANSACTION_ID,
                    TRACE_ID,
                    MODULE_ID,
                    WAS_ID,
                    BUSINESS_TRANSACTION_ID,
                    BUSINESS_TRANSACTION_NAME,
                    LOG_TYPE,
                    REQUEST_TYPE,
                    ORIGINAL_CHANNEL_CODE,
                    CHANNEL_CODE,
                    MEMBER_NO,
                    CUSTOMER_NO,
                    WORKFLOW_STATUS,
                    WORKFLOW_FAILURE_POLICY,
                    COMPENSATION_YN,
                    COMPENSATION_STATUS,
                    RESPONSE_CODE,
                    ERROR_CODE,
                    START_TIME,
                    END_TIME,
                    DURATION_MS
                FROM TRAN_LOG
                WHERE (? IS NULL OR TRANSACTION_ID LIKE CONCAT('%', ?, '%'))
                  AND (? IS NULL OR BUSINESS_TRANSACTION_ID LIKE CONCAT('%', ?, '%'))
                  AND (? IS NULL OR MEMBER_NO = ?)
                  AND (? IS NULL OR CUSTOMER_NO = ?)
                ORDER BY LOG_IDX DESC
                LIMIT ?
                """;
        String txId = blankToNull(transactionId);
        String bizTxId = blankToNull(businessTransactionId);
        String resolvedMemberNo = blankToNull(memberNo);
        String resolvedCustomerNo = blankToNull(customerNo);
        return pfwJdbcTemplate.queryForList(
                sql,
                txId, txId,
                bizTxId, bizTxId,
                resolvedMemberNo, resolvedMemberNo,
                resolvedCustomerNo, resolvedCustomerNo,
                resolvedLimit);
    }

    /**
     * 嫄곕옒 濡쒓렇 ?곸꽭瑜?議고쉶?⑸땲??
     *
     * @param logIdx 嫄곕옒 濡쒓렇 ?쒕쾲
     * @return ?붿빟 濡쒓렇? ?곸꽭 ??媛?紐⑸줉
     */
    public Map<String, Object> getLogDetail(Long logIdx) {
        Map<String, Object> response = new LinkedHashMap<>();
        Map<String, Object> summary = pfwJdbcTemplate.queryForMap(
                "SELECT * FROM TRAN_LOG WHERE LOG_IDX = ?",
                logIdx);
        List<Map<String, Object>> details = pfwJdbcTemplate.queryForList(
                "SELECT DETAIL_KEY, DETAIL_VALUE, CREATED_AT FROM TRAN_LOG_DTL WHERE LOG_IDX = ? ORDER BY DETAIL_KEY",
                logIdx);
        response.put("summary", summary);
        response.put("details", details);
        return response;
    }

    private String blankToNull(String value) {
        return TextUtils.hasText(value) ? value.trim() : null;
    }
}


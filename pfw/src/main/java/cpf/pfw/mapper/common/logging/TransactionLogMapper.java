package cpf.pfw.mapper.common.logging;

import cpf.pfw.common.logging.TransactionLogRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * ?꾨젅?꾩썙??嫄곕옒 濡쒓렇 愿由щ? ?꾪븳 MyBatis 留ㅽ띁?낅땲??
 */
@Mapper
public interface TransactionLogMapper {

    void insertTransactionLog(TransactionLogRecord record);

    /**
     * TRAN_LOG_DTL ?뚯씠釉붿뿉 ?곸꽭 濡쒓렇瑜??쎌엯?⑸땲??
     *
     * @param logIdx      嫄곕옒 濡쒓렇 ?몃뜳??     * @param detailKey   ?곸꽭 ??     * @param detailValue ?곸꽭 媛?     * @param auditUser   ?곸꽭 濡쒓렇 ?깅줉?먯? ?섏젙?먮줈 ?④만 ?ъ슜??ID
     */
    void insertTransactionLogDetail(@Param("logIdx") Long logIdx,
                                    @Param("detailKey") String detailKey,
                                    @Param("detailValue") String detailValue,
                                    @Param("auditUser") String auditUser);
}


package fps.pfw.mapper.common.logging;

import fps.pfw.common.logging.TransactionLogRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 프레임워크 거래 로그 관리를 위한 MyBatis 매퍼입니다.
 */
@Mapper
public interface TransactionLogMapper {

    void insertTransactionLog(TransactionLogRecord record);

    /**
     * TRAN_LOG_DTL 테이블에 상세 로그를 삽입합니다.
     *
     * @param logIdx      거래 로그 인덱스
     * @param detailKey   상세 키
     * @param detailValue 상세 값
     * @param auditUser   상세 로그 등록자와 수정자로 남길 사용자 ID
     */
    void insertTransactionLogDetail(@Param("logIdx") Long logIdx,
                                    @Param("detailKey") String detailKey,
                                    @Param("detailValue") String detailValue,
                                    @Param("auditUser") String auditUser);
}

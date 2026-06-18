package cpf.pfw.mapper.common.logging;

import cpf.pfw.common.logging.TransactionLogRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * CPF 기능 설명입니다.
 */
@Mapper
public interface TransactionLogMapper {

    void insertTransactionLog(TransactionLogRecord record);

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     */
    void insertTransactionLogDetail(@Param("logIdx") Long logIdx,
                                    @Param("detailKey") String detailKey,
                                    @Param("detailValue") String detailValue,
                                    @Param("auditUser") String auditUser);
}


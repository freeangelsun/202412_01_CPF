package com.cpf.core.mapper.common.logging;

import com.cpf.core.common.logging.TransactionLogRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** CPF 거래 원장과 상세 로그를 저장하는 MyBatis 매퍼입니다. */
@Mapper
public interface TransactionLogMapper {

    boolean existsRecoveryEvent(@Param("recoveryEventId") String recoveryEventId);

    void insertTransactionLog(TransactionLogRecord record);

    /** 거래 원장에 연결되는 key/value 상세 항목을 저장합니다. */
    void insertTransactionLogDetail(@Param("logIdx") Long logIdx,
                                    @Param("detailKey") String detailKey,
                                    @Param("detailValue") String detailValue,
                                    @Param("auditUser") String auditUser);
}

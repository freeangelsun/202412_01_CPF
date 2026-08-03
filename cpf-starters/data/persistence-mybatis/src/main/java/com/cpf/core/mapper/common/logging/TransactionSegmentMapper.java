package com.cpf.core.mapper.common.logging;

import com.cpf.core.common.logging.segment.TransactionSegmentRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 복합 거래 구간 로그를 저장하는 MyBatis mapper입니다.
 */
@Mapper
public interface TransactionSegmentMapper {
    void insertSegment(TransactionSegmentRecord record);

    int updateSegmentEnd(TransactionSegmentRecord record);

    int countByTransactionSegmentId(String transactionSegmentId);
}

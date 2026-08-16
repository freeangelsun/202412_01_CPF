package com.cpf.data.persistence.mybatis.mapper.logging;

import com.cpf.platform.operations.observability.spi.logging.segment.TransactionSegmentRecord;
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

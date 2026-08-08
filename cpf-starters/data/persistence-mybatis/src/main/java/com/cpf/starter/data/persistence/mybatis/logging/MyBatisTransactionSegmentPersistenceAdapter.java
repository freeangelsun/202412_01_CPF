package com.cpf.starter.data.persistence.mybatis.logging;

import com.cpf.core.common.logging.segment.TransactionSegmentRecord;
import com.cpf.core.common.logging.spi.CpfTransactionSegmentPersistencePort;
import com.cpf.starter.data.persistence.mybatis.mapper.logging.TransactionSegmentMapper;
import java.util.Objects;

/** MyBatis-owned implementation of the core transaction-segment persistence SPI. */
public final class MyBatisTransactionSegmentPersistenceAdapter implements CpfTransactionSegmentPersistencePort {
    private final TransactionSegmentMapper mapper;

    public MyBatisTransactionSegmentPersistenceAdapter(TransactionSegmentMapper mapper) {
        this.mapper = Objects.requireNonNull(mapper, "mapper");
    }

    @Override public void insertSegment(TransactionSegmentRecord record) { mapper.insertSegment(record); }
    @Override public int updateSegmentEnd(TransactionSegmentRecord record) { return mapper.updateSegmentEnd(record); }
    @Override public int countByTransactionSegmentId(String transactionSegmentId) { return mapper.countByTransactionSegmentId(transactionSegmentId); }
}

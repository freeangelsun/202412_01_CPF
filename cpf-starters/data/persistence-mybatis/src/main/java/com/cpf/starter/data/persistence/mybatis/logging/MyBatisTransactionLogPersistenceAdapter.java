package com.cpf.starter.data.persistence.mybatis.logging;

import com.cpf.core.common.logging.TransactionLogRecord;
import com.cpf.core.common.logging.spi.CpfTransactionLogPersistencePort;
import com.cpf.starter.data.persistence.mybatis.mapper.logging.TransactionLogMapper;
import java.util.Objects;

/** MyBatis-owned implementation of the core transaction-log persistence SPI. */
public final class MyBatisTransactionLogPersistenceAdapter implements CpfTransactionLogPersistencePort {
    private final TransactionLogMapper mapper;

    public MyBatisTransactionLogPersistenceAdapter(TransactionLogMapper mapper) {
        this.mapper = Objects.requireNonNull(mapper, "mapper");
    }

    @Override public boolean existsRecoveryEvent(String recoveryEventId) { return mapper.existsRecoveryEvent(recoveryEventId); }
    @Override public void insertTransactionLog(TransactionLogRecord record) { mapper.insertTransactionLog(record); }
    @Override public void insertTransactionLogDetail(Long logIdx, String detailKey, String detailValue, String auditUser) {
        mapper.insertTransactionLogDetail(logIdx, detailKey, detailValue, auditUser);
    }
}

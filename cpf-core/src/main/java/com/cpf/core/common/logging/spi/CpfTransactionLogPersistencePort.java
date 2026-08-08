package com.cpf.core.common.logging.spi;

import com.cpf.core.common.logging.TransactionLogRecord;

/** Topology-independent persistence contract for the CPF transaction log projection. */
public interface CpfTransactionLogPersistencePort {
    boolean existsRecoveryEvent(String recoveryEventId);
    void insertTransactionLog(TransactionLogRecord record);
    void insertTransactionLogDetail(Long logIdx, String detailKey, String detailValue, String auditUser);
}

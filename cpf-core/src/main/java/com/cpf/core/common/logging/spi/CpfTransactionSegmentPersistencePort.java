package com.cpf.core.common.logging.spi;

import com.cpf.core.common.logging.segment.TransactionSegmentRecord;

/** Topology-independent persistence contract for transaction segment lifecycle records. */
public interface CpfTransactionSegmentPersistencePort {
    void insertSegment(TransactionSegmentRecord record);
    int updateSegmentEnd(TransactionSegmentRecord record);
    int countByTransactionSegmentId(String transactionSegmentId);
}

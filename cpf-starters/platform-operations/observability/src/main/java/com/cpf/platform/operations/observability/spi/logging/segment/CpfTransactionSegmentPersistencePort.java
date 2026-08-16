package com.cpf.platform.operations.observability.spi.logging.segment;

import com.cpf.platform.operations.observability.spi.logging.segment.TransactionSegmentRecord;

/** Topology-independent persistence contract for transaction segment lifecycle records. */
/** CpfTransactionSegmentPersistencePort 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfTransactionSegmentPersistencePort {
    void insertSegment(TransactionSegmentRecord record);
    int updateSegmentEnd(TransactionSegmentRecord record);
    int countByTransactionSegmentId(String transactionSegmentId);
}

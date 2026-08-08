package com.cpf.starter.data.transaction.jta;

import com.cpf.core.api.transaction.CpfTransactionOutcome;
import com.cpf.core.api.transaction.CpfXaRecoveryRecord;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** JTA crash-recovery 결과를 ADM/reconcile과 연결하기 위한 durable recovery store 경계입니다. */
public interface CpfXaRecoveryStore {
    void started(String transactionId, String attemptId, Instant startedAt);
    void completed(String transactionId, CpfTransactionOutcome outcome, String detail);
    List<CpfXaRecoveryRecord> inDoubt();
    Optional<CpfXaRecoveryRecord> find(String transactionId);
}

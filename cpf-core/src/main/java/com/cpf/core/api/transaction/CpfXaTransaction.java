package com.cpf.core.api.transaction;

/** XA transaction의 enlist/2PC commit/rollback lifecycle 계약입니다. prepare fault injection은 provider recovery harness가 소유합니다. */
public interface CpfXaTransaction {
    String transactionId();
    void enlist(CpfXaResourceHandle resource);
    CpfTransactionOutcome commit();
    CpfTransactionOutcome rollback();
}

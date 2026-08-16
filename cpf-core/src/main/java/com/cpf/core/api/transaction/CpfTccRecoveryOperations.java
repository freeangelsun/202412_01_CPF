package com.cpf.core.api.transaction;

/** Timeout/UNKNOWN/Hanging branch를 운영적으로 복구하는 계약입니다. */
public interface CpfTccRecoveryOperations {
    CpfTccResult reconcile(String transactionId, String branchId);
    CpfTccResult requestManualReview(String transactionId, String branchId, String reason);
}

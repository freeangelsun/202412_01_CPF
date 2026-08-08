package com.cpf.core.api.transaction;

import java.time.Duration;
import java.util.List;

/** Standalone/Managed JTA Adapter가 구현하는 provider-neutral XA 관리 계약입니다. */
public interface CpfXaTransactionManager {
    CpfXaTransaction begin(String transactionId, Duration timeout);
    List<CpfXaRecoveryRecord> scanRecovery();
    CpfXaRecoveryRecord reconcile(String transactionId);
}

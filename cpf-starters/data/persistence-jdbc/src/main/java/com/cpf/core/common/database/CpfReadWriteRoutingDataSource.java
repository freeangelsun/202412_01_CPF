package com.cpf.core.common.database;

import com.cpf.core.api.database.CpfDataAccessIntent;
import com.cpf.core.api.logging.CpfTransactionContext;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Primary/Replica routing datasource입니다. write transaction과 read-after-write는 항상 Primary를 선택합니다.
 * Replica 상태/lag를 확인할 수 없으면 거래를 실패시키지 않고 Primary로 failover합니다.
 */
public class CpfReadWriteRoutingDataSource extends AbstractRoutingDataSource {
    private final CpfReadRoutingRuntimePolicy policy;
    private final CpfReplicaHealthMonitor healthMonitor;

    public CpfReadWriteRoutingDataSource(CpfReadRoutingRuntimePolicy policy, CpfReplicaHealthMonitor healthMonitor) {
        this.policy = policy;
        this.healthMonitor = healthMonitor;
    }

    @Override
    protected Object determineCurrentLookupKey() {
        CpfReadRoutingRuntimePolicy.Snapshot current = policy.current();
        String transactionId = currentTransactionId();
        CpfDataAccessIntent explicit = CpfDataAccessContext.current();
        boolean writeTransaction = TransactionSynchronizationManager.isActualTransactionActive()
                && !TransactionSynchronizationManager.isCurrentTransactionReadOnly();
        if (!current.enabled() || explicit == CpfDataAccessIntent.WRITE || writeTransaction) {
            policy.markWrite(transactionId);
            return "WRITE";
        }
        boolean readCandidate = explicit == CpfDataAccessIntent.READ
                || TransactionSynchronizationManager.isCurrentTransactionReadOnly();
        if (!readCandidate || policy.readAfterWriteActive(transactionId)) return "WRITE";
        CpfReplicaHealthMonitor.Status status = healthMonitor.current();
        if (!status.healthy() || status.lagMillis() > current.maxReplicaLagMillis()) return "WRITE";
        return "READ";
    }

    private String currentTransactionId() {
        try {
            return CpfTransactionContext.transactionId();
        } catch (RuntimeException ignored) {
            return null;
        }
    }
}

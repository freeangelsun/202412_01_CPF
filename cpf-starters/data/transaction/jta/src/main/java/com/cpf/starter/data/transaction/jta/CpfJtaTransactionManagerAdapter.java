package com.cpf.starter.data.transaction.jta;

import com.cpf.core.api.transaction.*;
import jakarta.transaction.*;
import javax.transaction.xa.XAResource;
import java.time.Instant;
import java.util.*;

/** WAS-managed 또는 standalone Jakarta TransactionManager를 CPF XA 계약에 연결합니다. */
public final class CpfJtaTransactionManagerAdapter implements CpfXaTransactionManager {
    private final TransactionManager transactionManager;
    private final CpfXaRecoveryStore recoveryStore;

    public CpfJtaTransactionManagerAdapter(TransactionManager transactionManager, CpfXaRecoveryStore recoveryStore) {
        this.transactionManager = Objects.requireNonNull(transactionManager, "transactionManager");
        this.recoveryStore = Objects.requireNonNull(recoveryStore, "recoveryStore");
    }

    @Override
    public CpfXaTransaction begin(String transactionId, java.time.Duration timeout) {
        if (transactionId == null || transactionId.isBlank()) throw new IllegalArgumentException("transactionId required");
        if (timeout == null || timeout.isZero() || timeout.isNegative()) throw new IllegalArgumentException("positive timeout required");
        try {
            transactionManager.setTransactionTimeout(Math.toIntExact(timeout.toSeconds()));
            transactionManager.begin();
            Transaction tx = transactionManager.getTransaction();
            if (tx == null) throw new IllegalStateException("JTA transaction was not created");
            String attemptId = java.util.UUID.randomUUID().toString();
            recoveryStore.started(transactionId, attemptId, Instant.now());
            return new Handle(tx, transactionId, recoveryStore);
        } catch (NotSupportedException | SystemException ex) {
            throw new IllegalStateException("Unable to begin JTA transaction", ex);
        }
    }

    @Override public List<CpfXaRecoveryRecord> scanRecovery() { return recoveryStore.inDoubt(); }

    @Override
    public CpfXaRecoveryRecord reconcile(String transactionId) {
        return recoveryStore.find(transactionId).orElseThrow(() -> new IllegalArgumentException("Unknown transactionId"));
    }

    private static final class Handle implements CpfXaTransaction {
        private final Transaction tx; private final String transactionId; private final CpfXaRecoveryStore store;
        private boolean completed;
        private Handle(Transaction tx, String transactionId, CpfXaRecoveryStore store) { this.tx=tx; this.transactionId=transactionId; this.store=store; }
        @Override public String transactionId() { return transactionId; }
        @Override public void enlist(CpfXaResourceHandle resource) {
            ensureOpen();
            try { if (!tx.enlistResource(resource.resource())) throw new IllegalStateException("XAResource enlist rejected: " + resource.resourceId()); }
            catch (RollbackException | SystemException ex) { throw new IllegalStateException("XAResource enlist failed: " + resource.resourceId(), ex); }
        }
        @Override public CpfTransactionOutcome commit() {
            ensureOpen(); completed=true;
            try { tx.commit(); store.completed(transactionId, CpfTransactionOutcome.COMMITTED, null); return CpfTransactionOutcome.COMMITTED; }
            catch (RollbackException ex) { store.completed(transactionId, CpfTransactionOutcome.ROLLED_BACK, ex.getClass().getSimpleName()); return CpfTransactionOutcome.ROLLED_BACK; }
            catch (HeuristicMixedException | HeuristicRollbackException ex) { store.completed(transactionId, CpfTransactionOutcome.HEURISTIC, ex.getClass().getSimpleName()); return CpfTransactionOutcome.HEURISTIC; }
            catch (SystemException ex) { store.completed(transactionId, CpfTransactionOutcome.IN_DOUBT, ex.getClass().getSimpleName()); return CpfTransactionOutcome.IN_DOUBT; }
            catch (SecurityException ex) { store.completed(transactionId, CpfTransactionOutcome.FAILED, ex.getClass().getSimpleName()); throw ex; }
        }
        @Override public CpfTransactionOutcome rollback() {
            ensureOpen(); completed=true;
            try { tx.rollback(); store.completed(transactionId, CpfTransactionOutcome.ROLLED_BACK, null); return CpfTransactionOutcome.ROLLED_BACK; }
            catch (SystemException ex) { store.completed(transactionId, CpfTransactionOutcome.IN_DOUBT, ex.getClass().getSimpleName()); return CpfTransactionOutcome.IN_DOUBT; }
        }
        private void ensureOpen() { if (completed) throw new IllegalStateException("XA transaction already completed"); }
    }
}

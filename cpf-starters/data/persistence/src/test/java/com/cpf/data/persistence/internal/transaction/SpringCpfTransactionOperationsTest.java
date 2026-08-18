package com.cpf.data.persistence.internal.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cpf.data.persistence.api.transaction.CpfTransactionOperations.CompletionStatus;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class SpringCpfTransactionOperationsTest {
    private final SpringCpfTransactionOperations operations = new SpringCpfTransactionOperations();

    @AfterEach
    void cleanup() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        TransactionSynchronizationManager.setActualTransactionActive(false);
    }

    @Test
    void hooksFailFastWithoutActiveTransaction() {
        assertThrows(IllegalStateException.class, () -> operations.afterCommit(() -> {}));
        assertThrows(IllegalStateException.class, () -> operations.afterRollback(() -> {}));
        assertThrows(IllegalStateException.class, () -> operations.afterCompletion(status -> {}));
    }

    @Test
    void commitRunsOnlyCommitAndCompletionCallbacks() {
        begin();
        List<String> events = new ArrayList<>();
        operations.afterCommit(20, () -> events.add("commit"));
        operations.afterRollback(10, () -> events.add("rollback"));
        operations.afterCompletion(30, status -> events.add("completion:" + status));

        callbacks().forEach(TransactionSynchronization::afterCommit);
        callbacks().forEach(callback -> callback.afterCompletion(TransactionSynchronization.STATUS_COMMITTED));

        assertEquals(List.of("commit", "completion:" + CompletionStatus.COMMITTED), events);
    }

    @Test
    void rollbackRunsOnlyRollbackAndCompletionCallbacks() {
        begin();
        List<String> events = new ArrayList<>();
        operations.afterCommit(() -> events.add("commit"));
        operations.afterRollback(() -> events.add("rollback"));
        operations.afterCompletion(status -> events.add("completion:" + status));

        callbacks().forEach(callback -> callback.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));

        assertEquals(List.of("rollback", "completion:" + CompletionStatus.ROLLED_BACK), events);
    }

    private static void begin() {
        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.initSynchronization();
    }

    private static List<TransactionSynchronization> callbacks() {
        return TransactionSynchronizationManager.getSynchronizations();
    }
}

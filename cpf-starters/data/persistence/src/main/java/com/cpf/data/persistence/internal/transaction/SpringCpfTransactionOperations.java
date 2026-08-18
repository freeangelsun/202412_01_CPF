package com.cpf.data.persistence.internal.transaction;

import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.data.persistence.api.transaction.CpfTransactionOperations;
import java.util.Objects;
import java.util.function.Consumer;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** Spring transaction synchronization을 CPF Public Transaction API에 연결하는 내부 구현입니다. */
public final class SpringCpfTransactionOperations implements CpfTransactionOperations {

    @Override
    public boolean isActive() {
        return TransactionSynchronizationManager.isActualTransactionActive();
    }

    @Override
    public boolean isRollbackOnly() {
        requireActiveTransaction();
        return TransactionAspectSupport.currentTransactionStatus().isRollbackOnly();
    }

    @Override
    public void setRollbackOnly() {
        requireActiveTransaction();
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    }

    @Override
    public void afterCommit(Runnable callback) {
        afterCommit(0, callback);
    }

    @Override
    public void afterCommit(int order, Runnable callback) {
        Objects.requireNonNull(callback, "callback");
        requireSynchronization();
        CpfContextSnapshot context = CpfContexts.snapshot();
        TransactionSynchronizationManager.registerSynchronization(new OrderedSynchronization(order) {
            @Override
            public void afterCommit() {
                runWithContext(context, callback);
            }
        });
    }

    @Override
    public void afterRollback(Runnable callback) {
        afterRollback(0, callback);
    }

    @Override
    public void afterRollback(int order, Runnable callback) {
        Objects.requireNonNull(callback, "callback");
        requireSynchronization();
        CpfContextSnapshot context = CpfContexts.snapshot();
        TransactionSynchronizationManager.registerSynchronization(new OrderedSynchronization(order) {
            @Override
            public void afterCompletion(int status) {
                if (status == TransactionSynchronization.STATUS_ROLLED_BACK) {
                    runWithContext(context, callback);
                }
            }
        });
    }

    @Override
    public void afterCompletion(Consumer<CompletionStatus> callback) {
        afterCompletion(0, callback);
    }

    @Override
    public void afterCompletion(int order, Consumer<CompletionStatus> callback) {
        Objects.requireNonNull(callback, "callback");
        requireSynchronization();
        CpfContextSnapshot context = CpfContexts.snapshot();
        TransactionSynchronizationManager.registerSynchronization(new OrderedSynchronization(order) {
            @Override
            public void afterCompletion(int status) {
                runWithContext(context, () -> callback.accept(map(status)));
            }
        });
    }

    private static CompletionStatus map(int status) {
        return switch (status) {
            case TransactionSynchronization.STATUS_COMMITTED -> CompletionStatus.COMMITTED;
            case TransactionSynchronization.STATUS_ROLLED_BACK -> CompletionStatus.ROLLED_BACK;
            default -> CompletionStatus.UNKNOWN;
        };
    }

    private static void requireSynchronization() {
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || !TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("CPF transaction lifecycle callback requires an active synchronized transaction");
        }
    }

    private static void requireActiveTransaction() {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("CPF transaction operation requires an active transaction");
        }
    }

    private static void runWithContext(CpfContextSnapshot context, Runnable callback) {
        if (context == null) {
            callback.run();
        } else {
            CpfContexts.run(context, callback);
        }
    }

    private abstract static class OrderedSynchronization implements TransactionSynchronization {
        private final int order;

        private OrderedSynchronization(int order) {
            this.order = order;
        }

        @Override
        public int getOrder() {
            return order;
        }
    }
}

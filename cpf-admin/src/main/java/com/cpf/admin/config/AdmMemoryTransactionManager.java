package com.cpf.admin.config;

import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

/**
 * 명시적 ADM MEMORY 데모 모드에서만 사용하는 무자원 TransactionManager입니다.
 *
 * <p>제품 DATABASE 모드에서는 절대 사용하지 않습니다.</p>
 */
final class AdmMemoryTransactionManager extends AbstractPlatformTransactionManager {
    @Override
    protected Object doGetTransaction() {
        return new Object();
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) {
        // MEMORY mode has no external transactional resource.
    }

    @Override
    protected void doCommit(DefaultTransactionStatus status) {
        // ConcurrentMap operations are synchronized at aggregate boundaries.
    }

    @Override
    protected void doRollback(DefaultTransactionStatus status) {
        // No external resource exists.
    }
}

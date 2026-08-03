package com.cpf.starter.integration.resilience.internal;

import java.util.Objects;
import java.util.function.Supplier;
import org.springframework.transaction.support.TransactionTemplate;

/** Spring transaction adapter kept inside the resilience implementation boundary. */
final class CpfSpringResilienceTransactionRunner implements CpfResilienceTransactionRunner {
    private final TransactionTemplate transaction;

    CpfSpringResilienceTransactionRunner(TransactionTemplate transaction) {
        this.transaction = Objects.requireNonNull(transaction, "transaction");
    }

    @Override public <T> T required(Supplier<T> work) {
        return transaction.execute(status -> work.get());
    }
}

package com.cpf.starter.platform.operations.feature.flag.openfeature.internal;

import java.util.Objects;
import java.util.function.Supplier;
import org.springframework.transaction.support.TransactionTemplate;

/** Spring transaction adapter kept inside the feature-flag implementation boundary. */
final class CpfSpringFeatureFlagTransactionRunner implements CpfFeatureFlagTransactionRunner {
    private final TransactionTemplate transaction;

    CpfSpringFeatureFlagTransactionRunner(TransactionTemplate transaction) {
        this.transaction = Objects.requireNonNull(transaction, "transaction");
    }

    @Override public <T> T required(Supplier<T> work) {
        return transaction.execute(status -> work.get());
    }
}

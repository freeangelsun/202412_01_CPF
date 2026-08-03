package com.cpf.starter.integration.resilience.internal;

import java.util.function.Supplier;

/** Internal transaction boundary for atomic policy state and audit changes. */
interface CpfResilienceTransactionRunner {
    <T> T required(Supplier<T> work);

    default void required(Runnable work) {
        required(() -> {
            work.run();
            return null;
        });
    }

    static CpfResilienceTransactionRunner direct() {
        return new CpfResilienceTransactionRunner() {
            @Override public <T> T required(Supplier<T> work) {
                return work.get();
            }
        };
    }
}

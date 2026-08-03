package com.cpf.starter.platform.operations.feature.flag.openfeature.internal;

import java.util.function.Supplier;

/** Internal transaction boundary for atomic feature-flag state and audit changes. */
interface CpfFeatureFlagTransactionRunner {
    <T> T required(Supplier<T> work);

    default void required(Runnable work) {
        required(() -> {
            work.run();
            return null;
        });
    }

    static CpfFeatureFlagTransactionRunner direct() {
        return new CpfFeatureFlagTransactionRunner() {
            @Override public <T> T required(Supplier<T> work) {
                return work.get();
            }
        };
    }
}

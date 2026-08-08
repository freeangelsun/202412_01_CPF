package com.cpf.core.api.transaction;

import java.time.Duration;
import java.util.Objects;

/**
 * Runtime 구현과 무관한 CPF 거래 정의입니다.
 * LOCAL을 기본값으로 하며 XA/JTA Provider가 없어도 사용할 수 있어야 합니다.
 */
public record CpfTransactionDefinition(
        CpfTransactionStrategy strategy,
        CpfTransactionPropagation propagation,
        CpfTransactionIsolation isolation,
        Duration timeout,
        boolean readOnly) {

    public CpfTransactionDefinition {
        Objects.requireNonNull(strategy, "strategy");
        Objects.requireNonNull(propagation, "propagation");
        Objects.requireNonNull(isolation, "isolation");
        Objects.requireNonNull(timeout, "timeout");
        if (timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException("timeout must be positive");
        }
    }

    /** 가장 단순하고 안전한 단일 DB 기본 정의입니다. */
    public static CpfTransactionDefinition localDefault() {
        return new CpfTransactionDefinition(
                CpfTransactionStrategy.LOCAL,
                CpfTransactionPropagation.REQUIRED,
                CpfTransactionIsolation.DEFAULT,
                Duration.ofSeconds(30),
                false);
    }
}

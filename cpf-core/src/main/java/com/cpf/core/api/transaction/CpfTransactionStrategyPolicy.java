package com.cpf.core.api.transaction;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * 선택한 Capability만 로드되도록 전략 조합을 검증합니다.
 * LOCAL과 XA_JTA는 같은 물리 경계에서 동시에 primary coordinator가 될 수 없습니다.
 */
public final class CpfTransactionStrategyPolicy {
    private final Set<CpfTransactionStrategy> strategies;

    private CpfTransactionStrategyPolicy(Set<CpfTransactionStrategy> strategies) {
        this.strategies = Set.copyOf(strategies);
    }

    public static CpfTransactionStrategyPolicy local() {
        return of(Set.of(CpfTransactionStrategy.LOCAL));
    }

    public static CpfTransactionStrategyPolicy of(Collection<CpfTransactionStrategy> requested) {
        Objects.requireNonNull(requested, "requested");
        EnumSet<CpfTransactionStrategy> values = requested.isEmpty()
                ? EnumSet.of(CpfTransactionStrategy.LOCAL)
                : EnumSet.copyOf(requested);
        if (values.contains(CpfTransactionStrategy.LOCAL) && values.contains(CpfTransactionStrategy.XA_JTA)) {
            throw new IllegalArgumentException("LOCAL and XA_JTA cannot both be the primary coordinator");
        }
        return new CpfTransactionStrategyPolicy(values);
    }

    public Set<CpfTransactionStrategy> strategies() { return strategies; }
    public boolean enabled(CpfTransactionStrategy strategy) { return strategies.contains(strategy); }
}

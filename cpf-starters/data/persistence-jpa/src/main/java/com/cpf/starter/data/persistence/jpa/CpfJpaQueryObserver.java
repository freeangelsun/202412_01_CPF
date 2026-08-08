package com.cpf.starter.data.persistence.jpa;

/** Micrometer/Log/Trace 등 운영 관측 구현이 연결되는 작은 확장점입니다. */
@FunctionalInterface
public interface CpfJpaQueryObserver {
    void observe(CpfJpaQueryObservation observation);

    static CpfJpaQueryObserver noop() { return observation -> { }; }
}

package com.cpf.core.api.broker;

/** Broker adapter가 실제 consumer container에 적용할 동적 제어 snapshot입니다. */
public record CpfBrokerConsumerControl(boolean paused, int concurrency, int prefetch) {
    public CpfBrokerConsumerControl {
        if (concurrency < 1 || concurrency > 1024) {
            throw new IllegalArgumentException("concurrency는 1~1024여야 합니다.");
        }
        if (prefetch < 1 || prefetch > 100000) {
            throw new IllegalArgumentException("prefetch는 1~100000이어야 합니다.");
        }
    }
}

package com.cpf.testkit.fixture;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 테스트 JVM을 종료하지 않고 worker/process lifecycle의 kill/restart 상태를 결정적으로 재현하는 fixture입니다.
 * 실제 OS process kill 검증에는 {@link CpfProcessKillHarness}를 사용합니다.
 */
public final class CpfProcessLifecycleFixture {
    private final AtomicBoolean alive = new AtomicBoolean(true);
    private volatile CountDownLatch killed = new CountDownLatch(1);

    public void kill() {
        if (alive.compareAndSet(true, false)) killed.countDown();
    }

    public void restart() {
        alive.set(true);
        killed = new CountDownLatch(1);
    }

    public boolean alive() { return alive.get(); }

    public boolean awaitKilled(Duration timeout) throws InterruptedException {
        Objects.requireNonNull(timeout, "timeout");
        if (timeout.isNegative()) throw new IllegalArgumentException("timeout must not be negative");
        return killed.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
    }
}

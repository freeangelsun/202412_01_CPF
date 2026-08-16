package com.cpf.testkit;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/** Deterministic stand-in for process-kill/recovery tests without terminating the test JVM. */
public final class CpfProcessKillHarness {
    private final AtomicBoolean alive = new AtomicBoolean(true);
    private final CountDownLatch killed = new CountDownLatch(1);
    public void kill() { if (alive.compareAndSet(true, false)) killed.countDown(); }
    public void restart() { alive.set(true); }
    public boolean alive() { return alive.get(); }
    public boolean awaitKilled(Duration timeout) throws InterruptedException { return killed.await(timeout.toMillis(), TimeUnit.MILLISECONDS); }
}

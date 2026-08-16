package com.cpf.platform.operations.health;

import com.cpf.platform.operations.api.health.CpfDrainControl;
import com.cpf.platform.operations.api.health.CpfDrainState;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

/** 신규 유입을 먼저 차단하고 기존 요청이 소진될 때까지 기다리는 Graceful Drain 구현입니다. */
public final class CpfDrainManager implements CpfDrainControl {
    private final AtomicReference<CpfDrainState> state = new AtomicReference<>(CpfDrainState.RUNNING);
    private final AtomicLong inFlight = new AtomicLong();
    @Override public CpfDrainState state() { return state.get(); }
    @Override public long inFlight() { return inFlight.get(); }
    @Override public boolean tryEnter() {
        if (state.get() != CpfDrainState.RUNNING) return false;
        inFlight.incrementAndGet();
        if (state.get() != CpfDrainState.RUNNING) { leave(); return false; }
        return true;
    }
    @Override public void leave() {
        long remaining = inFlight.updateAndGet(value -> Math.max(0, value - 1));
        if (remaining == 0) state.compareAndSet(CpfDrainState.DRAINING, CpfDrainState.STOPPED);
    }
    @Override public CpfDrainState beginDrain(Duration timeout) {
        Duration safe = timeout == null || timeout.isNegative() ? Duration.ZERO : timeout;
        state.updateAndGet(current -> current == CpfDrainState.STOPPED ? current : CpfDrainState.DRAINING);
        long deadline = System.nanoTime() + safe.toNanos();
        while (inFlight.get() > 0 && System.nanoTime() < deadline) LockSupport.parkNanos(1_000_000L);
        if (inFlight.get() == 0) state.set(CpfDrainState.STOPPED);
        return state.get();
    }
    @Override public void resume() {
        if (inFlight.get() != 0) throw new IllegalStateException("cannot resume while in-flight work remains");
        state.set(CpfDrainState.RUNNING);
    }
}

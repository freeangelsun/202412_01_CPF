package com.cpf.starter.platform.operations.health;
import com.cpf.core.api.health.*; import java.time.Duration; import java.util.concurrent.atomic.*; import java.util.concurrent.locks.LockSupport;
public final class CpfDrainManager implements CpfDrainControl {
 private final AtomicReference<CpfDrainState> state=new AtomicReference<>(CpfDrainState.RUNNING); private final AtomicLong inFlight=new AtomicLong();
 @Override public CpfDrainState state(){return state.get();} @Override public long inFlight(){return inFlight.get();}
 @Override public boolean tryEnter(){ if(state.get()!=CpfDrainState.RUNNING) return false; inFlight.incrementAndGet(); if(state.get()!=CpfDrainState.RUNNING){leave();return false;} return true; }
 @Override public void leave(){ long remaining=inFlight.updateAndGet(v->Math.max(0,v-1)); if(remaining==0) state.compareAndSet(CpfDrainState.DRAINING,CpfDrainState.STOPPED); }
 @Override public CpfDrainState beginDrain(Duration timeout){ state.set(CpfDrainState.DRAINING); long deadline=System.nanoTime()+Math.max(0,timeout.toNanos()); while(inFlight.get()>0 && System.nanoTime()<deadline) LockSupport.parkNanos(2_000_000L); if(inFlight.get()==0) state.set(CpfDrainState.STOPPED); return state.get(); }
 @Override public void resume(){ if(inFlight.get()!=0) throw new IllegalStateException("cannot resume while in-flight work remains"); state.set(CpfDrainState.RUNNING); }
}

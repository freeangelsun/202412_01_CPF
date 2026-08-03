package com.cpf.batch.execution;
import com.cpf.batch.api.BatchControlState;
import com.cpf.batch.spi.BatchExecutionLedgerPort;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
public final class CpfBatchAbandonCoordinatorHarness {
 private static int assertions;
 public static void main(String[] args) throws Exception {
  happyPathClaimsBeforeSideEffect();
  claimConflictPreventsSideEffect();
  sideEffectFailureRecordsUnknownAndMasksSecret();
  finalLedgerFailureRecordsUnknown();
  concurrentCallsDispatchExactlyOnce();
  System.out.println("CPF_BATCH_ABANDON_RUNTIME_PASS assertions="+assertions);
 }
 private static void happyPathClaimsBeforeSideEffect(){
  Ledger l=new Ledger(BatchControlState.STOPPED);AtomicInteger calls=new AtomicInteger();
  new CpfBatchAbandonCoordinator(l).abandon("E1","operator reason",()->{check(l.state==BatchControlState.ABANDONING,"claim precedes side effect");calls.incrementAndGet();});
  check(calls.get()==1,"side effect once");check(l.state==BatchControlState.ABANDONED,"final abandoned");check(l.history.equals(List.of(BatchControlState.ABANDONING,BatchControlState.ABANDONED)),"transition order");
 }
 private static void claimConflictPreventsSideEffect(){
  Ledger l=new Ledger(BatchControlState.STARTED);AtomicInteger calls=new AtomicInteger();
  expect(IllegalStateException.class,()->new CpfBatchAbandonCoordinator(l).abandon("E2","reason",calls::incrementAndGet));
  check(calls.get()==0,"conflict no external side effect");check(l.state==BatchControlState.STARTED,"conflict state unchanged");
 }
 private static void sideEffectFailureRecordsUnknownAndMasksSecret(){
  Ledger l=new Ledger(BatchControlState.FAILED);
  CpfBatchUnknownResultException ex=expect(CpfBatchUnknownResultException.class,()->new CpfBatchAbandonCoordinator(l).abandon("E3","reason",()->{throw new IllegalStateException("token=raw-secret response lost");}));
  check("BATCH_ABANDON_RESPONSE_UNKNOWN".equals(ex.code()),"response unknown code");check(l.state==BatchControlState.UNKNOWN_RESULT,"failure unknown state");
  check(l.lastDetail.contains("token=<masked>"),"secret masked");check(!l.lastDetail.contains("raw-secret"),"raw secret absent");
 }
 private static void finalLedgerFailureRecordsUnknown(){
  Ledger l=new Ledger(BatchControlState.UNKNOWN_RESULT);l.failFinal=true;
  CpfBatchUnknownResultException ex=expect(CpfBatchUnknownResultException.class,()->new CpfBatchAbandonCoordinator(l).abandon("E4","reason",()->{}));
  check("BATCH_ABANDON_LEDGER_CONFIRM_UNKNOWN".equals(ex.code()),"confirm unknown code");check(l.state==BatchControlState.UNKNOWN_RESULT,"confirm failure unknown state");check(l.unknownRecords==1,"confirm failure evidence recorded");
 }
 private static void concurrentCallsDispatchExactlyOnce() throws Exception {
  Ledger l=new Ledger(BatchControlState.STOPPED);AtomicInteger calls=new AtomicInteger();CountDownLatch entered=new CountDownLatch(1),release=new CountDownLatch(1);
  ExecutorService pool=Executors.newFixedThreadPool(2);
  Callable<String> task=()->{try{new CpfBatchAbandonCoordinator(l).abandon("E5","reason",()->{calls.incrementAndGet();entered.countDown();release.await(5,TimeUnit.SECONDS);});return "OK";}catch(Throwable x){return x.getClass().getSimpleName();}};
  Future<String> a=pool.submit(task);check(entered.await(5,TimeUnit.SECONDS),"first entered side effect");Future<String> b=pool.submit(task);Thread.sleep(100);release.countDown();String ar=a.get(5,TimeUnit.SECONDS),br=b.get(5,TimeUnit.SECONDS);pool.shutdownNow();
  check(calls.get()==1,"concurrent external side effect exactly once");check(("OK".equals(ar)&&"IllegalStateException".equals(br))||("OK".equals(br)&&"IllegalStateException".equals(ar)),"one success one claim conflict");check(l.state==BatchControlState.ABANDONED,"concurrent final state");
 }
 private static void check(boolean c,String m){assertions++;if(!c)throw new AssertionError(m);} private static <T extends Throwable>T expect(Class<T> type,Throwing x){assertions++;try{x.run();}catch(Throwable t){if(type.isInstance(t))return type.cast(t);throw new AssertionError(t);}throw new AssertionError("expected "+type);} @FunctionalInterface interface Throwing{void run() throws Exception;}
 private static final class Ledger implements BatchExecutionLedgerPort {
  private BatchControlState state;private final List<BatchControlState> history=new ArrayList<>();private boolean failFinal;private int unknownRecords;private String lastDetail="";
  Ledger(BatchControlState state){this.state=state;}
  @Override public synchronized void transition(String id,Set<BatchControlState> expected,BatchControlState target,String reason,String detail,Instant reconcileAfter){
   if(!expected.contains(state))throw new IllegalStateException("state conflict expected="+expected+" actual="+state);
   if(failFinal&&target==BatchControlState.ABANDONED){failFinal=false;throw new IllegalStateException("ledger confirmation unavailable");}
   state=target;history.add(target);lastDetail=Objects.toString(detail,"");if(target==BatchControlState.UNKNOWN_RESULT)unknownRecords++;
  }
 }
}

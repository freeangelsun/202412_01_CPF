package com.cpf.starter.integration.resilience.internal;
import com.cpf.core.api.locking.CpfLockManager;import com.cpf.core.internal.locking.DefaultCpfLockManager;import java.time.*;import java.util.concurrent.*;import java.util.concurrent.atomic.AtomicInteger;import org.springframework.jdbc.core.JdbcTemplate;import org.springframework.transaction.support.TransactionTemplate;
public final class JdbcCpfLockStoreHarness{
 public static void main(String[]args)throws Exception{
  JdbcCpfLockStore store=new JdbcCpfLockStore(new JdbcTemplate(),new TransactionTemplate()); Clock clock=Clock.fixed(Instant.parse("2026-08-05T00:00:00Z"),ZoneOffset.UTC); CpfLockManager manager=new DefaultCpfLockManager(store,null,clock);
  ExecutorService pool=Executors.newFixedThreadPool(12);CountDownLatch ready=new CountDownLatch(12),start=new CountDownLatch(1);AtomicInteger won=new AtomicInteger();
  Future<?>[]fs=new Future<?>[12];for(int i=0;i<12;i++){int id=i;fs[i]=pool.submit(()->{ready.countDown();start.await();if(manager.acquire("shared","node-"+id,"req-"+id,Duration.ofSeconds(5)).status()==CpfLockManager.AcquireStatus.ACQUIRED)won.incrementAndGet();return null;});}
  ready.await(5,TimeUnit.SECONDS);start.countDown();for(Future<?>f:fs)f.get(5,TimeUnit.SECONDS);pool.shutdownNow();if(won.get()!=1)throw new AssertionError("single winner expected "+won.get());
  CpfLockManager.LockToken first=manager.find("shared").map(s->new CpfLockManager.LockToken(s.key(),s.ownerId(),s.requestId(),s.fencingToken(),s.ownerEpoch(),s.version(),s.leaseUntil())).orElseThrow();
  CpfLockManager.RenewResult renewed=manager.renew(first,Duration.ofSeconds(5));if(renewed.status()!=CpfLockManager.RenewStatus.RENEWED)throw new AssertionError("renew failed "+renewed.status());if(renewed.token().version()!=first.version()+1)throw new AssertionError("version did not increment");if(manager.renew(first,Duration.ofSeconds(5)).status()!=CpfLockManager.RenewStatus.STALE_TOKEN)throw new AssertionError("stale version accepted");
  manager.release(renewed.token(),"done"); CpfLockManager.AcquireResult second=manager.acquire("shared","next","req-next",Duration.ofSeconds(5));if(second.token().fencingToken()<=first.fencingToken())throw new AssertionError("fence not monotonic");if(store.list(10).size()!=1)throw new AssertionError("list mismatch");
  boolean outside=false;try{store.nextFence("outside");}catch(IllegalStateException expected){outside=true;}if(!outside)throw new AssertionError("nextFence outside transaction accepted");
  System.out.println("CPF_JDBC_LOCK_STORE_HARNESS_PASS");
 }
}

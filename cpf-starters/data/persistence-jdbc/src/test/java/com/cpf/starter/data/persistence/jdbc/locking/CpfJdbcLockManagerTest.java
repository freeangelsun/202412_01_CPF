package com.cpf.starter.data.persistence.jdbc.locking;

import com.cpf.core.api.locking.CpfLockManager;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CpfJdbcLockManagerTest {
 @Test void acquireReplayBusyRenewReleaseAndTakeover(){
  MutableClock clock=new MutableClock(Instant.parse("2026-08-08T00:00:00Z")); Store store=new Store(); CpfJdbcLockManager m=manager(store,clock);
  var a=m.acquire("job:1","node-a","req-1",Duration.ofSeconds(10)); assertEquals(CpfLockManager.AcquireStatus.ACQUIRED,a.status()); assertEquals(1,a.token().fencingToken());
  assertEquals(CpfLockManager.AcquireStatus.IDEMPOTENT_REPLAY,m.acquire("job:1","node-a","req-1",Duration.ofSeconds(10)).status());
  assertEquals(CpfLockManager.AcquireStatus.BUSY,m.acquire("job:1","node-b","req-2",Duration.ofSeconds(10)).status());
  var renewed=m.renew(a.token(),Duration.ofSeconds(20)); assertEquals(CpfLockManager.RenewStatus.RENEWED,renewed.status()); assertTrue(m.validateToken(renewed.token()));
  assertEquals(CpfLockManager.ReleaseStatus.STALE_TOKEN,m.release(a.token(),"old-token").status());
  assertEquals(CpfLockManager.ReleaseStatus.RELEASED,m.release(renewed.token(),"done").status());
  assertEquals(CpfLockManager.ReleaseStatus.IDEMPOTENT_REPLAY,m.release(renewed.token(),"done-again").status());
  var next=m.acquire("job:1","node-b","req-2",Duration.ofSeconds(10)); assertEquals(2,next.token().fencingToken()); assertEquals(2,next.token().ownerEpoch());
 }
 @Test void expiryRejectsStaleWriterAndReconcileRecovers(){
  MutableClock clock=new MutableClock(Instant.parse("2026-08-08T00:00:00Z")); Store store=new Store(); CpfJdbcLockManager m=manager(store,clock);
  var a=m.acquire("k","a","r1",Duration.ofSeconds(2)); clock.plus(Duration.ofSeconds(3));
  assertFalse(m.validateFence("k",a.token().fencingToken()));
  var b=m.acquire("k","b","r2",Duration.ofSeconds(2)); assertEquals(2,b.token().fencingToken()); assertEquals(CpfLockManager.ReleaseStatus.STALE_TOKEN,m.release(a.token(),"stale").status());
  clock.plus(Duration.ofSeconds(3)); var rr=m.reconcileExpired(10); assertEquals(CpfLockManager.RecoveryStatus.SUCCESS,rr.status()); assertEquals(1,rr.recovered()); assertEquals(CpfLockManager.State.EXPIRED,m.find("k").orElseThrow().state());
 }
 @Test void forceReleaseRequiresSeparationAndCommandBoundApproval(){
  MutableClock clock=new MutableClock(Instant.parse("2026-08-08T00:00:00Z")); Store store=new Store(); CpfJdbcLockManager m=manager(store,clock);
  var a=m.acquire("k","owner","req",Duration.ofMinutes(5));
  var cmd=new CpfLockManager.ForceReleaseCommand("k","operator","incident-42",a.token().fencingToken(),a.token().version());
  var same=CpfLockManager.ForceReleaseApproval.approve("ap-1","operator",cmd,clock.instant(),clock.instant().plusSeconds(60));
  assertEquals(CpfLockManager.ForceReleaseStatus.SEPARATION_OF_DUTIES,m.forceRelease("k","operator","incident-42",same).status());
  var ok=CpfLockManager.ForceReleaseApproval.approve("ap-2","approver",cmd,clock.instant(),clock.instant().plusSeconds(60));
  assertEquals(CpfLockManager.ForceReleaseStatus.RELEASED,m.forceRelease("k","operator","incident-42",ok).status());
 }
 @Test void invalidAndStorageFailureFailClosed(){
  MutableClock clock=new MutableClock(Instant.EPOCH); Store store=new Store(); CpfJdbcLockManager m=manager(store,clock);
  assertEquals(CpfLockManager.AcquireStatus.INVALID,m.acquire("","a","r",Duration.ofSeconds(1)).status());
  assertEquals(CpfLockManager.AcquireStatus.INVALID,m.acquire("k","a","r",Duration.ofDays(2)).status());
  store.fail=true; assertEquals(CpfLockManager.AcquireStatus.UNKNOWN,m.acquire("k","a","r",Duration.ofSeconds(1)).status()); assertFalse(m.validateFence("k",1));
 }
 private static CpfJdbcLockManager manager(Store s,Clock c){ return new CpfJdbcLockManager(s,new TransactionTemplate(new NoopTx()),c); }
 static final class NoopTx implements PlatformTransactionManager {
  public TransactionStatus getTransaction(TransactionDefinition d){return new SimpleTransactionStatus();}
  public void commit(TransactionStatus s){}
  public void rollback(TransactionStatus s){}
 }
 static final class Store implements CpfJdbcLockManager.Store {
  final Map<String,CpfJdbcLockManager.LockRow> rows=new LinkedHashMap<>(); boolean fail;
  public Optional<CpfJdbcLockManager.LockRow> findForUpdate(String k){check();return Optional.ofNullable(rows.get(k));}
  public Optional<CpfJdbcLockManager.LockRow> find(String k){check();return Optional.ofNullable(rows.get(k));}
  public List<CpfJdbcLockManager.LockRow> list(int l){check();return new ArrayList<>(rows.values()).stream().limit(l).toList();}
  public void insert(CpfJdbcLockManager.LockRow r){check(); if(rows.putIfAbsent(r.key(),r)!=null) throw new org.springframework.dao.DuplicateKeyException("race");}
  public int update(CpfJdbcLockManager.LockRow r,long expected){check(); var old=rows.get(r.key()); if(old==null||old.version()!=expected)return 0; rows.put(r.key(),r);return 1;}
  void check(){if(fail)throw new IllegalStateException("db down");}
 }
 static final class MutableClock extends Clock {
  private Instant now; MutableClock(Instant now){this.now=now;} void plus(Duration d){now=now.plus(d);} public Instant instant(){return now;} public ZoneOffset getZone(){return ZoneOffset.UTC;} public Clock withZone(java.time.ZoneId zone){return this;}
 }
}

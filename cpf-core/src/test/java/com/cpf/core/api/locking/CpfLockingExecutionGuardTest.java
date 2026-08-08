package com.cpf.core.api.locking;

import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class CpfLockingExecutionGuardTest {
 @Test void passesFencingTokenAndReleases(){
  FakeManager m=new FakeManager(); CpfLockingExecutionGuard g=new CpfLockingExecutionGuard(m);
  long token=g.executeFenced("K","A","R",Duration.ofSeconds(5),t->t.fencingToken());
  assertEquals(1L,token); assertTrue(m.released);
 }
 @Test void rejectsStaleWriterBeforeCommit(){
  FakeManager m=new FakeManager(); m.invalidateAfterFirst=true; CpfLockingExecutionGuard g=new CpfLockingExecutionGuard(m);
  assertThrows(CpfLockingExecutionGuard.StaleFenceException.class,()->g.execute("K","A","R",Duration.ofSeconds(5),()->"ok"));
  assertTrue(m.released);
 }
 static final class FakeManager implements CpfLockManager {
  final Instant until=Instant.parse("2030-01-01T00:00:00Z"); boolean released; boolean invalidateAfterFirst; int validations;
  public AcquireResult acquire(String k,String o,String r,Duration d){return new AcquireResult(AcquireStatus.ACQUIRED,new LockToken(k,o,r,1,1,1,until),null,"ok");}
  public RenewResult renew(LockToken t,Duration d){throw new UnsupportedOperationException();}
  public ReleaseResult release(LockToken t,String reason){released=true;return new ReleaseResult(ReleaseStatus.RELEASED,null,"ok");}
  public boolean validateFence(String k,long f){return !invalidateAfterFirst || ++validations==1;}
  public Optional<LockSnapshot> find(String k){return Optional.of(new LockSnapshot(k,"A","R",1,1,1,Instant.EPOCH,until,State.ACTIVE));}
  public List<LockSnapshot> list(int l){return List.of();}
  public ForceReleaseResult forceRelease(String k,String o,String r,ForceReleaseApproval a){throw new UnsupportedOperationException();}
 }
}

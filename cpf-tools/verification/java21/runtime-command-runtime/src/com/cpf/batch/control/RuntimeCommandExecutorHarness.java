package com.cpf.batch.control;

import com.cpf.batch.api.*;
import com.cpf.batch.control.deploy.RuntimeLifecycleService;
import com.cpf.batch.control.internal.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class RuntimeCommandExecutorHarness {
 private static int assertions;
 public static void main(String[] args){
  validationStopsBeforeDispatch();
  duplicateTargetsStopBeforeDispatch();
  validationFinalizeFailureIsStableUnknown();
  snapshotFailureIsDeterministic();
  desiredStateConflictIsDeterministic();
  dispatchFailureIsUnknownAndMasked();
  partialFailureAggregatesFailed();
  unknownDominatesFailed();
  attemptEvidenceFailureBecomesUnknown();
  finalTransitionFailureThrowsStableUnknown();
  concurrentExecutionReturnsPersistedResult();
  rollbackPostStateFailureBecomesUnknown();
  System.out.println("CPF_RUNTIME_COMMAND_EXECUTOR_PASS assertions="+assertions);
 }

 private static RuntimeCommand command(String type,List<String> targets){
  return new RuntimeCommand("CMD-1","IDEM-1",type,targets,7L,"requester","approved maintenance",
    "POLICY-1","APR-1","approver",Instant.now().plusSeconds(300));
 }

 private static void validationStopsBeforeDispatch(){
  FakeRepo repo=new FakeRepo(); FakeRegistry reg=new FakeRegistry(); FakeLifecycle life=new FakeLifecycle();
  Map<String,Object> result=new RuntimeCommandExecutor(repo,reg,life).execute(command("DROP",List.of("a")));
  check("FAILED".equals(result.get("execution_state")),"validation state");
  check(life.calls.get()==0,"validation no dispatch");
  check("VALIDATION".equals(repo.lastTransitionStage),"validation stage");
 }


 private static void duplicateTargetsStopBeforeDispatch(){
  FakeRepo repo=new FakeRepo(); FakeRegistry reg=new FakeRegistry(); FakeLifecycle life=new FakeLifecycle();
  Map<String,Object> result=new RuntimeCommandExecutor(repo,reg,life).execute(command("RESTART",List.of("a","a")));
  check("FAILED".equals(result.get("execution_state")),"duplicate state");
  check(life.calls.get()==0,"duplicate no dispatch");
  check("VALIDATION".equals(repo.lastTransitionStage),"duplicate validation stage");
  check("Duplicate target IDs are not allowed".equals(repo.lastSummary),"duplicate validation message");
 }

 private static void validationFinalizeFailureIsStableUnknown(){
  FakeRepo repo=new FakeRepo(); FakeRegistry reg=new FakeRegistry(); FakeLifecycle life=new FakeLifecycle();repo.failAnyTransition=true;
  RuntimeCommandExecutionException ex=expect(RuntimeCommandExecutionException.class,
    ()->new RuntimeCommandExecutor(repo,reg,life).execute(command("RESTART",List.of("a","a"))));
  check("BATCH_RUNTIME_COMMAND_VALIDATION_FINALIZE_UNKNOWN".equals(ex.code()),"validation finalize code");
  check(ex.state()==CommandState.UNKNOWN_RESULT,"validation finalize state");
  check(life.calls.get()==0,"validation finalize no dispatch");
 }

 private static void snapshotFailureIsDeterministic(){
  FakeRepo repo=new FakeRepo(); FakeRegistry reg=new FakeRegistry(); FakeLifecycle life=new FakeLifecycle();reg.snapshotFailure=new IllegalArgumentException("missing runtime");
  new RuntimeCommandExecutor(repo,reg,life).execute(command("RESTART",List.of("a")));
  check(repo.lastTransitionState==CommandState.FAILED,"snapshot final failed");
  check("CONTROL_SNAPSHOT".equals(repo.attempts.get(0).stage),"snapshot stage");
  check(repo.attempts.get(0).state==CommandState.FAILED,"snapshot attempt failed");
  check(life.calls.get()==0,"snapshot no dispatch");
 }

 private static void desiredStateConflictIsDeterministic(){
  FakeRepo repo=new FakeRepo(); FakeRegistry reg=new FakeRegistry(); FakeLifecycle life=new FakeLifecycle();reg.updateFailure=new IllegalStateException("CAS conflict");
  new RuntimeCommandExecutor(repo,reg,life).execute(command("STOP",List.of("a")));
  check(repo.lastTransitionState==CommandState.FAILED,"desired final failed");
  check("DESIRED_STATE_UPDATE".equals(repo.attempts.get(0).stage),"desired stage");
  check(life.calls.get()==0,"desired no dispatch");
 }

 private static void dispatchFailureIsUnknownAndMasked(){
  FakeRepo repo=new FakeRepo(); FakeRegistry reg=new FakeRegistry(); FakeLifecycle life=new FakeLifecycle();life.failure=new IllegalStateException("token=raw-secret response lost");
  new RuntimeCommandExecutor(repo,reg,life).execute(command("RESTART",List.of("a")));
  check(repo.lastTransitionState==CommandState.UNKNOWN_RESULT,"dispatch final unknown");
  Attempt a=repo.attempts.get(0);
  check("OWNER_API_DISPATCH".equals(a.stage),"dispatch stage");
  check(a.message.contains("token=<masked>"),"dispatch secret masked");
  check(!a.message.contains("raw-secret"),"dispatch raw secret absent");
 }

 private static void partialFailureAggregatesFailed(){
  FakeRepo repo=new FakeRepo(); FakeRegistry reg=new FakeRegistry(); FakeLifecycle life=new FakeLifecycle();
  life.results.put("a",new AgentCommandResult(CommandState.SUCCEEDED,"ok"));
  life.results.put("b",new AgentCommandResult(CommandState.FAILED,"agent refused"));
  new RuntimeCommandExecutor(repo,reg,life).execute(command("RESTART",List.of("a","b")));
  check(repo.lastTransitionState==CommandState.FAILED,"partial final failed");
  check(repo.attempts.size()==2,"partial attempts");
  check(repo.lastSummary.contains("a=SUCCEEDED")&&repo.lastSummary.contains("b=FAILED"),"partial summary");
 }

 private static void unknownDominatesFailed(){
  FakeRepo repo=new FakeRepo(); FakeRegistry reg=new FakeRegistry(); FakeLifecycle life=new FakeLifecycle();
  life.results.put("a",new AgentCommandResult(CommandState.FAILED,"failed"));
  life.results.put("b",new AgentCommandResult(CommandState.UNKNOWN_RESULT,"response missing"));
  new RuntimeCommandExecutor(repo,reg,life).execute(command("RESTART",List.of("a","b")));
  check(repo.lastTransitionState==CommandState.UNKNOWN_RESULT,"unknown precedence");
  check("AGENT_RESTART".equals(repo.lastTransitionStage),"unknown stage retained");
 }

 private static void attemptEvidenceFailureBecomesUnknown(){
  FakeRepo repo=new FakeRepo(); FakeRegistry reg=new FakeRegistry(); FakeLifecycle life=new FakeLifecycle();repo.failAttempt=true;
  life.results.put("a",new AgentCommandResult(CommandState.SUCCEEDED,"ok"));
  new RuntimeCommandExecutor(repo,reg,life).execute(command("START",List.of("a")));
  check(repo.lastTransitionState==CommandState.UNKNOWN_RESULT,"attempt evidence unknown");
  check("ATTEMPT_EVIDENCE_PERSISTENCE".equals(repo.lastTransitionStage),"attempt evidence stage");
 }

 private static void finalTransitionFailureThrowsStableUnknown(){
  FakeRepo repo=new FakeRepo(); FakeRegistry reg=new FakeRegistry(); FakeLifecycle life=new FakeLifecycle();repo.failTransitionAtFinal=true;
  life.results.put("a",new AgentCommandResult(CommandState.SUCCEEDED,"ok"));
  RuntimeCommandExecutionException ex=expect(RuntimeCommandExecutionException.class,
    ()->new RuntimeCommandExecutor(repo,reg,life).execute(command("START",List.of("a"))));
  check("BATCH_RUNTIME_COMMAND_FINALIZE_UNKNOWN".equals(ex.code()),"finalization code");
  check(ex.state()==CommandState.UNKNOWN_RESULT,"finalization state");
 }

 private static void concurrentExecutionReturnsPersistedResult(){
  FakeRepo repo=new FakeRepo(); FakeRegistry reg=new FakeRegistry(); FakeLifecycle life=new FakeLifecycle();repo.begin=false;
  Map<String,Object> result=new RuntimeCommandExecutor(repo,reg,life).execute(command("START",List.of("a")));
  check("APPROVED".equals(result.get("execution_state")),"concurrent existing result");
  check(life.calls.get()==0,"concurrent no duplicate dispatch");
 }

 private static void rollbackPostStateFailureBecomesUnknown(){
  FakeRepo repo=new FakeRepo(); FakeRegistry reg=new FakeRegistry(); FakeLifecycle life=new FakeLifecycle();
  life.results.put("a",new AgentCommandResult(CommandState.SUCCEEDED,"rolled back"));reg.failSecondUpdate=true;
  new RuntimeCommandExecutor(repo,reg,life).execute(command("ROLLBACK",List.of("a")));
  check(repo.lastTransitionState==CommandState.UNKNOWN_RESULT,"rollback post state unknown");
  check("POST_ROLLBACK_STATE".equals(repo.lastTransitionStage),"rollback post stage");
  check(reg.updates==2,"rollback desired then running");
 }

 private static void check(boolean c,String m){assertions++;if(!c)throw new AssertionError(m);}
 private static <T extends Throwable>T expect(Class<T> t,Throwing r){assertions++;try{r.run();}catch(Throwable x){if(t.isInstance(x))return t.cast(x);throw new AssertionError(x);}throw new AssertionError("expected "+t);}
 @FunctionalInterface interface Throwing{void run();}

 private record Attempt(String stage,CommandState state,String message){}
 private static final class FakeRepo extends JdbcRuntimeCommandRepository{
  boolean begin=true,failAttempt,failTransitionAtFinal,failAnyTransition; CommandState lastTransitionState;String lastTransitionStage,lastSummary;List<Attempt> attempts=new ArrayList<>();
  Map<String,Object> current=new LinkedHashMap<>(Map.of("command_id","CMD-1","execution_state","APPROVED"));
  @Override public Map<String,Object> create(RuntimeCommand c){return current;}
  @Override public boolean beginExecution(String id){return begin;}
  @Override public Optional<Map<String,Object>> find(String key){return Optional.of(current);}
  @Override public void transition(String id,CommandState state,String stage,String result){
   if(failAnyTransition)throw new IllegalStateException("db unavailable");
   if(failTransitionAtFinal && state==CommandState.SUCCEEDED)throw new IllegalStateException("db unavailable");
   lastTransitionState=state;lastTransitionStage=stage;lastSummary=result;current=new LinkedHashMap<>(Map.of("command_id","CMD-1","execution_state",state.name()));
  }
  @Override public void recordAttempt(String id,int attempt,String target,String stage,CommandState state,String message){if(failAttempt)throw new IllegalStateException("attempt db unavailable");attempts.add(new Attempt(stage,state,message));}
 }
 private static final class FakeRegistry extends JdbcRuntimeRegistry{
  RuntimeException snapshotFailure,updateFailure;boolean failSecondUpdate;int updates;
  @Override public Map<String,Object> snapshot(String id){if(snapshotFailure!=null)throw snapshotFailure;return Map.of("actual_state","RUNNING");}
  @Override public long updateDesiredState(String id,DesiredState state,long version){updates++;if(updateFailure!=null)throw updateFailure;if(failSecondUpdate&&updates==2)throw new IllegalStateException("post rollback CAS conflict");return version+1;}
 }
 private static final class FakeLifecycle extends RuntimeLifecycleService{
  AtomicInteger calls=new AtomicInteger();RuntimeException failure;Map<String,AgentCommandResult> results=new HashMap<>();
  @Override public AgentCommandResult operate(String target,String type,String requestedBy,String approvedBy,String approvalRequestId,String reason){calls.incrementAndGet();if(failure!=null)throw failure;return results.getOrDefault(target,new AgentCommandResult(CommandState.SUCCEEDED,"ok"));}
 }
}

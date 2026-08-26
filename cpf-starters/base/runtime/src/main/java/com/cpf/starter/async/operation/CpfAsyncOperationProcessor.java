package com.cpf.starter.async.operation;

import com.cpf.core.api.async.*;
import com.cpf.core.api.context.*;
import com.cpf.core.api.result.CpfResult;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import java.time.*; import java.util.*; import java.util.concurrent.*;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.core.task.AsyncTaskExecutor;

/** claim/lease/heartbeat/fencing으로 Async handler를 실행하는 Runtime worker입니다. */
public final class CpfAsyncOperationProcessor {
 private static final Logger log=LoggerFactory.getLogger(CpfAsyncOperationProcessor.class);
 private final CpfAsyncOperationStore store; private final DefaultCpfAsyncOperations operations; private final CpfAsyncPayloadCodec payloads;
 private final CpfExecutionIdGenerator ids; private final AsyncTaskExecutor executor; private final String owner; private final Clock clock;
 private final Duration lease; private final List<CpfAsyncCompletionListener> listeners; private final ConcurrentMap<String,Active> active=new ConcurrentHashMap<>();
 public CpfAsyncOperationProcessor(CpfAsyncOperationStore store,DefaultCpfAsyncOperations operations,AsyncTaskExecutor executor,String owner,Clock clock,Duration lease,List<CpfAsyncCompletionListener> listeners){this.store=store;this.operations=operations;this.payloads=operations.payloadCodec();this.ids=operations.executionIds();this.executor=executor;this.owner=owner;this.clock=clock;this.lease=lease;this.listeners=listeners==null?List.of():List.copyOf(listeners);}
 public void poll(){Instant now=clock.instant();store.expireDue(now);if(active.size()>=32)return;store.claimNext(owner,now,now.plus(lease)).ifPresent(op->{Future<?> f=executor.submit(()->execute(op));active.put(op.executionId(),new Active(op.version(),f));});}
 public void heartbeat(){Instant now=clock.instant();for(var e:active.entrySet()){if(e.getValue().future().isDone()){active.remove(e.getKey(),e.getValue());continue;}if(!store.heartbeat(e.getKey(),owner,e.getValue().version(),now,now.plus(lease))) log.warn("CPF_ASYNC_HEARTBEAT_FENCE executionId={} owner={}",e.getKey(),owner);}}
 @SuppressWarnings({"unchecked","rawtypes"}) private void execute(CpfAsyncStoredOperation op){
  try{
   CpfAsyncHandler handler=operations.handler(op.commandType()); Object command=payloads.reveal("async-command",op.commandPayload(),handler.commandType(),"CPF_ASYNC_RUNTIME:"+owner,"ASYNC_COMMAND_EXECUTE"); CpfContextSnapshot stored=payloads.reveal("async-context",op.contextPayload(),CpfContextSnapshot.class,"CPF_ASYNC_RUNTIME:"+owner,"ASYNC_CONTEXT_RESTORE");
   CpfContext parent=stored.context(); Instant now=clock.instant(); CpfContext.CpfExecutionContext pe=parent.execution();
   CpfContext.CpfExecutionContext asyncExec=new CpfContext.CpfExecutionContext(pe.standardExecutionId(),op.executionId(),pe.rootExecutionId(),pe.executionId(),ids.newSegmentId(),pe.segmentId(),CpfContext.CpfExecutionType.ASYNC,1,pe.callDepth()+1,now,op.expiresAt(),CpfContext.CpfCancellationMode.COOPERATIVE);
   CpfContext.CpfOperationContext prev=parent.operation(); CpfContext.CpfOperationContext asyncOperation=new CpfContext.CpfOperationContext(op.operationId(),op.operationId(),op.executionId(),op.idempotencyKey(),CpfContext.CpfIdempotencyScope.CURRENT_OPERATION,CpfContext.CpfIdempotencyMode.REQUIRED,null,prev==null?null:prev.operationId(),null,prev==null?1:prev.transactionSequence()+1);
   CpfContextSnapshot snapshot=CpfContextSnapshot.capture(parent.child(asyncExec,asyncOperation),now);
   CpfResult<?> result=CpfContexts.call(snapshot,()->handler.execute(command,new Execution(op.executionId())));
   if(result==null) result=CpfResult.technicalFailure("CPF-ASYNC-NULL-RESULT","Async handler returned null");
   String data=result.isSuccess()?payloads.protect("async-result",result.data()):null;
   CpfAsyncStoredOperation completed=store.complete(op.executionId(),owner,op.version(),result.status().name(),handler.resultType().getName(),data,result.errorCode(),result.errorMessage(),result.recoveryInfo()==null?null:result.recoveryInfo().recoveryId(),result.recoveryInfo()==null?null:result.recoveryInfo().action(),clock.instant());
   notifyListeners(completed);
  }catch(CpfAsyncCancelledException cancelled){
   try{CpfAsyncStoredOperation completed=store.complete(op.executionId(),owner,op.version(),"CANCELLED",op.resultType(),null,"CPF-ASYNC-CANCELLED",safe(cancelled),null,null,clock.instant());notifyListeners(completed);}catch(Throwable secondary){log.error("CPF_ASYNC_CANCEL_RECORD_FAILED executionId={}",op.executionId(),secondary);}
  }catch(Throwable failure){
   try{CpfAsyncStoredOperation completed=store.complete(op.executionId(),owner,op.version(),"TECHNICAL_FAILURE",op.resultType(),null,"CPF-ASYNC-HANDLER",safe(failure),null,null,clock.instant());notifyListeners(completed);}catch(Throwable secondary){log.error("CPF_ASYNC_COMPLETION_RECORD_FAILED executionId={}",op.executionId(),secondary);}
  }finally{active.remove(op.executionId());}
 }
 private void notifyListeners(CpfAsyncStoredOperation op){var status=DefaultCpfAsyncOperations.status(op);for(var listener:listeners){try{if(listener.supports(op.operationId()))listener.onCompleted(status);}catch(RuntimeException e){log.warn("CPF_ASYNC_COMPLETION_LISTENER_FAILED executionId={} listener={}",op.executionId(),listener.getClass().getName(),e);}}}
 private String safe(Throwable t){String m=t.getMessage();return m==null||m.isBlank()?t.getClass().getSimpleName():m;}
 private record Active(long version,Future<?> future){}
 private final class Execution implements CpfAsyncExecution{private final String executionId;private Execution(String executionId){this.executionId=executionId;}public String executionId(){return executionId;}public boolean cancellationRequested(){return store.cancellationRequested(executionId);}}
}

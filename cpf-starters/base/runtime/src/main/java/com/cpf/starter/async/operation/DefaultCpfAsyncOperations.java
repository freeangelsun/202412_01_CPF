package com.cpf.starter.async.operation;

import com.cpf.core.api.async.*;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.core.api.result.CpfRecoveryInfo;
import com.cpf.core.api.result.CpfResult;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/** Durable Store 뒤에서 submit/status/result/cancel Public API를 구현합니다. */
public final class DefaultCpfAsyncOperations implements CpfAsyncOperations {
    private static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(15);
    private final CpfAsyncOperationStore store;
    private final Map<Class<?>, CpfAsyncHandler<?, ?>> handlersByCommand;
    private final ObjectMapper json;
    private final CpfAsyncPayloadCodec payloads;
    private final CpfExecutionIdGenerator ids;
    private final Clock clock;

    public DefaultCpfAsyncOperations(CpfAsyncOperationStore store, List<CpfAsyncHandler<?, ?>> handlers,
            ObjectMapper json, CpfAsyncPayloadCodec payloads, CpfExecutionIdGenerator ids, Clock clock) {
        this.store=Objects.requireNonNull(store,"store"); this.json=Objects.requireNonNull(json,"json");
        this.payloads=Objects.requireNonNull(payloads,"payloads");
        this.ids=Objects.requireNonNull(ids,"ids"); this.clock=Objects.requireNonNull(clock,"clock");
        Map<Class<?>,CpfAsyncHandler<?,?>> byCommand=new LinkedHashMap<>(); Set<String> operations=new HashSet<>();
        for (CpfAsyncHandler<?,?> handler : handlers == null ? List.<CpfAsyncHandler<?,?>>of() : handlers) {
            Objects.requireNonNull(handler,"handler");
            if (byCommand.putIfAbsent(handler.commandType(),handler)!=null) throw new IllegalStateException("Duplicate CPF Async command handler: "+handler.commandType().getName());
            if (!operations.add(required("operationId",handler.operationId()))) throw new IllegalStateException("Duplicate CPF Async operationId: "+handler.operationId());
        }
        this.handlersByCommand=Map.copyOf(byCommand);
    }

    @Override public <C> CpfAsyncSubmission submit(C command,String idempotencyKey,Duration timeout){
        Objects.requireNonNull(command,"command");
        @SuppressWarnings("unchecked") CpfAsyncHandler<C,?> handler=(CpfAsyncHandler<C,?>)handlersByCommand.get(command.getClass());
        if(handler==null) throw new IllegalArgumentException("CPF Async handler가 등록되지 않았습니다: "+command.getClass().getName());
        var current=CpfContexts.requireCurrent();
        String key=normalize(idempotencyKey); if(key==null) key=normalize(current.idempotencyKey());
        if(key==null) throw new IllegalArgumentException("Async submit idempotencyKey는 필수입니다.");
        Duration effective=timeout==null?DEFAULT_TIMEOUT:timeout;
        if(effective.isNegative()||effective.isZero()) throw new IllegalArgumentException("Async timeout은 양수여야 합니다.");
        Instant now=clock.instant(); String executionId=required("executionId",ids.newExecutionId());
        CpfAsyncStoredOperation candidate=new CpfAsyncStoredOperation(
                executionId,required("operationId",handler.operationId()),current.transactionId(),key,
                handler.commandType().getName(),payloads.protect("async-command",command),payloads.protect("async-context",CpfContexts.requireSnapshot()),handler.resultType().getName(),null,
                CpfAsyncState.ACCEPTED,null,null,null,null,null,now,null,now,null,now.plus(effective),null,null,null,null,1L);
        CpfAsyncStoredOperation actual=store.insertOrGet(candidate);
        return new CpfAsyncSubmission(actual.executionId(),actual.operationId(),actual.state(),actual.submittedAt(),!actual.executionId().equals(executionId));
    }

    @Override public CpfAsyncOperationStatus getStatus(String executionId){return status(require(executionId));}

    @Override public <R> CpfResult<R> getResult(String executionId,Class<R> resultType){
        Objects.requireNonNull(resultType,"resultType"); CpfAsyncStoredOperation op=require(executionId);
        if(!op.state().terminal()) throw new IllegalStateException("Async operation이 아직 완료되지 않았습니다: "+op.state());
        if(op.resultType()!=null && !op.resultType().equals(resultType.getName())) throw new IllegalArgumentException("Async result type mismatch: "+op.resultType());
        if(op.state()==CpfAsyncState.SUCCEEDED) return CpfResult.success(payloads.reveal("async-result",op.resultPayload(),resultType,"CPF_ASYNC_RESULT_READER","ASYNC_RESULT_READ"));
        if(op.state()==CpfAsyncState.UNKNOWN) return CpfResult.unknown(op.errorCode(),op.errorMessage(),new CpfRecoveryInfo(required("recoveryId",op.recoveryId()),required("recoveryAction",op.recoveryAction())));
        if("BUSINESS_FAILURE".equals(op.resultStatus())) return CpfResult.businessFailure(op.errorCode(),op.errorMessage());
        String code=op.errorCode(); if(code==null) code=switch(op.state()){case CANCELLED->"CPF-ASYNC-CANCELLED";case EXPIRED->"CPF-ASYNC-EXPIRED";default->"CPF-ASYNC-FAILED";};
        return CpfResult.technicalFailure(code,op.errorMessage()==null?op.state().name():op.errorMessage());
    }

    @Override public CpfAsyncOperationStatus cancel(String executionId,String reason){return status(store.requestCancel(required("executionId",executionId),required("reason",reason),clock.instant()));}

    CpfAsyncHandler<?,?> handler(String commandType){return handlersByCommand.values().stream().filter(h->h.commandType().getName().equals(commandType)).findFirst().orElseThrow(()->new IllegalStateException("CPF Async handler가 없습니다: "+commandType));}
    ObjectMapper objectMapper(){return json;}
    CpfAsyncPayloadCodec payloadCodec(){return payloads;}
    CpfExecutionIdGenerator executionIds(){return ids;}

    private CpfAsyncStoredOperation require(String executionId){return store.find(required("executionId",executionId)).orElseThrow(()->new NoSuchElementException("Async execution not found: "+executionId));}
    static CpfAsyncOperationStatus status(CpfAsyncStoredOperation op){return new CpfAsyncOperationStatus(op.executionId(),op.operationId(),op.transactionId(),op.state(),op.submittedAt(),op.startedAt(),op.updatedAt(),op.completedAt(),op.expiresAt(),op.heartbeatAt(),op.leaseOwner(),op.cancellationReason(),op.version());}
    static String required(String name,String value){String v=normalize(value);if(v==null)throw new IllegalArgumentException(name+" is required");return v;}
    static String normalize(String value){if(value==null)return null;String v=value.trim();return v.isEmpty()?null:v;}
}

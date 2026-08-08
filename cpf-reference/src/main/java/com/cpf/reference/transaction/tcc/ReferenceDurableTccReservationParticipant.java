package com.cpf.reference.transaction.tcc;

import com.cpf.core.api.transaction.*;import java.time.Instant;

/** Executable durable business consumer for TCC hold/reservation semantics. */
public final class ReferenceDurableTccReservationParticipant implements CpfTccParticipant<ReferenceTccReservationCommand> {
 private final ReferenceTccReservationStore store;
 public ReferenceDurableTccReservationParticipant(ReferenceTccReservationStore store){this.store=java.util.Objects.requireNonNull(store);}
 @Override public CpfTccResult tryAction(CpfTccContext c,ReferenceTccReservationCommand cmd){
   if(java.time.Instant.now().isAfter(c.deadline())) return CpfTccResult.FAILED;
   var current=store.find(c.transactionId(),c.branchId());if(current.isPresent())return duplicateTry(current.get());
   var r=new ReferenceTccReservationRecord(c.transactionId(),c.branchId(),c.idempotencyKey(),cmd.accountId(),cmd.amount(),ReferenceTccReservationState.TRYING,c.deadline(),0,Instant.now());
   if(!store.createTry(r))return store.find(c.transactionId(),c.branchId()).map(this::duplicateTry).orElse(CpfTccResult.RETRYABLE_FAILURE);
   var persisted=store.find(c.transactionId(),c.branchId()).orElseThrow();
   return store.transition(c.transactionId(),c.branchId(),ReferenceTccReservationState.TRYING,ReferenceTccReservationState.TRIED,persisted.fencingToken())?CpfTccResult.APPLIED:CpfTccResult.RETRYABLE_FAILURE;
 }
 @Override public CpfTccResult confirm(CpfTccContext c,ReferenceTccReservationCommand cmd){
   var r=store.find(c.transactionId(),c.branchId());if(r.isEmpty())return CpfTccResult.RETRYABLE_FAILURE;
   if(java.time.Instant.now().isAfter(c.deadline()) && (r.get().state()==ReferenceTccReservationState.TRYING||r.get().state()==ReferenceTccReservationState.TRIED)){store.markManualReview(c.transactionId(),c.branchId(),"CONFIRM_AFTER_DEADLINE");return CpfTccResult.MANUAL_REVIEW;}
   return switch(r.get().state()){case CONFIRMED->CpfTccResult.ALREADY_APPLIED;case CANCELED,MANUAL_REVIEW->CpfTccResult.HANGING_REJECTED;case UNKNOWN->CpfTccResult.UNKNOWN;case TRYING,TRIED->store.transition(c.transactionId(),c.branchId(),r.get().state(),ReferenceTccReservationState.CONFIRMED,r.get().fencingToken())?CpfTccResult.APPLIED:CpfTccResult.RETRYABLE_FAILURE;};
 }
 @Override public CpfTccResult cancel(CpfTccContext c,ReferenceTccReservationCommand cmd){
   var r=store.find(c.transactionId(),c.branchId());if(r.isEmpty()){
     var empty=new ReferenceTccReservationRecord(c.transactionId(),c.branchId(),c.idempotencyKey(),cmd.accountId(),cmd.amount(),ReferenceTccReservationState.CANCELED,c.deadline(),0,Instant.now());return store.createEmptyRollback(empty)?CpfTccResult.EMPTY_ROLLBACK:CpfTccResult.RETRYABLE_FAILURE;}
   return switch(r.get().state()){case CANCELED->CpfTccResult.ALREADY_APPLIED;case CONFIRMED,MANUAL_REVIEW->CpfTccResult.HANGING_REJECTED;case UNKNOWN->CpfTccResult.UNKNOWN;case TRYING,TRIED->store.transition(c.transactionId(),c.branchId(),r.get().state(),ReferenceTccReservationState.CANCELED,r.get().fencingToken())?CpfTccResult.APPLIED:CpfTccResult.RETRYABLE_FAILURE;};
 }
 private CpfTccResult duplicateTry(ReferenceTccReservationRecord r){return switch(r.state()){case TRYING,TRIED,CONFIRMED->CpfTccResult.ALREADY_APPLIED;case CANCELED,MANUAL_REVIEW->CpfTccResult.HANGING_REJECTED;case UNKNOWN->CpfTccResult.UNKNOWN;};}
}

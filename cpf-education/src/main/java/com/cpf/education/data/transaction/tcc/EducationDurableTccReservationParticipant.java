package com.cpf.education.data.transaction.tcc;
import com.cpf.core.api.transaction.*;import java.time.Instant;

/** Executable durable business consumer for TCC hold/reservation semantics. */
/** EducationDurableTccReservationParticipant 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public final class EducationDurableTccReservationParticipant implements CpfTccParticipant<EducationTccReservationCommand> {
 private final EducationTccReservationStore store;
 public EducationDurableTccReservationParticipant(EducationTccReservationStore store){this.store=java.util.Objects.requireNonNull(store);}
 @Override public CpfTccResult tryAction(CpfTccContext c,EducationTccReservationCommand cmd){
   if(java.time.Instant.now().isAfter(c.deadline())) return CpfTccResult.FAILED;
   var current=store.find(c.transactionId(),c.branchId());if(current.isPresent())return duplicateTry(current.get());
   var r=new EducationTccReservationRecord(c.transactionId(),c.branchId(),c.idempotencyKey(),cmd.accountId(),cmd.amount(),EducationTccReservationState.TRYING,c.deadline(),0,Instant.now());
   if(!store.createTry(r))return store.find(c.transactionId(),c.branchId()).map(this::duplicateTry).orElse(CpfTccResult.RETRYABLE_FAILURE);
   var persisted=store.find(c.transactionId(),c.branchId()).orElseThrow();
   return store.transition(c.transactionId(),c.branchId(),EducationTccReservationState.TRYING,EducationTccReservationState.TRIED,persisted.fencingToken())?CpfTccResult.APPLIED:CpfTccResult.RETRYABLE_FAILURE;
 }
 @Override public CpfTccResult confirm(CpfTccContext c,EducationTccReservationCommand cmd){
   var r=store.find(c.transactionId(),c.branchId());if(r.isEmpty())return CpfTccResult.RETRYABLE_FAILURE;
   if(java.time.Instant.now().isAfter(c.deadline()) && (r.get().state()==EducationTccReservationState.TRYING||r.get().state()==EducationTccReservationState.TRIED)){store.markManualReview(c.transactionId(),c.branchId(),"CONFIRM_AFTER_DEADLINE");return CpfTccResult.MANUAL_REVIEW;}
   return switch(r.get().state()){case CONFIRMED->CpfTccResult.ALREADY_APPLIED;case CANCELED,MANUAL_REVIEW->CpfTccResult.HANGING_REJECTED;case UNKNOWN->CpfTccResult.UNKNOWN;case TRYING,TRIED->store.transition(c.transactionId(),c.branchId(),r.get().state(),EducationTccReservationState.CONFIRMED,r.get().fencingToken())?CpfTccResult.APPLIED:CpfTccResult.RETRYABLE_FAILURE;};
 }
 @Override public CpfTccResult cancel(CpfTccContext c,EducationTccReservationCommand cmd){
   var r=store.find(c.transactionId(),c.branchId());if(r.isEmpty()){
     // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
     var empty=new EducationTccReservationRecord(c.transactionId(),c.branchId(),c.idempotencyKey(),cmd.accountId(),cmd.amount(),EducationTccReservationState.CANCELED,c.deadline(),0,Instant.now());return store.createEmptyRollback(empty)?CpfTccResult.EMPTY_ROLLBACK:CpfTccResult.RETRYABLE_FAILURE;}
   return switch(r.get().state()){case CANCELED->CpfTccResult.ALREADY_APPLIED;case CONFIRMED,MANUAL_REVIEW->CpfTccResult.HANGING_REJECTED;case UNKNOWN->CpfTccResult.UNKNOWN;case TRYING,TRIED->store.transition(c.transactionId(),c.branchId(),r.get().state(),EducationTccReservationState.CANCELED,r.get().fencingToken())?CpfTccResult.APPLIED:CpfTccResult.RETRYABLE_FAILURE;};
 }
 private CpfTccResult duplicateTry(EducationTccReservationRecord r){return switch(r.state()){case TRYING,TRIED,CONFIRMED->CpfTccResult.ALREADY_APPLIED;case CANCELED,MANUAL_REVIEW->CpfTccResult.HANGING_REJECTED;case UNKNOWN->CpfTccResult.UNKNOWN;};}
}

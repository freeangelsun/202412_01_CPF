package com.cpf.reference.transaction.tcc;
import java.util.List;import java.util.Optional;
public interface ReferenceTccReservationStore {
 Optional<ReferenceTccReservationRecord> find(String transactionId,String branchId);
 boolean createTry(ReferenceTccReservationRecord record);
 boolean createEmptyRollback(ReferenceTccReservationRecord record);
 boolean transition(String transactionId,String branchId,ReferenceTccReservationState expected,ReferenceTccReservationState next,long expectedFence);
 boolean markManualReview(String transactionId,String branchId,String reason);
 List<ReferenceTccReservationRecord> findExpired(int limit);
}

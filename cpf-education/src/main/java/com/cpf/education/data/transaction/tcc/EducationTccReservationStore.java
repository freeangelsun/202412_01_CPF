package com.cpf.education.data.transaction.tcc;
import java.util.List;import java.util.Optional;
/** EducationTccReservationStore 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface EducationTccReservationStore {
 Optional<EducationTccReservationRecord> find(String transactionId,String branchId);
 boolean createTry(EducationTccReservationRecord record);
 boolean createEmptyRollback(EducationTccReservationRecord record);
 boolean transition(String transactionId,String branchId,EducationTccReservationState expected,EducationTccReservationState next,long expectedFence);
 boolean markManualReview(String transactionId,String branchId,String reason);
 List<EducationTccReservationRecord> findExpired(int limit);
}

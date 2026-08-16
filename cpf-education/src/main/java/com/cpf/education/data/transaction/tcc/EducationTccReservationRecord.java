package com.cpf.education.data.transaction.tcc;
import java.math.BigDecimal;import java.time.Instant;
/** EducationTccReservationRecord 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record EducationTccReservationRecord(String transactionId,String branchId,String idempotencyKey,String accountId,BigDecimal amount,EducationTccReservationState state,Instant deadline,long fencingToken,Instant updatedAt) {}

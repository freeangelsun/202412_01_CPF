package com.cpf.reference.transaction.tcc;
import java.math.BigDecimal;import java.time.Instant;
public record ReferenceTccReservationRecord(String transactionId,String branchId,String idempotencyKey,String accountId,BigDecimal amount,ReferenceTccReservationState state,Instant deadline,long fencingToken,Instant updatedAt) {}

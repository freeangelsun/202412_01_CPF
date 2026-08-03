package com.cpf.batch.api;
import java.time.Instant;
import java.util.List;
public record RuntimeCommand(
 String commandId,String idempotencyKey,String commandType,List<String> targetIds,long expectedVersion,
 String requestedBy,String reason,String approvalPolicyVersion,String approvalRequestId,String approvedBy,
 Instant expiresAt) {}

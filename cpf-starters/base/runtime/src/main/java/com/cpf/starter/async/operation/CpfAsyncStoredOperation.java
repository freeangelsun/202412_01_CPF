package com.cpf.starter.async.operation;
import com.cpf.core.api.async.CpfAsyncState;
import java.time.Instant;
/** Runtime Store 내부 record입니다. Public API에 노출하지 않습니다. */
public record CpfAsyncStoredOperation(
 String executionId,String operationId,String transactionId,String idempotencyKey,
 String commandType,String commandPayload,String contextPayload,String resultType,String resultPayload,
 CpfAsyncState state,String resultStatus,String errorCode,String errorMessage,String recoveryId,String recoveryAction,
 Instant submittedAt,Instant startedAt,Instant updatedAt,Instant completedAt,Instant expiresAt,
 Instant heartbeatAt,String leaseOwner,Instant leaseUntil,String cancellationReason,long version) { }

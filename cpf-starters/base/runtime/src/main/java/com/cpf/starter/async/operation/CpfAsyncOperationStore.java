package com.cpf.starter.async.operation;
import java.time.Instant; import java.util.Optional;
/** Async Runtime persistence SPI. JDBC 등 Provider가 멱등/lease/fencing을 구현합니다. */
public interface CpfAsyncOperationStore {
 CpfAsyncStoredOperation insertOrGet(CpfAsyncStoredOperation operation);
 Optional<CpfAsyncStoredOperation> find(String executionId);
 Optional<CpfAsyncStoredOperation> claimNext(String owner, Instant now, Instant leaseUntil);
 boolean heartbeat(String executionId,String owner,long expectedVersion,Instant now,Instant leaseUntil);
 CpfAsyncStoredOperation requestCancel(String executionId,String reason,Instant now);
 boolean cancellationRequested(String executionId);
 CpfAsyncStoredOperation complete(String executionId,String owner,long expectedVersion,String resultStatus,String resultType,String resultPayload,String errorCode,String errorMessage,String recoveryId,String recoveryAction,Instant now);
 int expireDue(Instant now);
}

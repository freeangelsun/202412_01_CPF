package com.cpf.batch.execution;

import java.time.Instant;

/** Kafka at-least-once 전달을 side effect 기준 exactly-once로 수렴시키는 Durable Ledger입니다. */
public interface CpfBatchRemoteMessageLedger {
    enum Claim { CLAIMED, DUPLICATE_COMPLETE, IN_PROGRESS }
    Claim claim(String direction,String messageId,String payloadSha256,Instant expiresAt,String ownerId);
    void complete(String direction,String messageId,String ownerId);
    void fail(String direction,String messageId,String ownerId,String errorCode);
}

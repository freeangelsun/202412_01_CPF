package com.cpf.batch.execution;

import java.time.Instant;

/** Kafka at-least-once 전달을 side effect 기준 exactly-once로 수렴시키는 Durable Ledger입니다. */
public interface CpfBatchRemoteMessageLedger {
    enum Claim {
        CLAIMED,
        DUPLICATE_COMPLETE,
        IN_PROGRESS,
        /** Side effect 결과가 불명확해 운영 Reconcile 전에는 재실행할 수 없습니다. */
        UNKNOWN_RECONCILE_REQUIRED
    }
    Claim claim(String direction,String messageId,String payloadSha256,Instant expiresAt,String ownerId);
    void complete(String direction,String messageId,String ownerId);
    void fail(String direction,String messageId,String ownerId,String errorCode);
    /** 결과불명을 durable 상태로 보존하고 자동 재전달을 차단합니다. */
    void unknown(String direction,String messageId,String ownerId,String errorCode);
}

package member.sample.model;

import java.time.Instant;

/** 같은 key/hash replay와 다른 hash conflict를 판정하는 durable ledger 모델입니다. */
public record SampleIdempotencyRecord(
        String idempotencyKey, String operationCode, String requestHash,
        long sampleItemId, long resultVersion, String deletedYn,
        String transactionId, Instant createdAt) { }

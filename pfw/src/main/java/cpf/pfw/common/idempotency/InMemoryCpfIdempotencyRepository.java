package cpf.pfw.common.idempotency;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 외부 DB 없이 PFW 멱등 엔진을 교육·단위 테스트할 때 사용하는 결정적 reference adapter입니다.
 *
 * <p>운영 영속성 완료 근거로 사용하지 않으며 운영 profile은 JDBC adapter를 사용해야 합니다.</p>
 */
public class InMemoryCpfIdempotencyRepository implements CpfIdempotencyPort {
    private final ConcurrentMap<String, CpfIdempotencyRecord> records = new ConcurrentHashMap<>();

    @Override
    public boolean reserve(CpfIdempotencyRecord record) {
        return records.putIfAbsent(key(record.scope(), record.idempotencyKey()), record) == null;
    }

    @Override
    public Optional<CpfIdempotencyRecord> find(String scope, String idempotencyKey) {
        return Optional.ofNullable(records.get(key(scope, idempotencyKey)));
    }

    @Override
    public void complete(
            String scope,
            String idempotencyKey,
            String status,
            String storedResponse,
            boolean retryAllowed) {
        records.computeIfPresent(key(scope, idempotencyKey), (ignored, current) -> new CpfIdempotencyRecord(
                current.scope(),
                current.idempotencyKey(),
                current.requestHash(),
                current.payloadHash(),
                status,
                storedResponse,
                retryAllowed,
                current.createdAt(),
                Instant.now(),
                current.expiresAt()));
    }

    @Override
    public boolean restart(
            String scope,
            String idempotencyKey,
            String requestHash,
            String payloadHash,
            Instant expiresAt) {
        String recordKey = key(scope, idempotencyKey);
        boolean[] restarted = {false};
        records.computeIfPresent(recordKey, (ignored, current) -> {
            CpfIdempotencyStatus status = CpfIdempotencyStatus.from(current.status());
            boolean retryable = status == CpfIdempotencyStatus.EXPIRED
                    || ((status == CpfIdempotencyStatus.FAILED || status == CpfIdempotencyStatus.UNKNOWN)
                    && current.retryAllowed());
            if (!retryable
                    || !current.sameRequest(requestHash)
                    || !java.util.Objects.equals(current.payloadHash(), payloadHash)) {
                return current;
            }
            restarted[0] = true;
            return new CpfIdempotencyRecord(
                    current.scope(),
                    current.idempotencyKey(),
                    current.requestHash(),
                    current.payloadHash(),
                    CpfIdempotencyStatus.PROCESSING.name(),
                    null,
                    false,
                    current.createdAt(),
                    null,
                    expiresAt);
        });
        return restarted[0];
    }

    @Override
    public void expire(String scope, String idempotencyKey) {
        complete(scope, idempotencyKey, CpfIdempotencyStatus.EXPIRED.name(), null, true);
    }

    @Override
    public int expireBefore(Instant now, int limit) {
        Instant cutoff = now == null ? Instant.now() : now;
        int max = Math.max(1, Math.min(limit, 1000));
        int expired = 0;
        for (CpfIdempotencyRecord record : records.values()) {
            if (expired >= max) {
                break;
            }
            if (CpfIdempotencyStatus.from(record.status()) == CpfIdempotencyStatus.PROCESSING
                    && record.expiresAt() != null
                    && !record.expiresAt().isAfter(cutoff)) {
                expire(record.scope(), record.idempotencyKey());
                expired++;
            }
        }
        return expired;
    }

    private String key(String scope, String idempotencyKey) {
        return scope + '\u001f' + idempotencyKey;
    }
}

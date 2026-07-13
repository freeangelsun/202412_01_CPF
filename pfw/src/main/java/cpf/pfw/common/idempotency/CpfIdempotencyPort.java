package cpf.pfw.common.idempotency;

import java.time.Instant;
import java.util.Optional;

/**
 * PFW 중복 처리 저장소 표준 포트입니다.
 */
public interface CpfIdempotencyPort {

    /**
     * 처리 시작 상태를 저장합니다.
     *
     * @return 같은 scope/key가 이미 존재하면 false, 신규 저장이면 true
     */
    boolean reserve(CpfIdempotencyRecord record);

    Optional<CpfIdempotencyRecord> find(String scope, String idempotencyKey);

    void complete(String scope, String idempotencyKey, String status, String storedResponse, boolean retryAllowed);

    /**
     * 실패, 결과 미확정, 만료 상태를 새 처리로 원자적으로 전환합니다.
     *
     * @return 현재 상태와 hash가 재시도 조건을 만족해 PROCESSING으로 전환되면 true
     */
    boolean restart(
            String scope,
            String idempotencyKey,
            String requestHash,
            String payloadHash,
            Instant expiresAt);

    void expire(String scope, String idempotencyKey);

    /**
     * 만료 시각이 지난 처리 중 기록을 EXPIRED 상태로 정리합니다.
     */
    int expireBefore(Instant now, int limit);
}

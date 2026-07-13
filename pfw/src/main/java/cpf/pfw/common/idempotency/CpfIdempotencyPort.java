package cpf.pfw.common.idempotency;

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

    void expire(String scope, String idempotencyKey);
}

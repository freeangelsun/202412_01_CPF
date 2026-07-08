package cpf.pfw.common.runtime;

import java.time.Instant;

/**
 * 분산 lock 획득 결과입니다.
 */
public record CpfLockHandle(
        String lockKey,
        String ownerId,
        Instant acquiredAt,
        Instant expiresAt) {

    public CpfLockHandle {
        acquiredAt = acquiredAt == null ? Instant.now() : acquiredAt;
    }
}

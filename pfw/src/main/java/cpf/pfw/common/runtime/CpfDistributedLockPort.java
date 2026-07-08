package cpf.pfw.common.runtime;

import java.time.Duration;
import java.util.Optional;

/**
 * 다중 WAS/worker 환경의 중복 실행 방지 port입니다.
 */
public interface CpfDistributedLockPort {

    Optional<CpfLockHandle> tryAcquire(String lockKey, String ownerId, Duration ttl);

    boolean release(CpfLockHandle lockHandle);

    boolean isLocked(String lockKey);
}

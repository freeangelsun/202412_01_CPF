package com.cpf.core.api.cache;

import java.time.Duration;
import java.util.Optional;

/** Cache stampede와 운영 위험 조치를 보호하는 fencing lock SPI입니다. */
public interface CpfDistributedLockPort {
    Optional<CpfLockToken> tryAcquire(String lockName, Duration wait, Duration lease);
    boolean release(CpfLockToken token);
}

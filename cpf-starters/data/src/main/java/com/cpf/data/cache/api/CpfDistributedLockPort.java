package com.cpf.data.cache.api;
import java.time.Duration;
import java.util.Optional;
/** CpfDistributedLockPort 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfDistributedLockPort {
    Optional<CpfLockToken> tryAcquire(String lockName, Duration wait, Duration lease);
    boolean release(CpfLockToken token);
}

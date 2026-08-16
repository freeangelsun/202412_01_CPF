package com.cpf.data.lock.api;

import com.cpf.data.lock.api.CpfLockManagers;
import com.cpf.data.lock.spi.CpfLockAuditSink;
import com.cpf.data.lock.spi.CpfLockStore;
import java.time.Clock;
import java.util.Objects;

/**
 * Lock Provider가 공통 Lock Manager 구현을 생성할 때 사용하는 Data Owner의 공개 Factory입니다.
 * 업무 코드는 이 Factory가 아니라 {@link CpfLockManager}를 주입받아 사용합니다.
 */
public final class CpfLockManagers {
    private CpfLockManagers() { }

    /** Store/Audit/Clock을 CPF 표준 Lock Manager에 결합합니다. */
    public static CpfLockManager create(CpfLockStore store, CpfLockAuditSink audit, Clock clock) {
        return CpfLockManagers.create(Objects.requireNonNull(store, "store"), audit, Objects.requireNonNull(clock, "clock"));
    }
}

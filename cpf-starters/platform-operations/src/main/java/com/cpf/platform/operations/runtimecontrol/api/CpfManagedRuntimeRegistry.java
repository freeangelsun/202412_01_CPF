package com.cpf.platform.operations.runtimecontrol.api;

import java.time.Duration;
import java.util.List;

/**
 * Runtime lifecycle의 단일 중앙 Authority 계약입니다.
 *
 * <p>등록/lease/fencing은 CPF Runtime Control Agent가 담당하고, 업무 Runtime은 이 Port를 통해
 * 존재 확인, desired-state CAS, 조회만 수행합니다. 개별 Runtime DB에 별도 master registry를
 * 만들지 않습니다.</p>
 */
public interface CpfManagedRuntimeRegistry {
    CpfManagedRuntimeSnapshot snapshot(String instanceId);

    List<CpfManagedRuntimeSnapshot> list(Duration staleAfter);

    long updateDesiredState(String instanceId, String desiredState, long expectedVersion);

    void reportActualState(String instanceId, String actualState);
}

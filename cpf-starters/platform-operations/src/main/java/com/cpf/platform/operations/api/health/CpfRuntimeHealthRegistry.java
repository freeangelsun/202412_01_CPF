package com.cpf.platform.operations.api.health;
import java.util.List;
import java.util.Optional;
/** 다중 인스턴스 Health를 집계하는 저장소 계약입니다. */
public interface CpfRuntimeHealthRegistry {
    void upsert(CpfRuntimeHealth snapshot);
    Optional<CpfRuntimeHealth> find(String systemId, String instanceId);
    List<CpfRuntimeHealth> list();
}

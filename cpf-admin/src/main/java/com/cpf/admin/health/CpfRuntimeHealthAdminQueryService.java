package com.cpf.admin.health;
import com.cpf.platform.operations.api.health.CpfRuntimeHealth;
import com.cpf.platform.operations.api.health.CpfRuntimeHealthRegistry;
import java.util.List;
import java.util.Optional;
/** ADM에서 시스템/인스턴스별 Health를 읽는 실제 Consumer입니다. */
public final class CpfRuntimeHealthAdminQueryService {
    private final CpfRuntimeHealthRegistry registry;
    public CpfRuntimeHealthAdminQueryService(CpfRuntimeHealthRegistry registry) { this.registry = registry; }
    public List<CpfRuntimeHealth> list() { return registry.list(); }
    public Optional<CpfRuntimeHealth> detail(String systemId, String instanceId) { return registry.find(systemId, instanceId); }
}

package cpf.pfw.common.servicecall;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * CPF 서비스 기본 정보를 조회하는 레지스트리입니다.
 */
public class CpfServiceRegistry {
    private final CpfServiceRegistryRepository repository;

    public CpfServiceRegistry(CpfServiceRegistryRepository repository) {
        this.repository = repository;
    }

    public List<Map<String, Object>> findServices(String serviceId, String useYn, int limit) {
        return repository.findServices(serviceId, useYn, limit);
    }

    public Optional<Map<String, Object>> findService(String serviceId) {
        return repository.findServices(serviceId, "Y", 1).stream().findFirst();
    }
}

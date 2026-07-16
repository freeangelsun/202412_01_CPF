package cpf.pfw.common.gateway;

import cpf.pfw.common.execution.CpfExecutionCatalogPort;
import cpf.pfw.common.execution.CpfExecutionDefinition;
import cpf.pfw.common.execution.CpfExecutionType;
import cpf.pfw.common.execution.CpfStandardExecutionId;

import java.util.LinkedHashMap;
import java.util.Map;

/** 실행 카탈로그에서 공개 온라인 route snapshot을 생성합니다. */
public final class CpfGatewayRouteCatalog {
    private final CpfExecutionCatalogPort executionCatalog;

    public CpfGatewayRouteCatalog(CpfExecutionCatalogPort executionCatalog) {
        this.executionCatalog = executionCatalog;
    }

    public Map<String, CpfGatewayRoute> loadPublicRoutes() {
        Map<String, CpfGatewayRoute> routes = new LinkedHashMap<>();
        for (CpfExecutionDefinition definition : executionCatalog.findAll()) {
            if (definition.executionType() != CpfExecutionType.ONLINE
                    || !definition.gatewayAllowed()
                    || !"PUBLIC".equalsIgnoreCase(definition.visibility())
                    || definition.endpoint() == null
                    || definition.endpoint().isBlank()) {
                continue;
            }
            CpfGatewayRoute previous = routes.putIfAbsent(
                    definition.standardExecutionId(), CpfGatewayRoute.from(definition));
            if (previous != null) {
                throw new IllegalStateException("Gateway 공개 route ID가 중복되었습니다. id="
                        + definition.standardExecutionId());
            }
        }
        return Map.copyOf(routes);
    }

    public CpfGatewayRoute resolve(Map<String, CpfGatewayRoute> snapshot, String executionId) {
        if (!CpfStandardExecutionId.isValid(executionId)) {
            if (CpfStandardExecutionId.isLegacy(executionId)) {
                CpfExecutionDefinition resolved = executionCatalog.resolve(executionId)
                        .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 구형 실행 ID입니다."));
                executionId = resolved.standardExecutionId();
            } else {
                throw new IllegalArgumentException("10자리 CPF 표준 실행 ID가 필요합니다.");
            }
        }
        CpfStandardExecutionId parsed = CpfStandardExecutionId.parse(executionId);
        if (parsed.type() != CpfExecutionType.ONLINE) {
            throw new IllegalArgumentException("공개 Gateway는 O 유형 온라인 거래만 실행할 수 있습니다.");
        }
        CpfGatewayRoute route = snapshot.get(executionId);
        if (route == null) {
            throw new IllegalArgumentException("사용 가능한 Gateway route가 없습니다. id=" + executionId);
        }
        return route;
    }
}

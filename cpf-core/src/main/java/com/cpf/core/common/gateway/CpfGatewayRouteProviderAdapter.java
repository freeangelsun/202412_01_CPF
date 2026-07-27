package com.cpf.core.common.gateway;

import com.cpf.core.api.gateway.CpfGatewayRoute;
import com.cpf.core.api.gateway.CpfGatewayRouteProvider;
import com.cpf.core.common.execution.CpfExecutionCatalogPort;
import com.cpf.core.common.execution.CpfExecutionDefinition;
import com.cpf.core.common.execution.CpfExecutionType;
import com.cpf.core.common.execution.CpfStandardExecutionId;

import java.util.LinkedHashMap;
import java.util.Map;

/** Core 실행 카탈로그를 Gateway Public SPI로 변환하는 내부 adapter입니다. */
public final class CpfGatewayRouteProviderAdapter implements CpfGatewayRouteProvider {
    private final CpfExecutionCatalogPort executionCatalog;
    public CpfGatewayRouteProviderAdapter(CpfExecutionCatalogPort executionCatalog){this.executionCatalog=executionCatalog;}
    @Override
    public Map<String,CpfGatewayRoute> loadPublicRoutes(){
        Map<String,CpfGatewayRoute> routes=new LinkedHashMap<>();
        for(CpfExecutionDefinition d:executionCatalog.findAll()){
            if(d.executionType()!=CpfExecutionType.ONLINE||!d.gatewayAllowed()||!"PUBLIC".equalsIgnoreCase(d.visibility())||d.endpoint()==null||d.endpoint().isBlank()) continue;
            CpfGatewayRoute route=route(d);
            if(routes.putIfAbsent(d.standardExecutionId(),route)!=null) throw new IllegalStateException("Gateway 공개 route ID가 중복되었습니다. id="+d.standardExecutionId());
        }
        return Map.copyOf(routes);
    }
    @Override
    public CpfGatewayRoute resolve(Map<String,CpfGatewayRoute> snapshot,String executionId){
        if(!CpfStandardExecutionId.isValid(executionId)){
            if(CpfStandardExecutionId.isLegacy(executionId)){
                CpfExecutionDefinition d=executionCatalog.resolve(executionId).orElseThrow(()->new IllegalArgumentException("등록되지 않은 구형 실행 ID입니다."));
                executionId=d.standardExecutionId();
            }else throw new IllegalArgumentException("10자리 CPF 표준 실행 ID가 필요합니다.");
        }
        CpfStandardExecutionId parsed=CpfStandardExecutionId.parse(executionId);
        if(parsed.type()!=CpfExecutionType.ONLINE) throw new IllegalArgumentException("공개 Gateway는 O 유형 온라인 거래만 실행할 수 있습니다.");
        CpfGatewayRoute route=snapshot.get(executionId);
        if(route==null) throw new IllegalArgumentException("사용 가능한 Gateway route가 없습니다. id="+executionId);
        return route;
    }
    private static CpfGatewayRoute route(CpfExecutionDefinition d){
        return new CpfGatewayRoute(d.standardExecutionId(),d.sourceModule(),d.httpMethod(),d.endpoint(),d.operationId(),d.requiredPermission(),d.auditReasonRequired(),d.sourceVersion());
    }
}

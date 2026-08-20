package com.cpf.admin.opr.service;

import com.cpf.admin.config.AdmPersistencePolicy;
import com.cpf.admin.opr.security.AdmApiPermissionPolicy;
import com.cpf.core.api.error.CpfBusinessException;
import com.cpf.core.api.error.CpfErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 현재 운영자의 API Permission을 OpenAPI operationId projection으로 변환합니다.
 * Button ID와 operationId를 섞지 않고, Browser에는 Backend가 실제 허용하는 operation hint만 제공합니다.
 * 최종 권한 판정자는 항상 {@link com.cpf.admin.opr.filter.AdmApiAuthFilter}입니다.
 */
@Service
public class AdmOperationPermissionProjectionService {
    private final JdbcTemplate admJdbcTemplate;
    private final AdmPersistencePolicy persistencePolicy;
    private final ObjectProvider<RequestMappingHandlerMapping> handlerMappingProvider;

    public AdmOperationPermissionProjectionService(
            @Qualifier("admJdbcTemplate") JdbcTemplate admJdbcTemplate,
            AdmPersistencePolicy persistencePolicy,
            @Qualifier("requestMappingHandlerMapping") ObjectProvider<RequestMappingHandlerMapping> handlerMappingProvider) {
        this.admJdbcTemplate = admJdbcTemplate;
        this.persistencePolicy = persistencePolicy;
        this.handlerMappingProvider = handlerMappingProvider;
    }

    public List<String> findAllowedOperationIds(List<String> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }
        List<OperationRoute> routes = discoverOperationRoutes();
        if (persistencePolicy.memoryEnabled()) {
            // MEMORY는 local/test 전용입니다. ADM_ADMIN만 전체 operation hint를 받고,
            // 다른 role은 Button/Menu projection과 Backend filter의 fail-closed 정책을 사용합니다.
            if (roleIds.contains("ADM_ADMIN")) {
                return routes.stream().map(OperationRoute::operationId).distinct().sorted().toList();
            }
            return List.of();
        }
        List<AdmApiPermissionPolicy.Rule> rules = loadRules(roleIds);
        Set<String> allowed = new LinkedHashSet<>();
        for (OperationRoute route : routes) {
            if (AdmApiPermissionPolicy.evaluate(rules, route.httpMethod(), route.path()).orElse(false)) {
                allowed.add(route.operationId());
            }
        }
        return allowed.stream().sorted().toList();
    }

    private List<AdmApiPermissionPolicy.Rule> loadRules(List<String> roleIds) {
        String placeholders = String.join(",", roleIds.stream().map(role -> "?").toList());
        try {
            List<Map<String, Object>> rows = admJdbcTemplate.queryForList("""
                    SELECT a.HTTP_METHOD, a.API_PATH, ra.ALLOW_YN
                      FROM adm_api_permission a
                      LEFT JOIN adm_role_api_permission ra
                        ON ra.API_PERMISSION_ID = a.API_PERMISSION_ID
                       AND ra.ROLE_ID IN (%s)
                     WHERE a.USE_YN = 'Y'
                    """.formatted(placeholders), roleIds.toArray());
            return rows.stream().map(row -> new AdmApiPermissionPolicy.Rule(
                    String.valueOf(row.get("HTTP_METHOD")),
                    String.valueOf(row.get("API_PATH")),
                    row.get("ALLOW_YN") == null ? "N" : String.valueOf(row.get("ALLOW_YN"))))
                    .toList();
        } catch (DataAccessException ex) {
            throw new CpfBusinessException(
                    CpfErrorCode.INFRASTRUCTURE_UNAVAILABLE,
                    "ADM API 권한 저장소를 사용할 수 없습니다.",
                    Map.of("0", "adm_api_permission.operationProjection", "1", ex.getClass().getSimpleName()));
        }
    }

    private List<OperationRoute> discoverOperationRoutes() {
        RequestMappingHandlerMapping mapping = handlerMappingProvider.getIfAvailable();
        if (mapping == null) {
            if (persistencePolicy.databaseRequired()) {
                throw new CpfBusinessException(
                        CpfErrorCode.INFRASTRUCTURE_UNAVAILABLE,
                        "ADM operation mapping을 사용할 수 없습니다.");
            }
            return List.of();
        }
        List<OperationRoute> routes = new ArrayList<>();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : mapping.getHandlerMethods().entrySet()) {
            HandlerMethod handler = entry.getValue();
            if (!handler.getBeanType().getPackageName().startsWith("com.cpf.admin")) {
                continue;
            }
            Operation operation = handler.getMethodAnnotation(Operation.class);
            if (operation == null || operation.operationId() == null || operation.operationId().isBlank()) {
                continue;
            }
            Set<String> paths = entry.getKey().getPatternValues();
            Set<org.springframework.web.bind.annotation.RequestMethod> methods = entry.getKey().getMethodsCondition().getMethods();
            for (String path : paths) {
                if (path == null || !path.startsWith("/adm/api/")) {
                    continue;
                }
                if (methods.isEmpty()) {
                    routes.add(new OperationRoute(operation.operationId().trim(), "ANY", path));
                } else {
                    for (org.springframework.web.bind.annotation.RequestMethod method : methods) {
                        routes.add(new OperationRoute(operation.operationId().trim(), method.name(), path));
                    }
                }
            }
        }
        return routes.stream()
                .sorted(Comparator.comparing(OperationRoute::operationId)
                        .thenComparing(OperationRoute::httpMethod)
                        .thenComparing(OperationRoute::path))
                .toList();
    }

    record OperationRoute(String operationId, String httpMethod, String path) {
    }
}

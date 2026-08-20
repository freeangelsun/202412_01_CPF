package com.cpf.admin.opr.filter;

import com.cpf.admin.config.AdmPersistencePolicy;
import com.cpf.admin.config.AdmSecurityProperties;
import com.cpf.admin.opr.dto.AdmSession;
import com.cpf.admin.opr.service.AdmSessionService;
import com.cpf.admin.opr.security.AdmApiPermissionPolicy;
import com.cpf.core.api.error.CpfBusinessException;
import com.cpf.core.api.error.CpfErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ADM REST API 인증과 서버 권한검사를 담당하는 필터입니다.
 *
 * <p>ADM 화면에서 메뉴나 버튼을 숨겨도 API를 직접 호출할 수 있으므로 서버에서 메뉴 권한과 버튼 권한을 다시 검사합니다.
 * 제품 모드의 API 권한 정본은 {@code adm_api_permission}/{@code adm_role_api_permission}이며,
 * 이 클래스의 경로 Map은 DB를 사용하지 않는 명시적 MEMORY 모드의 메뉴 권한 fallback에만 사용합니다.</p>
 */
@Component
public class AdmApiAuthFilter extends OncePerRequestFilter {
    private static final Map<String, String> MENU_BY_PATH_PREFIX = new LinkedHashMap<>();
    private static final Map<String, String> BUTTON_BY_METHOD_PATH_PREFIX = new LinkedHashMap<>();

    static {
        MENU_BY_PATH_PREFIX.put("/adm/api/v1/system", "DASHBOARD");
        MENU_BY_PATH_PREFIX.put("/adm/api/capability-management", "CAPABILITY_FLEET");
        MENU_BY_PATH_PREFIX.put("/adm/api/logs", "LOG_LIST");
        MENU_BY_PATH_PREFIX.put("/adm/api/transaction-groups", "LOG_LIST");
        MENU_BY_PATH_PREFIX.put("/adm/api/observability", "LOG_LIST");
        MENU_BY_PATH_PREFIX.put("/adm/api/service-registry", "SERVICE_REGISTRY");
        MENU_BY_PATH_PREFIX.put("/adm/api/reliability", "RELIABILITY");
        MENU_BY_PATH_PREFIX.put("/adm/api/transactions", "TRANSACTION_META");
        MENU_BY_PATH_PREFIX.put("/adm/api/standard-executions", "STANDARD_EXECUTION");
        MENU_BY_PATH_PREFIX.put("/adm/api/channels", "CHANNEL_POLICY");
        MENU_BY_PATH_PREFIX.put("/adm/api/remote-logs", "REMOTE_LOG");
        MENU_BY_PATH_PREFIX.put("/adm/api/audit-logs", "AUDIT_LOG");
        MENU_BY_PATH_PREFIX.put("/adm/api/business-calendars", "BUSINESS_CALENDAR");
        MENU_BY_PATH_PREFIX.put("/adm/api/batch-runtime", "BATCH_RUNTIME");
        MENU_BY_PATH_PREFIX.put("/adm/api/batch", "BATCH");
        MENU_BY_PATH_PREFIX.put("/adm/api/center-cut", "BATCH");
        MENU_BY_PATH_PREFIX.put("/adm/api/integration-closure", "INTEGRATION");
        MENU_BY_PATH_PREFIX.put("/adm/api/servers", "RUNTIME_CONTROL");
        MENU_BY_PATH_PREFIX.put("/adm/api/runtime-inventory", "RUNTIME_CONTROL");
        MENU_BY_PATH_PREFIX.put("/adm/api/runtime-control", "RUNTIME_CONTROL");
        MENU_BY_PATH_PREFIX.put("/adm/api/maintenance", "MAINTENANCE");
        MENU_BY_PATH_PREFIX.put("/adm/api/incidents", "INCIDENT");
        MENU_BY_PATH_PREFIX.put("/adm/api/notifications", "NOTIFICATION");
        MENU_BY_PATH_PREFIX.put("/adm/api/downloads", "DOWNLOAD");
        MENU_BY_PATH_PREFIX.put("/adm/api/file-jobs", "FILE_JOB");
        MENU_BY_PATH_PREFIX.put("/adm/api/gateway-registry", "GATEWAY_DASHBOARD");
        MENU_BY_PATH_PREFIX.put("/adm/api/cache", "CACHE");
        MENU_BY_PATH_PREFIX.put("/adm/api/messages", "MESSAGE");
        MENU_BY_PATH_PREFIX.put("/adm/api/codes", "CODE");
        MENU_BY_PATH_PREFIX.put("/adm/api/response-codes", "RESPONSE_CODE");
        MENU_BY_PATH_PREFIX.put("/adm/api/configs", "CONFIG");
        MENU_BY_PATH_PREFIX.put("/adm/api/log-level", "DYNAMIC_LOG");
        MENU_BY_PATH_PREFIX.put("/adm/api/log-policy-audits", "LOG_POLICY");
        MENU_BY_PATH_PREFIX.put("/adm/api/log-policies", "LOG_POLICY");
        MENU_BY_PATH_PREFIX.put("/adm/api/secrets", "SECRET");
        MENU_BY_PATH_PREFIX.put("/adm/api/approvals", "APPROVAL");
        MENU_BY_PATH_PREFIX.put("/adm/api/break-glass", "BREAK_GLASS");
        MENU_BY_PATH_PREFIX.put("/adm/api/security", "SECURITY");
        MENU_BY_PATH_PREFIX.put("/adm/api/permissions", "PERMISSION");
        MENU_BY_PATH_PREFIX.put("/adm/api/operators", "OPERATOR");

        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/audit-logs/deliveries", "AUDIT_LOG_RETRY");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/capability-management", "CAPABILITY_FLEET_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/logs", "LOG_LIST_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/transaction-groups", "LOG_LIST_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/transaction-groups/subject-search", "LOG_LIST_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/observability", "LOG_LIST_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/service-registry", "SERVICE_REGISTRY_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/service-registry", "SERVICE_REGISTRY_WRITE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("DELETE /adm/api/service-registry", "SERVICE_REGISTRY_DELETE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/reliability", "RELIABILITY_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/reliability/broker/dlq", "RELIABILITY_REPLAY");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/reliability/unknown-results", "RELIABILITY_RESOLVE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/reliability/transaction-log-recovery", "RELIABILITY_RECOVERY_RUN");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/transactions", "TRANSACTION_META_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/standard-executions", "STANDARD_EXECUTION_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/channels", "CHANNEL_POLICY_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/channels/refresh", "CHANNEL_POLICY_REFRESH");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/channels/package/import", "CHANNEL_POLICY_IMPORT");
        BUTTON_BY_METHOD_PATH_PREFIX.put("PUT /adm/api/channels", "CHANNEL_POLICY_WRITE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/remote-logs", "REMOTE_LOG_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/remote-logs/bundles", "REMOTE_LOG_BUNDLE_DOWNLOAD");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/remote-logs/bundle-jobs", "REMOTE_LOG_BUNDLE_CREATE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/transactions", "TRANSACTION_META_WRITE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/audit-logs", "AUDIT_LOG_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/business-calendars", "BUSINESS_CALENDAR_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("PUT /adm/api/business-calendars", "BUSINESS_CALENDAR_WRITE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("DELETE /adm/api/business-calendars", "BUSINESS_CALENDAR_DELETE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/batch-runtime", "BATCH_RUNTIME_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/batch-runtime/retention", "BAT_RETENTION_VIEW");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/batch-runtime/retention/preview", "BAT_RETENTION_PREVIEW");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/batch-runtime/retention/policies", "BAT_RETENTION_POLICY_REQUEST");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/batch-runtime/job-definitions/validate", "BATCH_DEFINITION_VALIDATE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/batch-runtime/job-definitions/drafts", "BATCH_DEFINITION_WRITE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/batch-runtime/job-definitions", "BATCH_DEFINITION_TRANSITION");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/batch-runtime/commands", "BATCH_RUNTIME_COMMAND");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/batch-runtime/deployment-plans", "BATCH_DEPLOYMENT_PLAN");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/batch", "BATCH_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/batch/workbench", "BATCH_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/center-cut", "BATCH_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/center-cut", "BATCH_RECONCILE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/integration-closure", "INTEGRATION_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/integration-closure", "INTEGRATION_CONTROL");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/notifications", "NOTIFICATION_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/notifications", "NOTIFICATION_WRITE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("PUT /adm/api/notifications", "NOTIFICATION_WRITE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/downloads", "DOWNLOAD_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/downloads", "DOWNLOAD_EXECUTE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/file-jobs", "FILE_JOB_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/file-jobs/uploads", "FILE_JOB_UPLOAD");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/file-jobs", "FILE_JOB_APPLY");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/gateway-registry", "GATEWAY_DASHBOARD_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/gateway-registry/server-groups", "GATEWAY_GROUP_WRITE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("DELETE /adm/api/gateway-registry/server-groups", "GATEWAY_GROUP_DELETE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/gateway-registry/bindings", "GATEWAY_ROUTE_WRITE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("DELETE /adm/api/gateway-registry/bindings", "GATEWAY_ROUTE_DELETE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/gateway-registry/connection-test-operations", "GATEWAY_CONNECTION_TEST");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/batch/jobs", "BATCH_REGISTER");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/batch/schedules", "BATCH_SCHEDULE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/batch/executions", "BATCH_RETRY");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/batch", "BATCH_EXECUTE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/cache", "CACHE_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/cache", "CACHE_REFRESH");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/messages", "MESSAGE_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/messages", "MESSAGE_WRITE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("PUT /adm/api/messages", "MESSAGE_WRITE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("DELETE /adm/api/messages", "MESSAGE_DISABLE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/codes", "CODE_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/codes", "CODE_WRITE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("PUT /adm/api/codes", "CODE_WRITE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("DELETE /adm/api/codes", "CODE_DISABLE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/response-codes", "RESPONSE_CODE_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/response-codes", "RESPONSE_CODE_WRITE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("PUT /adm/api/response-codes", "RESPONSE_CODE_WRITE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("DELETE /adm/api/response-codes", "RESPONSE_CODE_DELETE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/configs", "CONFIG_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/configs", "CONFIG_WRITE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("PUT /adm/api/configs", "CONFIG_WRITE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("DELETE /adm/api/configs", "CONFIG_DELETE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/log-level", "DYNAMIC_LOG_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/log-level", "DYNAMIC_LOG_WRITE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("PUT /adm/api/log-level", "DYNAMIC_LOG_WRITE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("DELETE /adm/api/log-level", "DYNAMIC_LOG_DELETE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/log-policy-audits", "LOG_POLICY_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/log-policies", "LOG_POLICY_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/log-policies/cache/refresh", "LOG_POLICY_CACHE_REFRESH");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/log-policies/cache/clear", "LOG_POLICY_CACHE_CLEAR");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/log-policies", "LOG_POLICY_WRITE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("PUT /adm/api/log-policies", "LOG_POLICY_WRITE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("PATCH /adm/api/log-policies", "LOG_POLICY_WRITE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/security", "SECURITY_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/security", "SECURITY_WRITE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/permissions", "PERMISSION_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/permissions", "PERMISSION_WRITE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("PUT /adm/api/permissions", "PERMISSION_WRITE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/operators/password-policy", "PASSWORD_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/operators/sessions", "PASSWORD_SESSION_REVOKE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/operators", "OPERATOR_CREATE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("PUT /adm/api/operators", "OPERATOR_ROLE_UPDATE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/operators", "OPERATOR_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/incidents", "INCIDENT_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/incidents", "INCIDENT_WRITE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("PUT /adm/api/incidents/policies", "INCIDENT_WRITE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("PUT /adm/api/incidents/maintenance-windows", "INCIDENT_WRITE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/maintenance", "MAINTENANCE_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/maintenance", "MAINTENANCE_EXECUTE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/secrets", "SECRET_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/secrets/rotate", "SECRET_ROTATE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/break-glass", "BREAK_GLASS_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/break-glass", "BREAK_GLASS_OPEN");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/servers", "RUNTIME_CONTROL_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/runtime-inventory", "RUNTIME_CONTROL_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/servers", "RUNTIME_CONTROL_GROUP_WRITE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/runtime-control", "RUNTIME_CONTROL_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/runtime-control/preview-targets", "RUNTIME_CONTROL_PREVIEW");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/runtime-control/preview-change", "RUNTIME_CONTROL_PREVIEW");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/runtime-control/changes", "RUNTIME_CONTROL_CHANGE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/runtime-control/groups", "RUNTIME_CONTROL_GROUP_WRITE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("DELETE /adm/api/runtime-control/groups", "RUNTIME_CONTROL_GROUP_DELETE");
    }

    private final AdmSecurityProperties properties;
    private final AdmSessionService sessionService;
    private final JdbcTemplate admJdbcTemplate;
    private final AdmPersistencePolicy persistencePolicy;

    public AdmApiAuthFilter(
            AdmSecurityProperties properties,
            AdmSessionService sessionService,
            @Qualifier("admJdbcTemplate") JdbcTemplate admJdbcTemplate,
            AdmPersistencePolicy persistencePolicy) {
        this.properties = properties;
        this.sessionService = sessionService;
        this.admJdbcTemplate = admJdbcTemplate;
        this.persistencePolicy = persistencePolicy;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        if (!properties.isEnabled()
                || !path.startsWith("/adm/api/")
                || path.equals("/adm/api/auth/login")
                || isPublicHealthRequest(request.getMethod(), path)
                || HttpMethod.OPTIONS.matches(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Optional<AdmSession> session = sessionService.findValidSession(resolveBearerToken(request));
            if (session.isEmpty()) {
                writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, "ADM 인증이 필요합니다.");
                return;
            }

            AdmSession authenticatedSession = session.get();
            request.setAttribute("adm.operatorId", authenticatedSession.operatorId());

            boolean selfPasswordChange = isSelfPasswordChange(authenticatedSession, request.getMethod(), path);
            if (authenticatedSession.passwordChangeRequired()
                    && !isPasswordChangeOnlyRequest(authenticatedSession, request.getMethod(), path)) {
                writeJson(response, HttpServletResponse.SC_FORBIDDEN, "비밀번호를 먼저 변경해야 합니다.");
                return;
            }

            boolean authenticatedSelfService = isAuthenticatedSelfServiceRequest(request.getMethod(), path);
            if (!selfPasswordChange
                    && !authenticatedSelfService
                    && !hasPermission(authenticatedSession, request.getMethod(), path)) {
                writeJson(response, HttpServletResponse.SC_FORBIDDEN, "ADM 권한이 필요한 작업입니다.");
                return;
            }

            filterChain.doFilter(request, response);
        } catch (CpfBusinessException ex) {
            // Filter 단계의 인프라 장애는 MVC ControllerAdvice까지 전달되지 않을 수 있으므로 여기서 503으로 고정합니다.
            response.setHeader("X-Error-Code", CpfErrorCode.INFRASTRUCTURE_UNAVAILABLE.statusCode());
            response.setHeader("X-Message-Code", CpfErrorCode.INFRASTRUCTURE_UNAVAILABLE.messageCode());
            writeJson(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE, "ADM 인증/권한 저장소를 사용할 수 없습니다.");
        }
    }

    private boolean isPublicHealthRequest(String method, String path) {
        boolean readOnly = HttpMethod.GET.matches(method) || HttpMethod.HEAD.matches(method);
        return readOnly && (path.equals("/adm/api/health")
                || path.equals("/adm/api/health/liveness")
                || path.equals("/adm/api/health/readiness"));
    }

    private boolean isPasswordChangeOnlyRequest(AdmSession session, String method, String path) {
        return isSelfPasswordChange(session, method, path)
                || isAuthenticatedSelfServiceRequest(method, path)
                || (HttpMethod.GET.matches(method) && path.equals("/adm/api/operators/password-policy"));
    }

    private boolean isAuthenticatedSelfServiceRequest(String method, String path) {
        return (HttpMethod.GET.matches(method) && path.equals("/adm/api/auth/me"))
                || (HttpMethod.POST.matches(method) && path.equals("/adm/api/auth/logout"));
    }

    private boolean isSelfPasswordChange(AdmSession session, String method, String path) {
        return HttpMethod.POST.matches(method)
                && path.equals("/adm/api/operators/" + session.operatorId() + "/password");
    }

    private boolean hasPermission(AdmSession session, String method, String path) {
        // DB API permission이 제품 모드의 canonical route/action 계약입니다.
        // DB에 등록된 확장 API가 MEMORY fallback Map 누락 때문에 먼저 차단되지 않도록 우선 조회합니다.
        Optional<Boolean> dbApiPermission = hasDbApiPermission(session.roleIds(), method, path);
        if (dbApiPermission.isPresent()) {
            return dbApiPermission.get();
        }

        String menuId = resolveMenuId(path);
        if (menuId == null) {
            // canonical DB permission과 MEMORY fallback 양쪽에 없는 /adm/api/**는 기본 거부합니다.
            return false;
        }

        String buttonId = resolveButtonId(method, path);
        Optional<Boolean> dbButtonPermission = hasDbButtonPermission(session.roleIds(), buttonId);
        if (dbButtonPermission.isPresent()) {
            return dbButtonPermission.get();
        }

        Optional<Boolean> dbMenuPermission = hasDbMenuPermission(session.roleIds(), menuId, method);
        if (dbMenuPermission.isPresent()) {
            return dbMenuPermission.get();
        }

        if (persistencePolicy.databaseRequired()) {
            // DB 권한 Resource 자체가 없으면 신규 API가 자동 허용되는 것을 막습니다.
            return false;
        }

        // MEMORY는 명시적인 EDU/test 모드에서만 사용하는 개발 편의 정책입니다.
        List<String> roles = session.roleIds();
        if (roles.contains("ADM_ADMIN")) {
            return true;
        }
        if ("OPERATOR".equals(menuId) || "PERMISSION".equals(menuId) || "PASSWORD".equals(menuId) || "SECURITY".equals(menuId)) {
            return false;
        }
        return roles.contains("ADM_OPERATOR") || roles.contains("ADM_DEV_OPERATOR") || roles.contains("ADM_BIZ_OPERATOR");
    }

    private Optional<Boolean> hasDbApiPermission(List<String> roleIds, String method, String path) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Optional.of(false);
        }
        String placeholders = String.join(",", roleIds.stream().map(role -> "?").toList());
        List<Object> args = new ArrayList<>(roleIds);
        args.add(method);
        args.add("ANY");
        try {
            List<Map<String, Object>> permissions = admJdbcTemplate.queryForList("""
                    SELECT a.API_PATH, a.HTTP_METHOD, ra.ALLOW_YN
                      FROM adm_api_permission a
                      LEFT JOIN adm_role_api_permission ra
                        ON ra.API_PERMISSION_ID = a.API_PERMISSION_ID
                       AND ra.ROLE_ID IN (%s)
                     WHERE a.USE_YN = 'Y'
                       AND a.HTTP_METHOD IN (?, ?)
                    """.formatted(placeholders), args.toArray());
            List<AdmApiPermissionPolicy.Rule> rules = permissions.stream()
                    .map(permission -> new AdmApiPermissionPolicy.Rule(
                            String.valueOf(permission.get("HTTP_METHOD")),
                            String.valueOf(permission.get("API_PATH")),
                            permission.get("ALLOW_YN") == null ? "N" : String.valueOf(permission.get("ALLOW_YN"))))
                    .toList();
            return AdmApiPermissionPolicy.evaluate(rules, method, path);
        } catch (DataAccessException ex) {
            return onPermissionDataAccess("adm_api_permission", ex);
        }
    }

    private Optional<Boolean> hasDbMenuPermission(List<String> roleIds, String menuId, String method) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Optional.of(false);
        }
        String permissionColumn = permissionColumn(method);
        String placeholders = String.join(",", roleIds.stream().map(role -> "?").toList());
        Object[] args = new Object[roleIds.size() + 1];
        for (int i = 0; i < roleIds.size(); i++) {
            args[i] = roleIds.get(i);
        }
        args[roleIds.size()] = menuId;
        try {
            Integer count = admJdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM adm_role_menu rm
                    JOIN adm_menu m ON m.MENU_ID = rm.MENU_ID
                    WHERE rm.ROLE_ID IN (%s)
                      AND rm.MENU_ID = ?
                      AND m.USE_YN = 'Y'
                      AND rm.%s = 'Y'
                    """.formatted(placeholders, permissionColumn), Integer.class, args);
            return Optional.of(count != null && count > 0);
        } catch (DataAccessException ex) {
            return onPermissionDataAccess("adm_role_menu", ex);
        }
    }

    private Optional<Boolean> hasDbButtonPermission(List<String> roleIds, String buttonId) {
        if (buttonId == null || roleIds == null || roleIds.isEmpty()) {
            return Optional.empty();
        }
        String placeholders = String.join(",", roleIds.stream().map(role -> "?").toList());
        Object[] args = new Object[roleIds.size() + 1];
        for (int i = 0; i < roleIds.size(); i++) {
            args[i] = roleIds.get(i);
        }
        args[roleIds.size()] = buttonId;
        try {
            Integer count = admJdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM adm_role_button rb
                    JOIN adm_button b ON b.BUTTON_ID = rb.BUTTON_ID
                    WHERE rb.ROLE_ID IN (%s)
                      AND rb.BUTTON_ID = ?
                      AND rb.ALLOW_YN = 'Y'
                      AND b.USE_YN = 'Y'
                    """.formatted(placeholders), Integer.class, args);
            return Optional.of(count != null && count > 0);
        } catch (DataAccessException ex) {
            return onPermissionDataAccess("adm_role_button", ex);
        }
    }

    private Optional<Boolean> onPermissionDataAccess(String component, DataAccessException ex) {
        if (persistencePolicy.memoryEnabled()) {
            return Optional.empty();
        }
        throw new CpfBusinessException(
                CpfErrorCode.INFRASTRUCTURE_UNAVAILABLE,
                "ADM 권한 저장소를 사용할 수 없습니다.",
                Map.of("0", component, "1", ex.getClass().getSimpleName()));
    }

    private String permissionColumn(String method) {
        if (HttpMethod.DELETE.matches(method)) {
            return "DELETE_YN";
        }
        if (HttpMethod.POST.matches(method) || HttpMethod.PUT.matches(method) || HttpMethod.PATCH.matches(method)) {
            return "WRITE_YN";
        }
        return "READ_YN";
    }

    private String resolveMenuId(String path) {
        for (Map.Entry<String, String> entry : MENU_BY_PATH_PREFIX.entrySet()) {
            if (matchesPathPrefix(entry.getKey(), path)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private boolean matchesPathPrefix(String prefix, String path) {
        return path.equals(prefix) || path.startsWith(prefix + "/");
    }

    private String resolveButtonId(String method, String path) {
        if (HttpMethod.GET.matches(method) && path.startsWith("/adm/api/batch-runtime/retention/")) {
            return "BAT_RETENTION_VIEW";
        }
        if (HttpMethod.POST.matches(method) && path.equals("/adm/api/batch-runtime/retention/preview")) {
            return "BAT_RETENTION_PREVIEW";
        }
        if (HttpMethod.POST.matches(method) && path.equals("/adm/api/batch-runtime/retention/policies")) {
            return "BAT_RETENTION_POLICY_REQUEST";
        }
        if (HttpMethod.POST.matches(method) && path.startsWith("/adm/api/batch-runtime/retention/policies/")) {
            if (path.endsWith("/run")) return "BAT_RETENTION_RUN_REQUEST";
            if (path.endsWith("/pause")) return "BAT_RETENTION_POLICY_PAUSE";
            if (path.endsWith("/resume")) return "BAT_RETENTION_POLICY_RESUME";
        }
        if (HttpMethod.POST.matches(method) && path.startsWith("/adm/api/batch-runtime/retention/runs/")) {
            if (path.endsWith("/pause")) return "BAT_RETENTION_RUN_PAUSE";
            if (path.endsWith("/resume")) return "BAT_RETENTION_RUN_RESUME";
        }
        if (HttpMethod.GET.matches(method) && path.startsWith("/adm/api/file-jobs/")) {
            return path.endsWith("/artifact") ? "FILE_JOB_DOWNLOAD" : "FILE_JOB_READ";
        }
        if (HttpMethod.GET.matches(method) && path.equals("/adm/api/file-jobs")) {
            return "FILE_JOB_READ";
        }
        if (HttpMethod.POST.matches(method) && path.equals("/adm/api/file-jobs/uploads")) {
            return "FILE_JOB_UPLOAD";
        }
        if (HttpMethod.POST.matches(method) && path.startsWith("/adm/api/file-jobs/")) {
            if (path.endsWith("/apply")) return "FILE_JOB_APPLY";
            if (path.endsWith("/retry")) return "FILE_JOB_RETRY";
            if (path.endsWith("/cancel")) return "FILE_JOB_CANCEL";
            if (path.endsWith("/rollback")) return "FILE_JOB_ROLLBACK";
            if (path.endsWith("/resolve-unknown")) return "FILE_JOB_RESOLVE";
        }
        if (HttpMethod.POST.matches(method) && path.equals("/adm/api/cache/evict-key")) {
            return "CACHE_EVICT_KEY";
        }
        if (HttpMethod.POST.matches(method) && path.equals("/adm/api/cache/evict-namespace")) {
            return "CACHE_EVICT_NAMESPACE";
        }
        if (HttpMethod.POST.matches(method) && path.equals("/adm/api/cache/reconcile")) {
            return "CACHE_RECONCILE";
        }
        if (HttpMethod.POST.matches(method) && path.contains("/notifications/delivery-logs/") && path.endsWith("/retry")) {
            return "NOTIFICATION_RETRY";
        }
        if (HttpMethod.POST.matches(method) && path.contains("/notifications/delivery-logs/") && path.endsWith("/cancel")) {
            return "NOTIFICATION_CANCEL";
        }
        if (HttpMethod.POST.matches(method)
                && path.startsWith("/adm/api/remote-logs/bundle-jobs/")
                && path.endsWith("/download-tokens")) {
            return "REMOTE_LOG_BUNDLE_TOKEN";
        }
        if (HttpMethod.GET.matches(method)
                && path.startsWith("/adm/api/remote-logs/bundle-jobs/")
                && path.endsWith("/download")) {
            return "REMOTE_LOG_JOB_DOWNLOAD";
        }
        if (HttpMethod.GET.matches(method) && path.startsWith("/adm/api/remote-logs/") && path.endsWith("/download")) {
            return "REMOTE_LOG_DOWNLOAD";
        }
        if (HttpMethod.POST.matches(method) && path.endsWith("/password/reset")) {
            return "PASSWORD_RESET";
        }
        if (HttpMethod.POST.matches(method) && path.endsWith("/unlock")) {
            return "PASSWORD_UNLOCK";
        }
        if (HttpMethod.POST.matches(method) && path.endsWith("/contacts/raw")) {
            return "OPERATOR_PII_RAW";
        }
        if (HttpMethod.PUT.matches(method) && path.endsWith("/contacts")) {
            return "OPERATOR_CONTACT_UPDATE";
        }
        if (HttpMethod.PUT.matches(method) && path.endsWith("/status")) {
            return "OPERATOR_STATUS_UPDATE";
        }
        if (HttpMethod.PUT.matches(method) && path.endsWith("/roles")) {
            return "OPERATOR_ROLE_UPDATE";
        }
        if (HttpMethod.POST.matches(method) && path.contains("/executions/") && path.endsWith("/retry")) {
            return "BATCH_RETRY";
        }
        if (HttpMethod.POST.matches(method) && path.contains("/executions/") && path.endsWith("/stop")) {
            return "BATCH_STOP";
        }
        if (HttpMethod.POST.matches(method) && path.contains("/schedules/")) {
            return "BATCH_SCHEDULE";
        }
        if (HttpMethod.POST.matches(method) && path.endsWith("/scheduler/run-once")) {
            return "BATCH_SCHEDULER_RUN";
        }
        if (HttpMethod.POST.matches(method) && path.endsWith("/run")) {
            return "BATCH_EXECUTE";
        }
        if (HttpMethod.PUT.matches(method) && path.contains("/notifications/rules/") && path.endsWith("/disable")) {
            return "NOTIFICATION_DISABLE";
        }
        if (HttpMethod.POST.matches(method) && path.contains("/notifications/rules/") && path.endsWith("/test-send")) {
            return "NOTIFICATION_TEST_SEND";
        }
        String keyPrefix = method + " ";
        for (Map.Entry<String, String> entry : BUTTON_BY_METHOD_PATH_PREFIX.entrySet()) {
            if (entry.getKey().startsWith(keyPrefix)
                    && matchesPathPrefix(entry.getKey().substring(keyPrefix.length()), path)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private String resolveBearerToken(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return "";
        }
        return authorization.substring("Bearer ".length()).trim();
    }

    private void writeJson(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"message\":\"" + message.replace("\"", "\\\"") + "\"}");
    }
}

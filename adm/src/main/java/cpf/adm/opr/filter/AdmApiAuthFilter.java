package cpf.adm.opr.filter;

import cpf.adm.config.AdmSecurityProperties;
import cpf.adm.opr.dto.AdmSession;
import cpf.adm.opr.service.AdmSessionService;
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
 * 신규 ADM 기능을 추가할 때는 메뉴 ID와 버튼 ID를 이 필터, seed SQL, 관리자 가이드에 함께 반영해야 합니다.</p>
 */
@Component
public class AdmApiAuthFilter extends OncePerRequestFilter {
    private static final Map<String, String> MENU_BY_PATH_PREFIX = new LinkedHashMap<>();
    private static final Map<String, String> BUTTON_BY_METHOD_PATH_PREFIX = new LinkedHashMap<>();

    static {
        MENU_BY_PATH_PREFIX.put("/adm/api/logs", "LOG_LIST");
        MENU_BY_PATH_PREFIX.put("/adm/api/transaction-groups", "LOG_LIST");
        MENU_BY_PATH_PREFIX.put("/adm/api/observability", "LOG_LIST");
        MENU_BY_PATH_PREFIX.put("/adm/api/service-registry", "SERVICE_REGISTRY");
        MENU_BY_PATH_PREFIX.put("/adm/api/transactions", "TRANSACTION_META");
        MENU_BY_PATH_PREFIX.put("/adm/api/audit-logs", "AUDIT_LOG");
        MENU_BY_PATH_PREFIX.put("/adm/api/members", "MEMBER");
        MENU_BY_PATH_PREFIX.put("/adm/api/batch", "BATCH");
        MENU_BY_PATH_PREFIX.put("/adm/api/center-cut", "BATCH");
        MENU_BY_PATH_PREFIX.put("/adm/api/notifications", "NOTIFICATION");
        MENU_BY_PATH_PREFIX.put("/adm/api/downloads", "DOWNLOAD");
        MENU_BY_PATH_PREFIX.put("/adm/api/cache", "CACHE");
        MENU_BY_PATH_PREFIX.put("/adm/api/messages", "MESSAGE");
        MENU_BY_PATH_PREFIX.put("/adm/api/codes", "CODE");
        MENU_BY_PATH_PREFIX.put("/adm/api/response-codes", "RESPONSE_CODE");
        MENU_BY_PATH_PREFIX.put("/adm/api/configs", "CONFIG");
        MENU_BY_PATH_PREFIX.put("/adm/api/log-level", "DYNAMIC_LOG");
        MENU_BY_PATH_PREFIX.put("/adm/api/log-policy-audits", "LOG_POLICY");
        MENU_BY_PATH_PREFIX.put("/adm/api/log-policies", "LOG_POLICY");
        MENU_BY_PATH_PREFIX.put("/adm/api/security", "SECURITY");
        MENU_BY_PATH_PREFIX.put("/adm/api/permissions", "PERMISSION");
        MENU_BY_PATH_PREFIX.put("/adm/api/operators", "OPERATOR");

        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/logs", "LOG_LIST_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/transaction-groups", "LOG_LIST_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/observability", "LOG_LIST_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/service-registry", "SERVICE_REGISTRY_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/transactions", "TRANSACTION_META_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/transactions/scan", "TRANSACTION_META_SCAN");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/transactions", "TRANSACTION_META_WRITE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/audit-logs", "AUDIT_LOG_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/members", "MEMBER_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/members", "MEMBER_CREATE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("PUT /adm/api/members", "MEMBER_UPDATE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("DELETE /adm/api/members", "MEMBER_DELETE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/batch", "BATCH_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/center-cut", "BATCH_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/notifications", "NOTIFICATION_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/notifications", "NOTIFICATION_WRITE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("PUT /adm/api/notifications", "NOTIFICATION_WRITE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/downloads", "DOWNLOAD_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/downloads", "DOWNLOAD_EXECUTE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/batch/jobs", "BATCH_REGISTER");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/batch/calendar", "BATCH_CALENDAR_SAVE");
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
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/configs", "CONFIG_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/configs", "CONFIG_WRITE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("PUT /adm/api/configs", "CONFIG_WRITE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/log-level", "DYNAMIC_LOG_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/log-level", "DYNAMIC_LOG_WRITE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/log-policy-audits", "LOG_POLICY_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/log-policies", "LOG_POLICY_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/log-policies/cache/refresh", "LOG_POLICY_CACHE_REFRESH");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/log-policies/cache/clear", "LOG_POLICY_CACHE_CLEAR");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/log-policies", "LOG_POLICY_WRITE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("PUT /adm/api/log-policies", "LOG_POLICY_WRITE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("PATCH /adm/api/log-policies", "LOG_POLICY_WRITE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/permissions", "PERMISSION_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/permissions", "PERMISSION_WRITE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("PUT /adm/api/permissions", "PERMISSION_WRITE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/operators/password-policy", "PASSWORD_READ");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/operators/sessions", "PASSWORD_SESSION_REVOKE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("POST /adm/api/operators", "OPERATOR_CREATE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("PUT /adm/api/operators", "OPERATOR_ROLE_UPDATE");
        BUTTON_BY_METHOD_PATH_PREFIX.put("GET /adm/api/operators", "OPERATOR_READ");
    }

    private final AdmSecurityProperties properties;
    private final AdmSessionService sessionService;
    private final JdbcTemplate admJdbcTemplate;

    public AdmApiAuthFilter(
            AdmSecurityProperties properties,
            AdmSessionService sessionService,
            @Qualifier("admJdbcTemplate") JdbcTemplate admJdbcTemplate) {
        this.properties = properties;
        this.sessionService = sessionService;
        this.admJdbcTemplate = admJdbcTemplate;
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
                || path.equals("/adm/api/health")
                || HttpMethod.OPTIONS.matches(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        Optional<AdmSession> session = sessionService.findValidSession(resolveBearerToken(request));
        if (session.isEmpty()) {
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, "ADM 인증이 필요합니다.");
            return;
        }

        if (!hasPermission(session.get(), request.getMethod(), path)) {
            writeJson(response, HttpServletResponse.SC_FORBIDDEN, "ADM 권한이 필요한 작업입니다.");
            return;
        }

        request.setAttribute("adm.operatorId", session.get().operatorId());
        filterChain.doFilter(request, response);
    }

    private boolean hasPermission(AdmSession session, String method, String path) {
        String menuId = resolveMenuId(path);
        if (menuId == null) {
            return true;
        }

        Optional<Boolean> dbApiPermission = hasDbApiPermission(session.roleIds(), method, path);
        if (dbApiPermission.isPresent()) {
            return dbApiPermission.get();
        }

        Optional<Boolean> dbMenuPermission = hasDbMenuPermission(session.roleIds(), menuId, method);
        if (dbMenuPermission.isPresent() && !dbMenuPermission.get()) {
            return false;
        }

        String buttonId = resolveButtonId(method, path);
        Optional<Boolean> dbButtonPermission = hasDbButtonPermission(session.roleIds(), buttonId);
        if (dbButtonPermission.isPresent()) {
            return dbButtonPermission.get();
        }
        if (dbMenuPermission.isPresent()) {
            return dbMenuPermission.get();
        }

        List<String> roles = session.roleIds();
        if (roles.contains("ADM_ADMIN")) {
            return true;
        }
        boolean readOnly = HttpMethod.GET.matches(method) || HttpMethod.HEAD.matches(method);
        if (readOnly) {
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
        List<Object> args = new ArrayList<>();
        args.addAll(roleIds);
        args.add(method);
        args.add("ANY");
        try {
            List<Map<String, Object>> permissions = admJdbcTemplate.queryForList("""
                    SELECT a.API_PATH, COALESCE(MAX(ra.ALLOW_YN), 'N') AS ALLOW_YN
                    FROM adm_api_permission a
                    LEFT JOIN adm_role_api_permission ra
                           ON ra.API_PERMISSION_ID = a.API_PERMISSION_ID
                          AND ra.ROLE_ID IN (%s)
                    WHERE a.USE_YN = 'Y'
                      AND a.HTTP_METHOD IN (?, ?)
                    GROUP BY a.API_PERMISSION_ID, a.API_PATH
                    """.formatted(placeholders), args.toArray());
            boolean matched = false;
            boolean allowed = false;
            for (Map<String, Object> permission : permissions) {
                String apiPath = String.valueOf(permission.get("API_PATH"));
                if (matchesApiPattern(apiPath, path)) {
                    matched = true;
                    allowed = allowed || "Y".equals(String.valueOf(permission.get("ALLOW_YN")));
                }
            }
            if (!matched) {
                return Optional.empty();
            }
            return Optional.of(allowed);
        } catch (DataAccessException ex) {
            return Optional.empty();
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
            return Optional.empty();
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
            return Optional.empty();
        }
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
            if (path.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private String resolveButtonId(String method, String path) {
        if (HttpMethod.POST.matches(method) && path.endsWith("/password/reset")) {
            return "PASSWORD_RESET";
        }
        if (HttpMethod.POST.matches(method) && path.endsWith("/unlock")) {
            return "PASSWORD_UNLOCK";
        }
        if (HttpMethod.PUT.matches(method) && path.endsWith("/roles")) {
            return "OPERATOR_ROLE_UPDATE";
        }
        if (HttpMethod.POST.matches(method) && path.contains("/members/") && path.endsWith("/roles")) {
            return "MEMBER_ROLE_GRANT";
        }
        if (HttpMethod.DELETE.matches(method) && path.contains("/members/") && path.contains("/roles/")) {
            return "MEMBER_ROLE_REVOKE";
        }
        if (HttpMethod.PUT.matches(method) && path.contains("/members/") && path.endsWith("/status")) {
            return "MEMBER_STATUS";
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
            if (entry.getKey().startsWith(keyPrefix) && path.startsWith(entry.getKey().substring(keyPrefix.length()))) {
                return entry.getValue();
            }
        }
        return null;
    }

    private boolean matchesApiPattern(String pattern, String path) {
        if (pattern == null || pattern.isBlank()) {
            return false;
        }
        String normalizedPattern = pattern.trim();
        if (normalizedPattern.equals(path)) {
            return true;
        }
        if (normalizedPattern.endsWith("/**")) {
            String prefix = normalizedPattern.substring(0, normalizedPattern.length() - 3);
            return path.equals(prefix) || path.startsWith(prefix + "/");
        }
        if (!normalizedPattern.contains("*")) {
            return false;
        }
        String[] parts = normalizedPattern.split("\\*", -1);
        int index = 0;
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.isEmpty()) {
                continue;
            }
            int found = path.indexOf(part, index);
            if (found < 0) {
                return false;
            }
            if (i == 0 && found != 0) {
                return false;
            }
            index = found + part.length();
        }
        String last = parts[parts.length - 1];
        return last.isEmpty() || path.endsWith(last);
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

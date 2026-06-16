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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ADM REST API authentication and authorization filter.
 */
@Component
public class AdmApiAuthFilter extends OncePerRequestFilter {
    private static final Map<String, String> MENU_BY_PATH_PREFIX = new LinkedHashMap<>();

    static {
        MENU_BY_PATH_PREFIX.put("/adm/api/logs", "LOG_LIST");
        MENU_BY_PATH_PREFIX.put("/adm/api/audit-logs", "AUDIT_LOG");
        MENU_BY_PATH_PREFIX.put("/adm/api/cache", "CACHE");
        MENU_BY_PATH_PREFIX.put("/adm/api/response-codes", "RESPONSE_CODE");
        MENU_BY_PATH_PREFIX.put("/adm/api/log-level", "DYNAMIC_LOG");
        MENU_BY_PATH_PREFIX.put("/adm/api/operators", "OPERATOR");
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
                || HttpMethod.OPTIONS.matches(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        Optional<AdmSession> session = sessionService.findValidSession(resolveBearerToken(request));
        if (session.isEmpty()) {
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, "ADM authentication is required.");
            return;
        }

        if (!hasPermission(session.get(), request.getMethod(), path)) {
            writeJson(response, HttpServletResponse.SC_FORBIDDEN, "ADM permission is required for this operation.");
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

        Optional<Boolean> dbPermission = hasDbPermission(session.roleIds(), menuId, method);
        if (dbPermission.isPresent()) {
            return dbPermission.get();
        }

        List<String> roles = session.roleIds();
        if (roles.contains("ADM_ADMIN")) {
            return true;
        }
        boolean readOnly = HttpMethod.GET.matches(method) || HttpMethod.HEAD.matches(method);
        if (readOnly) {
            return true;
        }
        if ("OPERATOR".equals(menuId) || "RESPONSE_CODE".equals(menuId) || "AUDIT_LOG".equals(menuId)) {
            return false;
        }
        return roles.contains("ADM_OPERATOR");
    }

    private Optional<Boolean> hasDbPermission(List<String> roleIds, String menuId, String method) {
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
                    FROM operator_role_menu rm
                    JOIN operator_menu m ON m.MENU_ID = rm.MENU_ID
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

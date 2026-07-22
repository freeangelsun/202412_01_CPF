package cpf.bza.auth.filter;

import cpf.bza.auth.service.BzaAuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

/** BZA 화면 숨김과 별개로 모든 업무 백오피스 API 권한을 서버에서 재검사합니다. */
@Component
public class BzaApiAuthFilter extends OncePerRequestFilter {
    private final BzaAuthService authService;

    public BzaApiAuthFilter(BzaAuthService authService) {
        this.authService = authService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/bza/")
                || path.startsWith("/api/bza/auth/")
                || "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            Permission permission = resolvePermission(request);
            Map<String, Object> operator = authService.authorize(
                    request.getHeader(HttpHeaders.AUTHORIZATION), permission.menuCode(), permission.actionCode());
            request.setAttribute("bza.operator", operator);
            request.setAttribute("bza.operatorId", operator.get("loginId"));
            filterChain.doFilter(request, response);
        } catch (ResponseStatusException ex) {
            response.setStatus(ex.getStatusCode().value());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"message\":\"BZA 인증 또는 권한 확인에 실패했습니다.\"}");
        }
    }

    private Permission resolvePermission(HttpServletRequest request) {
        String path = request.getRequestURI().substring("/api/bza/".length());
        String[] segments = path.split("/");
        String resource = segments.length == 0 ? "DASHBOARD" : segments[0];
        if ("backoffice".equals(resource) && segments.length > 1) {
            resource = segments[1];
        }
        String menu = switch (resource) {
            case "admin-users" -> "USER";
            case "menus" -> "MENU";
            case "roles" -> "ROLE";
            case "permissions" -> "PERMISSION";
            case "customers", "masking" -> "CUSTOMER";
            case "products" -> "PRODUCT";
            case "orders" -> "ORDER";
            case "settings" -> "SETTING";
            case "downloads", "download-audits" -> "DOWNLOAD";
            case "organizations" -> "ORGANIZATION";
            case "employees" -> "EMPLOYEE";
            case "approvals" -> "APPROVAL";
            case "audits" -> "AUDIT";
            case "notifications" -> "NOTIFICATION";
            case "attachments" -> "ATTACHMENT";
            case "saved-searches" -> "SAVED_SEARCH";
            case "dashboard" -> "DASHBOARD";
            default -> resource.toUpperCase(Locale.ROOT).replace('-', '_');
        };
        String action = "GET".equalsIgnoreCase(request.getMethod()) ? "READ" : "WRITE";
        if (path.contains("masking/unmask")) {
            action = "UNMASK";
        } else if (path.contains("/download")) {
            action = "DOWNLOAD";
        }
        return new Permission(menu, action);
    }

    private record Permission(String menuCode, String actionCode) {
    }
}

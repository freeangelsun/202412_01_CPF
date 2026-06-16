package cpf.adm.opr.filter;

import cpf.adm.config.AdmSecurityProperties;
import cpf.adm.opr.dto.AdmSession;
import cpf.adm.opr.service.AdmSessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

/**
 * ADM REST API 蹂댄샇 ?꾪꽣?낅땲??
 *
 * <p>Vue ?붾㈃怨??몃? ?꾧뎄 紐⑤몢 媛숈? `/adm/api/**` API瑜??몄텧?섎ŉ,
 * 濡쒓렇??API瑜??쒖쇅???몄텧? Bearer ?좏겙怨???븷 沅뚰븳???뺤씤?⑸땲??</p>
 */
@Component
public class AdmApiAuthFilter extends OncePerRequestFilter {
    private final AdmSecurityProperties properties;
    private final AdmSessionService sessionService;

    public AdmApiAuthFilter(
            AdmSecurityProperties properties,
            AdmSessionService sessionService) {
        this.properties = properties;
        this.sessionService = sessionService;
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
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, "ADM 濡쒓렇?몄씠 ?꾩슂?⑸땲??");
            return;
        }

        if (!hasPermission(session.get(), request.getMethod(), path)) {
            writeJson(response, HttpServletResponse.SC_FORBIDDEN, "ADM 硫붾돱 沅뚰븳???놁뒿?덈떎.");
            return;
        }

        request.setAttribute("adm.operatorId", session.get().operatorId());
        filterChain.doFilter(request, response);
    }

    private boolean hasPermission(AdmSession session, String method, String path) {
        List<String> roles = session.roleIds();
        if (roles.contains("ADM_ADMIN")) {
            return true;
        }
        boolean readOnly = HttpMethod.GET.matches(method) || HttpMethod.HEAD.matches(method);
        if (readOnly) {
            return true;
        }
        if (path.startsWith("/adm/api/operators")) {
            return false;
        }
        return roles.contains("ADM_OPERATOR");
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
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}


package com.cpf.security.resourceserver;

import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.security.context.CpfSecurityRuntimeContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Objects;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 인증 완료 결과를 Core의 최소 identity와 Security Owner Context로 분리해 바인딩합니다.
 *
 * <p>Raw Authorization Header, Access Token, Password, API Key는 어떤 Context에도 저장하지 않습니다.
 * subject/actor/tenant처럼 전역적으로 신뢰 가능한 의미만 Core로 승격하고 JWT/인증 방식 같은 Runtime
 * 메타데이터는 {@link CpfSecurityRuntimeContext}로 Servlet request 범위에서만 유지합니다.</p>
 */
public final class CpfAuthenticatedContextFilter extends OncePerRequestFilter {
    private final CpfResourceServerProperties properties;

    public CpfAuthenticatedContextFilter(CpfResourceServerProperties properties) {
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        CpfContextSnapshot snapshot = CpfContexts.snapshot();
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (snapshot == null || authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            chain.doFilter(request, response);
            return;
        }

        String subject = String.valueOf(jwt.getClaims().getOrDefault(properties.getUserIdClaim(), jwt.getSubject()));
        Object tenantClaim = jwt.getClaims().get(properties.getTenantIdClaim());
        String tenant = tenantClaim == null ? null : String.valueOf(tenantClaim);
        String assurance = text(jwt.getClaims().get("acr"));
        String delegationId = text(jwt.getClaims().get("act"));
        Instant authenticatedAt = jwt.getIssuedAt() == null ? Instant.now() : jwt.getIssuedAt();
        CpfContext.CpfIdentityContext identity = new CpfContext.CpfIdentityContext(
                subject, authentication.getName(), CpfContext.CpfPrincipalType.USER,
                jwt.getId(), delegationId, assurance, authenticatedAt);
        CpfContext.CpfTenantContext tenantContext = tenant == null || tenant.isBlank()
                ? null : new CpfContext.CpfTenantContext(tenant);
        CpfContext enriched = snapshot.context().withIdentityAndTenant(identity, tenantContext);
        CpfSecurityRuntimeContext securityContext = new CpfSecurityRuntimeContext(
                jwt.getId(), "JWT", assurance, authenticatedAt,
                null, null, delegationId, null, "RESOURCE_SERVER");

        Object previousSecurityContext = request.getAttribute(CpfSecurityRuntimeContext.REQUEST_ATTRIBUTE);
        request.setAttribute(CpfSecurityRuntimeContext.REQUEST_ATTRIBUTE, securityContext);
        try (AutoCloseable ignored = CpfContexts.bind(CpfContextSnapshot.capture(enriched))) {
            chain.doFilter(request, response);
        } catch (IOException | ServletException e) {
            throw e;
        } catch (Exception e) {
            throw new ServletException("CPF context scope close failed", e);
        } finally {
            if (previousSecurityContext == null) request.removeAttribute(CpfSecurityRuntimeContext.REQUEST_ATTRIBUTE);
            else request.setAttribute(CpfSecurityRuntimeContext.REQUEST_ATTRIBUTE, previousSecurityContext);
        }
    }

    /** 현재 Servlet request에 바인딩된 Security Owner Context를 반환합니다. */
    public static CpfSecurityRuntimeContext currentSecurityContext(HttpServletRequest request) {
        Object value = request.getAttribute(CpfSecurityRuntimeContext.REQUEST_ATTRIBUTE);
        return value instanceof CpfSecurityRuntimeContext context ? context : null;
    }

    private static String text(Object value) {
        if (value == null) return null;
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }
}

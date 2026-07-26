package com.cpf.core.common.tenant;

import com.cpf.core.api.tenant.CpfTenantContext;
import com.cpf.core.spi.tenant.CpfTenantResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/** Tenant 기능을 명시적으로 켠 경우에만 활성화되는 fail-closed 요청 경계. */
@Component
@ConditionalOnProperty(name = "cpf.tenant.enabled", havingValue = "true")
public class CpfTenantContextFilter extends OncePerRequestFilter {
    private final ObjectProvider<CpfTenantResolver> resolverProvider;
    public CpfTenantContextFilter(ObjectProvider<CpfTenantResolver> resolverProvider) { this.resolverProvider = resolverProvider; }

    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        CpfTenantResolver resolver = resolverProvider.getIfAvailable();
        if (resolver == null) { response.sendError(503, "Tenant resolver is not configured"); return; }
        String tenantId = resolver.resolveTenantId(request);
        if (tenantId == null || tenantId.isBlank()) { response.sendError(400, "Tenant is required"); return; }
        try { CpfTenantContext.set(tenantId); chain.doFilter(request, response); }
        finally { CpfTenantContext.clear(); }
    }
}

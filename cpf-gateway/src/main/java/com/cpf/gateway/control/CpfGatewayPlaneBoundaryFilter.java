package com.cpf.gateway.control;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Control API가 Data Plane Listener로 노출되지 않도록 Port/TLS 경계를 먼저 강제합니다. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnProperty(prefix = "cpf.gateway.control", name = "enabled", havingValue = "true")
public final class CpfGatewayPlaneBoundaryFilter extends OncePerRequestFilter {
    private static final String REGISTRY_PREFIX = "/internal/v1/gateway/registry";
    private final CpfGatewayControlSecurityProperties properties;

    public CpfGatewayPlaneBoundaryFilter(CpfGatewayControlSecurityProperties properties) {
        this.properties = properties;
        properties.validate();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(REGISTRY_PREFIX);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        if (request.getLocalPort() != properties.getListenerPort()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (properties.isTlsEnabled() && !request.isSecure()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        chain.doFilter(request, response);
    }
}

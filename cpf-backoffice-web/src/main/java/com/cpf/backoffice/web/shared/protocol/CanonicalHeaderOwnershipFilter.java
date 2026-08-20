package com.cpf.backoffice.web.shared.protocol;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public final class CanonicalHeaderOwnershipFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/api/v1/backoffice/")) {
            for (String name : CanonicalTransactionHeaders.BROWSER_FORBIDDEN) {
                if (request.getHeader(name) != null) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Canonical transaction headers are channel-owned");
                    return;
                }
            }
        }
        chain.doFilter(request, response);
    }
}

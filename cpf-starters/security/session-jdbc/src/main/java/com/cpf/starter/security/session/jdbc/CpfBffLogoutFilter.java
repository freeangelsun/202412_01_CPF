package com.cpf.starter.security.session.jdbc;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.springframework.web.filter.OncePerRequestFilter;

/** 로그아웃 성공 여부와 무관하게 현재 BFF Credential을 폐기하고 Session을 무효화합니다. */
final class CpfBffLogoutFilter extends OncePerRequestFilter {
    private final CpfBffCredentialVault vault;
    CpfBffLogoutFilter(CpfBffCredentialVault vault) { this.vault = vault; }

    @Override protected boolean shouldNotFilter(HttpServletRequest request) {
        return !("POST".equals(request.getMethod()) && request.getRequestURI().endsWith("/auth/logout"));
    }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String handle = session == null ? null : string(session.getAttribute(CpfBffSessionBridgeFilter.CREDENTIAL_HANDLE));
        RuntimeException revokeFailure = null;
        try {
            chain.doFilter(request, response);
        } finally {
            try {
                if (handle != null) {
                    vault.revoke(handle);
                }
            } catch (RuntimeException failure) {
                revokeFailure = failure;
            } finally {
                if (session != null) {
                    try {
                        session.invalidate();
                    } catch (IllegalStateException ignored) {
                        // 이미 무효화된 Session입니다.
                    }
                }
            }
            if (revokeFailure != null) {
                throw new ServletException("CPF_BFF_LOGOUT_VAULT_REVOKE_FAILED", revokeFailure);
            }
        }
    }
    private static String string(Object value) { return value instanceof String text && !text.isBlank() ? text : null; }
}

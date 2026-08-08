package com.cpf.starter.security.session.jdbc;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/** Session에는 난수 Handle만 보관하고 Vault Credential을 서버 내부 인증 경계에만 전달합니다. */
public final class CpfBffSessionBridgeFilter extends OncePerRequestFilter {
    public static final String CREDENTIAL_HANDLE = "CPF_BFF_CREDENTIAL_HANDLE";
    public static final String PRINCIPAL_ID = "CPF_BFF_PRINCIPAL_ID";
    public static final String INTERNAL_REFRESH_TOKEN_ATTRIBUTE =
            CpfBffSessionBridgeFilter.class.getName() + ".REFRESH_TOKEN";

    private final CpfBffCredentialVault vault;

    public CpfBffSessionBridgeFilter(CpfBffCredentialVault vault) {
        this.vault = vault;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !(path.startsWith("/adm/api/") || path.startsWith("/api/bza/"));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        if (request.getHeader("Authorization") != null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Browser Authorization header is prohibited");
            return;
        }

        String path = request.getRequestURI();
        if (path.endsWith("/auth/login")) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = request.getSession(false);
        String handle = session == null ? null : attribute(session, CREDENTIAL_HANDLE);
        if (handle == null) {
            chain.doFilter(request, response);
            return;
        }

        CpfBffCredential credential = vault.find(handle).orElse(null);
        if (credential == null) {
            session.removeAttribute(CREDENTIAL_HANDLE);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "BFF credential is not available");
            return;
        }

        String principal = attribute(session, PRINCIPAL_ID);
        if (principal == null) {
            vault.revoke(handle);
            session.removeAttribute(CREDENTIAL_HANDLE);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "BFF session principal is not available");
            return;
        }

        SecurityContext previousContext = SecurityContextHolder.getContext();
        SecurityContext bffContext = SecurityContextHolder.createEmptyContext();
        bffContext.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                principal, null, List.of()));
        SecurityContextHolder.setContext(bffContext);
        try {
            Instant now = Instant.now();
            if (path.endsWith("/auth/refresh")) {
                if (credential.refreshToken() == null || credential.refreshExpired(now)) {
                    vault.revoke(handle);
                    session.removeAttribute(CREDENTIAL_HANDLE);
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "BFF refresh credential expired");
                    return;
                }
                request.setAttribute(INTERNAL_REFRESH_TOKEN_ATTRIBUTE, credential.refreshToken());
                chain.doFilter(request, response);
                return;
            }

            if (path.endsWith("/auth/logout") && credential.refreshToken() != null) {
                request.setAttribute(INTERNAL_REFRESH_TOKEN_ATTRIBUTE, credential.refreshToken());
            }
            if (credential.accessExpired(now)) {
                if (path.endsWith("/auth/logout")) {
                    chain.doFilter(request, response);
                } else {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "BFF access credential expired");
                }
                return;
            }

            String token = credential.accessToken();
            chain.doFilter(new HttpServletRequestWrapper(request) {
                @Override
                public String getHeader(String name) {
                    return "Authorization".equalsIgnoreCase(name) ? "Bearer " + token : super.getHeader(name);
                }

                @Override
                public Enumeration<String> getHeaders(String name) {
                    return "Authorization".equalsIgnoreCase(name)
                            ? Collections.enumeration(Collections.singleton("Bearer " + token))
                            : super.getHeaders(name);
                }

                @Override
                public Enumeration<String> getHeaderNames() {
                    java.util.LinkedHashSet<String> names = new java.util.LinkedHashSet<>();
                    Enumeration<String> existing = super.getHeaderNames();
                    if (existing != null) {
                        while (existing.hasMoreElements()) names.add(existing.nextElement());
                    }
                    names.add("Authorization");
                    return Collections.enumeration(names);
                }
            }, response);
        } finally {
            SecurityContextHolder.setContext(previousContext);
        }
    }

    public static String internalRefreshToken(HttpServletRequest request) {
        Object value = request.getAttribute(INTERNAL_REFRESH_TOKEN_ATTRIBUTE);
        return value instanceof String token && !token.isBlank() ? token : null;
    }

    private static String attribute(HttpSession session, String name) {
        Object value = session.getAttribute(name);
        return value instanceof String text && !text.isBlank() ? text : null;
    }
}

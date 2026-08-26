package com.cpf.security.session.jdbc;

import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.security.context.CpfSessionContext;
import com.cpf.security.api.util.CpfHashes;
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

/**
 * BFF Session과 서버 내부 Credential Vault를 연결하는 Security Owner 경계입니다.
 *
 * <p>Core Context에는 인증된 공통 identity만 유지하고, sessionId/generation/만료시각 같은 Session 전용
 * 메타데이터는 {@link CpfSessionContext}로 현재 Servlet request에만 보관하며 원문 Session ID는 SHA-256 reference로 변환합니다. Access/Refresh Token은
 * Context에 저장하지 않으며 기존 Vault 경계 밖으로 노출하지 않습니다.</p>
 */
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
        return !(path.startsWith("/adm/api/"));
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
        HttpSession activeSession = java.util.Objects.requireNonNull(session, "BFF session");

        CpfBffCredential credential = vault.find(handle).orElse(null);
        if (credential == null) {
            activeSession.removeAttribute(CREDENTIAL_HANDLE);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "BFF credential is not available");
            return;
        }

        String principal = attribute(activeSession, PRINCIPAL_ID);
        if (principal == null) {
            vault.revoke(handle);
            activeSession.removeAttribute(CREDENTIAL_HANDLE);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "BFF session principal is not available");
            return;
        }

        CpfContextSnapshot currentSnapshot = CpfContexts.snapshot();
        Instant issuedAt = Instant.ofEpochMilli(activeSession.getCreationTime());
        Instant lastAccessed = Instant.ofEpochMilli(activeSession.getLastAccessedTime());
        Instant expiresAt = lastAccessed.plusSeconds(Math.max(0, activeSession.getMaxInactiveInterval()));
        CpfSessionContext cpfSession = new CpfSessionContext(
                CpfHashes.sha256(activeSession.getId()), 0L, principal, issuedAt, lastAccessed, expiresAt, null,
                currentSnapshot == null || currentSnapshot.context().identity() == null
                        ? null : currentSnapshot.context().identity().authenticationContextId(),
                null, CpfSessionContext.State.ACTIVE);

        Object previousSessionContext = request.getAttribute(CpfSessionContext.REQUEST_ATTRIBUTE);
        request.setAttribute(CpfSessionContext.REQUEST_ATTRIBUTE, cpfSession);
        SecurityContext previousContext = SecurityContextHolder.getContext();
        SecurityContext bffContext = SecurityContextHolder.createEmptyContext();
        bffContext.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(principal, null, List.of()));
        SecurityContextHolder.setContext(bffContext);
        try {
            Instant now = Instant.now();
            if (path.endsWith("/auth/refresh")) {
                if (credential.refreshToken() == null || credential.refreshExpired(now)) {
                    vault.revoke(handle);
                    activeSession.removeAttribute(CREDENTIAL_HANDLE);
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
                    if (existing != null) while (existing.hasMoreElements()) names.add(existing.nextElement());
                    names.add("Authorization");
                    return Collections.enumeration(names);
                }
            }, response);
        } finally {
            SecurityContextHolder.setContext(previousContext);
            if (previousSessionContext == null) request.removeAttribute(CpfSessionContext.REQUEST_ATTRIBUTE);
            else request.setAttribute(CpfSessionContext.REQUEST_ATTRIBUTE, previousSessionContext);
        }
    }

    /** 현재 BFF request의 Session Owner Context를 조회합니다. */
    public static CpfSessionContext currentSessionContext(HttpServletRequest request) {
        Object value = request.getAttribute(CpfSessionContext.REQUEST_ATTRIBUTE);
        return value instanceof CpfSessionContext context ? context : null;
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

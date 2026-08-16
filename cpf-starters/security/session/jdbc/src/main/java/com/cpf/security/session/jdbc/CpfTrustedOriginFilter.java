package com.cpf.security.session.jdbc;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.springframework.web.filter.OncePerRequestFilter;

/** 상태 변경 요청의 Origin/Referer를 allowlist와 대조해 CSRF 우회를 차단합니다. */
final class CpfTrustedOriginFilter extends OncePerRequestFilter {
    private final Set<String> allowedOrigins;

    CpfTrustedOriginFilter(java.util.List<String> allowedOrigins) {
        this.allowedOrigins = new HashSet<>();
        for (String origin : allowedOrigins) {
            this.allowedOrigins.add(normalize(origin));
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return safe(request.getMethod())
                || !(path.startsWith("/adm/") || path.startsWith("/api/bza/") || path.startsWith("/bza/"));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        final String normalized;
        try {
            String candidate = request.getHeader("Origin");
            if (candidate == null || candidate.isBlank()) {
                candidate = originFromReferer(request.getHeader("Referer"));
            }
            normalized = candidate == null ? null : normalize(candidate);
        } catch (IllegalArgumentException invalidOrigin) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid request origin");
            return;
        }
        if (normalized == null || (!allowedOrigins.isEmpty() && !allowedOrigins.contains(normalized))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Untrusted request origin");
            return;
        }
        if (allowedOrigins.isEmpty() && !sameOrigin(request, normalized)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Cross-origin request is prohibited");
            return;
        }
        chain.doFilter(request, response);
    }

    private static boolean sameOrigin(HttpServletRequest request, String origin) {
        String scheme = request.isSecure() ? "https" : request.getScheme().toLowerCase(Locale.ROOT);
        int port = request.getServerPort();
        String expected = scheme + "://" + request.getServerName().toLowerCase(Locale.ROOT);
        if (!(port == 80 && "http".equals(scheme)) && !(port == 443 && "https".equals(scheme))) {
            expected += ":" + port;
        }
        return expected.equals(origin);
    }

    private static String originFromReferer(String referer) {
        if (referer == null || referer.isBlank()) {
            return null;
        }
        URI uri = URI.create(referer);
        int port = uri.getPort();
        return uri.getScheme() + "://" + uri.getHost() + (port < 0 ? "" : ":" + port);
    }

    private static String normalize(String origin) {
        URI uri = URI.create(origin.trim());
        if (uri.getScheme() == null
                || uri.getHost() == null
                || uri.getUserInfo() != null
                || uri.getFragment() != null
                || uri.getQuery() != null
                || (uri.getPath() != null && !uri.getPath().isBlank() && !"/".equals(uri.getPath()))) {
            throw new IllegalArgumentException("Invalid trusted origin");
        }
        String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
        if (!("https".equals(scheme) || "http".equals(scheme))) {
            throw new IllegalArgumentException("Unsupported origin scheme");
        }
        int port = uri.getPort();
        boolean defaultPort = (port == 80 && "http".equals(scheme))
                || (port == 443 && "https".equals(scheme));
        return scheme + "://" + uri.getHost().toLowerCase(Locale.ROOT)
                + (port < 0 || defaultPort ? "" : ":" + port);
    }

    private static boolean safe(String method) {
        return "GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method);
    }
}

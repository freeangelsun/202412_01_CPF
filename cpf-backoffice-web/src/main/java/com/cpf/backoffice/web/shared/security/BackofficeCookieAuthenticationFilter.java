package com.cpf.backoffice.web.shared.security;

import com.cpf.backoffice.web.shared.config.BackofficeWebProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * MBW Backoffice BFF의 Browser 진입 경계를 구성합니다.
 *
 * <p>이 Filter는 HttpOnly Access Cookie의 존재만 BFF-local authenticated context로 승격합니다.
 * 토큰의 서명·만료·업무권한은 {@code BusinessApiHttpClient}가 동일 Cookie를 MBW Owner에 전달한 뒤
 * MBW Security Runtime이 최종 판정합니다. 따라서 Browser가 임의 Bearer Header를 주입하는 우회 경로를 만들지 않습니다.</p>
 */
public final class BackofficeCookieAuthenticationFilter extends OncePerRequestFilter {
    private final BackofficeWebProperties properties;

    public BackofficeCookieAuthenticationFilter(BackofficeWebProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null && hasAccessCookie(request)) {
            var authentication = UsernamePasswordAuthenticationToken.authenticated(
                    "MBW_BROWSER_SESSION", "N/A", List.of());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        chain.doFilter(request, response);
    }

    private boolean hasAccessCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return false;
        String expected = properties.accessCookieName();
        for (Cookie cookie : cookies) {
            if (expected.equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) return true;
        }
        return false;
    }
}

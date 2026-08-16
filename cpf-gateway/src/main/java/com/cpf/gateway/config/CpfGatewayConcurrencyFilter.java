package com.cpf.gateway.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Gateway Data Plane의 동시 요청 수를 설치 안전 상한으로 제한하는 fail-fast Bulkhead입니다.
 *
 * <p>Actuator와 분리 Control Plane은 Data Plane 포화에 같이 잠기지 않도록 대상에서 제외합니다.
 * 상한 초과 시 Queue를 무한 증가시키지 않고 503과 Retry-After를 반환합니다.</p>
 */
@Component
final class CpfGatewayConcurrencyFilter extends OncePerRequestFilter {
    private final CpfGatewaySafetyProperties safety;
    private final AtomicInteger inFlight = new AtomicInteger();

    CpfGatewayConcurrencyFilter(CpfGatewaySafetyProperties safety) {
        this.safety = safety;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator/") || path.startsWith("/internal/") || path.startsWith("/api/gateway/control/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        int current = inFlight.incrementAndGet();
        try {
            if (current > safety.getMaxConcurrentRequestsCap()) {
                response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
                response.setHeader(HttpHeaders.RETRY_AFTER,
                        Long.toString(Math.max(1L, safety.getMaintenanceRetryAfter().toSeconds())));
                response.setHeader("X-Cpf-Gateway-Rejection", "CONCURRENCY_LIMIT");
                return;
            }
            chain.doFilter(request, response);
        } finally {
            inFlight.decrementAndGet();
        }
    }
}

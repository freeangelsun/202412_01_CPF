package com.cpf.batch.control.security;

import com.cpf.batch.api.BatControlHeaders;
import com.cpf.core.api.util.CpfHeaders;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * BAT Control Server의 transport 인증 경계입니다.
 *
 * <p>prod/stg에서는 TLS가 검증한 client certificate subject와 설정된 caller mapping이 일치해야
 * 합니다. local/test에서는 loopback 또는 명시한 peer만 개발 신원으로 허용합니다. Body에 포함된
 * requestUser/requestedBy/approvedBy는 이 필터의 인증 자료가 아닙니다.</p>
 */
public final class BatControlAuthenticationFilter extends OncePerRequestFilter {
    private static final Set<String> LOCAL_PROFILES = Set.of("local", "test");
    private static final Set<String> PRODUCT_PROFILES = Set.of("prod", "stg");

    private final Set<String> activeProfiles;
    private final Set<String> trustedPeerAddresses;
    private final Map<String, String> trustedClientIdentities;

    public BatControlAuthenticationFilter(Environment environment) {
        this.activeProfiles = Arrays.stream(environment.getActiveProfiles())
                .map(value -> value.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
        this.trustedPeerAddresses = split(environment.getProperty(
                "cpf.batch.security.trusted-peer-addresses", ""));
        this.trustedClientIdentities = parseClientIdentities(environment.getProperty(
                "cpf.batch.security.trusted-client-identities", ""));
        if (isProduct() && trustedClientIdentities.isEmpty()) {
            throw new IllegalStateException(
                    "BAT prod/stg requires cpf.batch.security.trusted-client-identities "
                            + "in CALLER=certificate-subject format");
        }
        if (isProduct()
                && (!environment.getProperty("server.ssl.enabled", Boolean.class, false)
                || !"need".equalsIgnoreCase(
                        environment.getProperty("server.ssl.client-auth", "")))) {
            throw new IllegalStateException("BAT prod/stg requires TLS with client-auth=need");
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return HttpMethod.OPTIONS.matches(request.getMethod())
                || path.equals("/actuator/health")
                || path.startsWith("/actuator/health/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String callerService = normalized(request.getHeader(CpfHeaders.callerService()));
        String callerInstance = normalized(request.getHeader(CpfHeaders.callerInstanceId()));
        String certificateSubject = certificateSubject(request);

        if (isProduct()) {
            if (callerService == null || callerInstance == null || certificateSubject == null) {
                unauthorized(response, "BAT mTLS caller identity is required");
                return;
            }
            String expectedSubject = trustedClientIdentities.get(callerService.toUpperCase(Locale.ROOT));
            if (expectedSubject == null || !expectedSubject.equals(certificateSubject)) {
                forbidden(response, "BAT client certificate is not approved for caller");
                return;
            }
        } else if (isLocal()) {
            if (!isLoopback(request.getRemoteAddr())
                    && !trustedPeerAddresses.contains(request.getRemoteAddr())) {
                forbidden(response, "BAT local/test caller address is not trusted");
                return;
            }
            if (callerService == null) {
                callerService = "LOCAL";
            }
            if (callerInstance == null) {
                callerInstance = "local";
            }
        } else {
            unauthorized(response, "BAT authentication profile is not configured");
            return;
        }

        if (request.getRequestURI().startsWith("/bat/internal/")
                && !"ADM".equalsIgnoreCase(callerService)) {
            forbidden(response, "BAT internal Owner API only accepts authenticated ADM callers");
            return;
        }

        String operatorId = normalized(request.getHeader(CpfHeaders.operatorId()));
        if ("ADM".equalsIgnoreCase(callerService) && operatorId == null) {
            unauthorized(response, "BAT ADM caller requires a verified operator identity");
            return;
        }
        if (operatorId == null) {
            operatorId = certificateSubject == null ? callerService : certificateSubject;
        }
        BatAuthenticatedIdentity identity = new BatAuthenticatedIdentity(
                operatorId,
                callerService,
                callerInstance,
                certificateSubject,
                normalized(request.getHeader(BatControlHeaders.APPROVAL_REQUEST_ID)),
                normalized(request.getHeader(BatControlHeaders.APPROVAL_REQUESTER_ID)));

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("BAT_AUTHENTICATED"));
        if ("ADM".equalsIgnoreCase(callerService)) {
            authorities.add(new SimpleGrantedAuthority("BAT_CALLER_ADM"));
        } else {
            authorities.add(new SimpleGrantedAuthority("BAT_RUNTIME"));
        }
        UsernamePasswordAuthenticationToken authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        operatorId,
                        certificateSubject == null ? "LOCAL_TRUST_BOUNDARY" : certificateSubject,
                        authorities);
        authentication.setDetails(identity);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        try {
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private boolean isProduct() {
        return activeProfiles.stream().anyMatch(PRODUCT_PROFILES::contains);
    }

    private boolean isLocal() {
        return activeProfiles.stream().anyMatch(LOCAL_PROFILES::contains);
    }

    private static String certificateSubject(HttpServletRequest request) {
        Object value = request.getAttribute("jakarta.servlet.request.X509Certificate");
        if (!(value instanceof X509Certificate[] certificates) || certificates.length == 0) {
            return null;
        }
        return certificates[0].getSubjectX500Principal().getName();
    }

    private static Map<String, String> parseClientIdentities(String configured) {
        Map<String, String> mappings = new LinkedHashMap<>();
        if (configured == null || configured.isBlank()) {
            return Map.of();
        }
        for (String entry : configured.split(";")) {
            int separator = entry.indexOf('=');
            if (separator < 1 || separator == entry.length() - 1) {
                throw new IllegalArgumentException(
                        "cpf.batch.security.trusted-client-identities must use CALLER=certificate-subject");
            }
            String caller = entry.substring(0, separator).trim().toUpperCase(Locale.ROOT);
            String subject = entry.substring(separator + 1).trim();
            if (caller.isBlank() || subject.isBlank() || mappings.putIfAbsent(caller, subject) != null) {
                throw new IllegalArgumentException("Duplicate or blank BAT trusted client identity");
            }
        }
        return Map.copyOf(mappings);
    }

    private static Set<String> split(String configured) {
        if (configured == null || configured.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(configured.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toUnmodifiableSet());
    }

    private static boolean isLoopback(String address) {
        return "127.0.0.1".equals(address)
                || "0:0:0:0:0:0:0:1".equals(address)
                || "::1".equals(address);
    }

    private static String normalized(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
    }

    private static void forbidden(HttpServletResponse response, String message) throws IOException {
        response.sendError(HttpServletResponse.SC_FORBIDDEN, message);
    }
}

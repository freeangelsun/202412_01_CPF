package com.cpf.starter.security.resource;

import java.util.*;
import java.util.function.Supplier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;

/** 인증 사용자/테넌트/권한 조회와 안전한 JWT claim 접근을 단순화하는 CPF convenience API입니다. */
public final class CpfSecurityContext {
    private final CpfResourceServerProperties properties;
    private final Supplier<SecurityContext> contextSupplier;
    CpfSecurityContext(CpfResourceServerProperties properties, Supplier<SecurityContext> contextSupplier) {
        this.properties = Objects.requireNonNull(properties); this.contextSupplier = Objects.requireNonNull(contextSupplier);
    }
    public Optional<String> currentUserId() { return safeClaim(properties.getUserIdClaim()).map(String::valueOf).filter(v -> !v.isBlank()); }
    public Optional<String> currentTenantId() { return safeClaim(properties.getTenantIdClaim()).map(String::valueOf).filter(v -> !v.isBlank()); }
    public Optional<Authentication> currentPrincipal() {
        Authentication a=contextSupplier.get().getAuthentication();
        return a == null || !a.isAuthenticated() ? Optional.empty() : Optional.of(a);
    }
    public boolean hasRole(String role) { return hasAuthority("ROLE_" + normalize(role)); }
    public boolean hasScope(String scope) { return hasAuthority("SCOPE_" + normalize(scope)); }
    public Optional<Object> safeClaim(String name) {
        if (name == null || name.isBlank()) return Optional.empty();
        if (!properties.getSafeClaimNames().contains(name)) return Optional.empty();
        return currentPrincipal().map(Authentication::getPrincipal).filter(Jwt.class::isInstance).map(Jwt.class::cast)
                .map(jwt -> jwt.getClaims().get(name)).filter(CpfSecurityContext::safeValue);
    }
    /** Downstream OAuth2 propagation용 Bearer header. 로그/Evidence에 원문을 남기지 마십시오. */
    public Optional<String> authorizationHeader() {
        return currentPrincipal().map(Authentication::getPrincipal).filter(Jwt.class::isInstance).map(Jwt.class::cast)
                .map(jwt -> "Bearer " + jwt.getTokenValue());
    }
    private boolean hasAuthority(String required) { return currentPrincipal().stream().flatMap(a -> a.getAuthorities().stream()).map(GrantedAuthority::getAuthority).anyMatch(required::equals); }
    private static String normalize(String v) { if (v==null || v.isBlank()) throw new IllegalArgumentException("authority must not be blank"); return v.trim(); }
    private static boolean safeValue(Object value) { return value instanceof String || value instanceof Number || value instanceof Boolean || value instanceof UUID; }
}

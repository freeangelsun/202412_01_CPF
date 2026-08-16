package com.cpf.security.resourceserver;

import java.util.*;
import com.cpf.core.api.context.CpfContexts;
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
    public Optional<String> currentUserId() { var c=CpfContexts.current(); if(c!=null&&c.identity()!=null&&c.identity().subjectId()!=null)return Optional.of(c.identity().subjectId()); return safeClaim(properties.getUserIdClaim()).map(String::valueOf).filter(v -> !v.isBlank()); }
    public Optional<String> currentTenantId() { var c=CpfContexts.current(); if(c!=null&&c.tenant()!=null&&c.tenant().tenantId()!=null)return Optional.of(c.tenant().tenantId()); return safeClaim(properties.getTenantIdClaim()).map(String::valueOf).filter(v -> !v.isBlank()); }
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
    /** Raw bearer propagation is forbidden. Use a dedicated OAuth2 client-credentials boundary. */
    @Deprecated(forRemoval = true)
    public Optional<String> authorizationHeader() { return Optional.empty(); }
    private boolean hasAuthority(String required) { return currentPrincipal().stream().flatMap(a -> a.getAuthorities().stream()).map(GrantedAuthority::getAuthority).anyMatch(required::equals); }
    private static String normalize(String v) { if (v==null || v.isBlank()) throw new IllegalArgumentException("authority must not be blank"); return v.trim(); }
    private static boolean safeValue(Object value) { return value instanceof String || value instanceof Number || value instanceof Boolean || value instanceof UUID; }
}

package com.cpf.security.oidc;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/** Keycloak/Entra ID/Okta의 표준 OIDC claims를 provider SDK 없이 CPF user/tenant/authority context로 매핑합니다. */
public final class CpfOidcPrincipalMapper {
    private final CpfOidcProperties properties;
    public CpfOidcPrincipalMapper(CpfOidcProperties properties) { this.properties = Objects.requireNonNull(properties); }

    public CpfOidcPrincipal map(OidcUser user) {
        Objects.requireNonNull(user, "user");
        String userId = scalar(resolve(user.getClaims(), properties.getUserIdClaim()));
        if (userId == null) throw new IllegalStateException("OIDC user id claim missing: " + properties.getUserIdClaim());
        String tenantId = scalar(resolve(user.getClaims(), properties.getTenantClaim()));
        Set<String> authorities = new LinkedHashSet<>();
        addAuthorities(authorities, resolve(user.getClaims(), properties.getGroupClaim()), properties.getRolePrefix());
        addAuthorities(authorities, resolve(user.getClaims(), properties.getRoleClaim()), properties.getRolePrefix());
        addScopes(authorities, resolve(user.getClaims(), properties.getScopeClaim()), properties.getScopePrefix());

        Map<String,Object> safeClaims = new LinkedHashMap<>();
        for (String name : properties.getSafeClaimNames()) {
            Object value = resolve(user.getClaims(), name);
            if (safeValue(value)) safeClaims.put(name, value);
        }
        return new CpfOidcPrincipal(userId, tenantId, authorities, safeClaims);
    }

    private static void addAuthorities(Set<String> out, Object value, String prefix) {
        if (value instanceof Collection<?> values) for (Object item : values) add(out, item, prefix);
        else add(out, value, prefix);
    }
    private static void addScopes(Set<String> out, Object value, String prefix) {
        if (value instanceof String text) for (String scope : text.split("\\s+")) add(out, scope, prefix);
        else addAuthorities(out, value, prefix);
    }
    private static void add(Set<String> out, Object value, String prefix) {
        String text = scalar(value);
        if (text != null) out.add(text.startsWith(prefix) ? text : prefix + text);
    }
    private static Object resolve(Map<String,Object> claims, String path) {
        Object current = claims;
        for (String segment : path.split("[.]")) {
            if (!(current instanceof Map<?,?> map)) return null;
            current = map.get(segment);
        }
        return current;
    }
    private static String scalar(Object value) {
        if (value == null) return null;
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }
    private static boolean safeValue(Object value) {
        if (value == null) return false;
        if (value instanceof String || value instanceof Number || value instanceof Boolean) return true;
        if (value instanceof Collection<?> c) return c.size() <= 32 && c.stream().allMatch(CpfOidcPrincipalMapper::safeValue);
        return false;
    }
}

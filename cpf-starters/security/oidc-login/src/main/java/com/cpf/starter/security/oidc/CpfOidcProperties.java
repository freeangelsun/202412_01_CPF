package com.cpf.starter.security.oidc;
import com.cpf.core.api.config.CpfConfigMutability;
import com.cpf.core.api.config.CpfConfigPolicy;

import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** OIDC provider SDK 없이 claim mapping을 구성하는 typed properties입니다. */
@CpfConfigPolicy(prefix="cpf.security.oidc", mutability=CpfConfigMutability.RESTART_REQUIRED, secretSeparated=true)
@ConfigurationProperties("cpf.security.oidc")
public class CpfOidcProperties {
    private boolean enabled;
    private String userIdClaim = "sub";
    private String tenantClaim = "tenant_id";
    private String groupClaim = "groups";
    private String roleClaim = "realm_access.roles";
    private String scopeClaim = "scope";
    private String rolePrefix = "ROLE_";
    private String scopePrefix = "SCOPE_";
    private String postLogoutRedirectUri = "{baseUrl}";
    private Set<String> safeClaimNames = new LinkedHashSet<>(Set.of("sub", "iss", "aud", "tenant_id"));

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean value) { enabled = value; }
    public String getUserIdClaim() { return userIdClaim; }
    public void setUserIdClaim(String value) { userIdClaim = required(value, "userIdClaim"); }
    public String getTenantClaim() { return tenantClaim; }
    public void setTenantClaim(String value) { tenantClaim = required(value, "tenantClaim"); }
    public String getGroupClaim() { return groupClaim; }
    public void setGroupClaim(String value) { groupClaim = required(value, "groupClaim"); }
    public String getRoleClaim() { return roleClaim; }
    public void setRoleClaim(String value) { roleClaim = required(value, "roleClaim"); }
    public String getScopeClaim() { return scopeClaim; }
    public void setScopeClaim(String value) { scopeClaim = required(value, "scopeClaim"); }
    public String getRolePrefix() { return rolePrefix; }
    public void setRolePrefix(String value) { rolePrefix = required(value, "rolePrefix"); }
    public String getScopePrefix() { return scopePrefix; }
    public void setScopePrefix(String value) { scopePrefix = required(value, "scopePrefix"); }
    public String getPostLogoutRedirectUri() { return postLogoutRedirectUri; }
    public void setPostLogoutRedirectUri(String value) { postLogoutRedirectUri = required(value, "postLogoutRedirectUri"); }
    public Set<String> getSafeClaimNames() { return Set.copyOf(safeClaimNames); }
    public void setSafeClaimNames(Set<String> values) {
        if (values == null) throw new IllegalArgumentException("safeClaimNames must not be null");
        var safe = new LinkedHashSet<String>();
        for (String value : values) safe.add(required(value, "safeClaimNames"));
        safeClaimNames = safe;
    }

    private static String required(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " must not be blank");
        return value.trim();
    }
}

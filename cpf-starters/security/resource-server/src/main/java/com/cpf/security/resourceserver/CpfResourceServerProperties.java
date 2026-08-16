package com.cpf.security.resourceserver;
import com.cpf.core.api.config.CpfConfigMutability;
import com.cpf.core.api.config.CpfConfigPolicy;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;

@CpfConfigPolicy(prefix="cpf.security.resource-server", mutability=CpfConfigMutability.RESTART_REQUIRED, secretSeparated=true)
@ConfigurationProperties("cpf.security.resource-server")
/** CpfResourceServerProperties 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public class CpfResourceServerProperties {
    private boolean enabled;
    private String issuerUri;
    private String jwkSetUri;
    private List<String> audiences = new ArrayList<>();
    private List<String> publicPaths = new ArrayList<>(List.of("/actuator/health/**"));
    private Duration clockSkew = Duration.ofSeconds(60);
    /** 설정값의 의미와 기본 동작을 명확히 하여 운영 설정 영향을 추적한다. */
    private String userIdClaim = "sub";
    private String tenantIdClaim = "tenant_id";
    private Set<String> safeClaimNames = new LinkedHashSet<>(Set.of("sub", "tenant_id", "scope"));

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getIssuerUri() { return issuerUri; }
    public void setIssuerUri(String issuerUri) { this.issuerUri = issuerUri; }
    public String getJwkSetUri() { return jwkSetUri; }
    public void setJwkSetUri(String jwkSetUri) { this.jwkSetUri = jwkSetUri; }
    public List<String> getAudiences() { return audiences; }
    public void setAudiences(List<String> audiences) { this.audiences = audiences == null ? new ArrayList<>() : new ArrayList<>(audiences); }
    public List<String> getPublicPaths() { return publicPaths; }
    public void setPublicPaths(List<String> publicPaths) { this.publicPaths = publicPaths == null ? new ArrayList<>() : new ArrayList<>(publicPaths); }
    public String getUserIdClaim() { return userIdClaim; }
    public void setUserIdClaim(String userIdClaim) { this.userIdClaim = requireClaim(userIdClaim, "user-id-claim"); }
    public String getTenantIdClaim() { return tenantIdClaim; }
    public void setTenantIdClaim(String tenantIdClaim) { this.tenantIdClaim = requireClaim(tenantIdClaim, "tenant-id-claim"); }
    public Set<String> getSafeClaimNames() { return Set.copyOf(safeClaimNames); }
    public void setSafeClaimNames(Set<String> values) {
        if (values == null) throw new IllegalArgumentException("safe-claim-names must not be null");
        var safe = new LinkedHashSet<String>();
        for (String value : values) safe.add(requireClaim(value, "safe-claim-names"));
        safeClaimNames = safe;
    }
    public Duration getClockSkew() { return clockSkew; }
    public void setClockSkew(Duration clockSkew) { this.clockSkew = clockSkew; }

    private static String requireClaim(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " must not be blank");
        return value.trim();
    }

    /** validate 작업을 CPF 표준 계약에 따라 수행한다. */
    public void validate() {
        if (!enabled) return;
        boolean issuer = issuerUri != null && !issuerUri.isBlank();
        boolean jwk = jwkSetUri != null && !jwkSetUri.isBlank();
        if (issuer == jwk) throw new IllegalStateException("Exactly one of issuer-uri or jwk-set-uri is required");
        if (clockSkew == null || clockSkew.isNegative() || clockSkew.compareTo(Duration.ofMinutes(10)) > 0) {
            throw new IllegalStateException("clock-skew must be between 0 and 10 minutes");
        }
    }
}

package com.cpf.starter.security.resource;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("cpf.security.resource-server")
public class CpfResourceServerProperties {
    private boolean enabled;
    private String issuerUri;
    private String jwkSetUri;
    private List<String> audiences = new ArrayList<>();
    private List<String> publicPaths = new ArrayList<>(List.of("/actuator/health/**"));
    private Duration clockSkew = Duration.ofSeconds(60);

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
    public Duration getClockSkew() { return clockSkew; }
    public void setClockSkew(Duration clockSkew) { this.clockSkew = clockSkew; }

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

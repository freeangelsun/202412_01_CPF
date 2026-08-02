package com.cpf.starter.base;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("cpf.starter.base")
public class CpfBaseProperties {
    private boolean strict = true;
    private String profileId = "MINIMAL_BOOT_DOMAIN";
    private String profileVersion = "1.0";

    public boolean isStrict() { return strict; }
    public void setStrict(boolean strict) { this.strict = strict; }
    public String getProfileId() { return profileId; }
    public void setProfileId(String profileId) { this.profileId = profileId; }
    public String getProfileVersion() { return profileVersion; }
    public void setProfileVersion(String profileVersion) { this.profileVersion = profileVersion; }

    public void validate() {
        if (profileId == null || profileId.isBlank()) throw new IllegalStateException("cpf.starter.base.profile-id is required");
        if (profileVersion == null || profileVersion.isBlank()) throw new IllegalStateException("cpf.starter.base.profile-version is required");
    }
}

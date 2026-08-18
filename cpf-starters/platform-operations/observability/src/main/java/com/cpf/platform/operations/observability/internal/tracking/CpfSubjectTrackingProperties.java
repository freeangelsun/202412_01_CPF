package com.cpf.platform.operations.observability.internal.tracking;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Subject protected-index Runtime 설정입니다. Secret 값 자체는 이 설정에 저장하지 않습니다. */
@ConfigurationProperties("cpf.tracking.subject")
public class CpfSubjectTrackingProperties {
    private boolean enabled = true;
    private boolean failOnStoreUnavailable;
    private List<String> readableKeyVersions = new ArrayList<>();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isFailOnStoreUnavailable() { return failOnStoreUnavailable; }
    public void setFailOnStoreUnavailable(boolean value) { this.failOnStoreUnavailable = value; }
    public List<String> getReadableKeyVersions() { return List.copyOf(readableKeyVersions); }
    public void setReadableKeyVersions(List<String> versions) {
        this.readableKeyVersions = versions == null ? new ArrayList<>() : new ArrayList<>(versions);
    }
}

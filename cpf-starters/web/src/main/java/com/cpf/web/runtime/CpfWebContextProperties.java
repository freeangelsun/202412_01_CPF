package com.cpf.web.runtime;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/** Owner-declared non-business management boundaries for the canonical Web context filter. */
@ConfigurationProperties("cpf.web.context")
public final class CpfWebContextProperties {
    private List<String> managementRootPaths = new ArrayList<>();

    public List<String> getManagementRootPaths() {
        return List.copyOf(managementRootPaths);
    }

    public void setManagementRootPaths(List<String> managementRootPaths) {
        this.managementRootPaths = managementRootPaths == null
                ? new ArrayList<>()
                : new ArrayList<>(managementRootPaths);
    }
}

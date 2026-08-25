package com.cpf.web.runtime;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/** CPF Web Context Filter에서 업무 거래 Context를 강제하지 않을 관리 경로 경계를 설정합니다. */
@ConfigurationProperties("cpf.web.context")
public final class CpfWebContextProperties {
    /** 업무 거래 Header/Context 강제 대상에서 제외할 관리 API Root 경로 목록입니다. */
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

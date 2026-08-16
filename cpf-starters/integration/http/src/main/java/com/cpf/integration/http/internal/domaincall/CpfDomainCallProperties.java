package com.cpf.integration.http.internal.domaincall;

import com.cpf.core.api.domain.CpfDomainBindingMode;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 논리 Domain/SystemCode의 Local/Remote Runtime Binding 설정입니다.
 * 실제 IP/URL은 이 설정이 아니라 Service Registry/Endpoint Binding이 소유합니다.
 */
@ConfigurationProperties(prefix = "cpf.integration.domain-call")
public class CpfDomainCallProperties {
    /** systemCode별 Binding. 예: EXS.mode=REMOTE, EXS.service-id=EXS-SERVICE */
    private Map<String, Binding> bindings = new LinkedHashMap<>();

    public Map<String, Binding> getBindings() { return bindings; }
    public void setBindings(Map<String, Binding> bindings) {
        this.bindings = bindings == null ? new LinkedHashMap<>() : new LinkedHashMap<>(bindings);
    }

    /** 개별 논리 Domain의 topology 선택과 Registry serviceId입니다. */
    public static class Binding {
        /** AUTO는 동일 JVM operation이 있으면 LOCAL, 없으면 REMOTE를 선택합니다. */
        private CpfDomainBindingMode mode = CpfDomainBindingMode.AUTO;
        /** REMOTE/AUTO 원격 fallback에서 조회할 논리 Service Registry ID입니다. */
        private String serviceId;
        public CpfDomainBindingMode getMode() { return mode; }
        public void setMode(CpfDomainBindingMode mode) { this.mode = mode == null ? CpfDomainBindingMode.AUTO : mode; }
        public String getServiceId() { return serviceId; }
        public void setServiceId(String serviceId) { this.serviceId = serviceId; }
    }
}

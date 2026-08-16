package com.cpf.data.persistence.runtime;
import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties("cpf.data.persistence.repository-policy")
/** CpfRepositoryPolicyProperties 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public class CpfRepositoryPolicyProperties {
    private boolean enabled=true;
    public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
}

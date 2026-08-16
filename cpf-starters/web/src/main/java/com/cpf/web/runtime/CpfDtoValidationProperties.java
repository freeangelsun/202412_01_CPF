package com.cpf.web.runtime;
import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties("cpf.web.dto-validation")
/** CpfDtoValidationProperties 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public class CpfDtoValidationProperties {
    private boolean enabled=true;
    public boolean isEnabled(){return enabled;}
    public void setEnabled(boolean enabled){this.enabled=enabled;}
}

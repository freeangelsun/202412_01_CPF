package com.cpf.security.secret;
import com.cpf.core.api.config.CpfConfigMutability;
import com.cpf.core.api.config.CpfConfigPolicy;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
/** KMS/HSM operation timeout. */
@CpfConfigPolicy(prefix="cpf.security.key", mutability=CpfConfigMutability.RESTART_REQUIRED, secretSeparated=true)
@ConfigurationProperties("cpf.security.key")
/** CpfKeyOperationProperties 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public class CpfKeyOperationProperties {
    private Duration operationTimeout=Duration.ofSeconds(3);
    public Duration getOperationTimeout(){return operationTimeout;}
    public void setOperationTimeout(Duration v){if(v==null||v.isZero()||v.isNegative())throw new IllegalArgumentException("operationTimeout must be positive");operationTimeout=v;}
}

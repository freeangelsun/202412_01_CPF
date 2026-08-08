package com.cpf.starter.integration.soap;
import com.cpf.core.api.config.CpfConfigMutability;
import com.cpf.core.api.config.CpfConfigPolicy;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
/** SOAP client safe defaults. URI/action은 호출 시 명시하고 timeout만 공통화합니다. */
@CpfConfigPolicy(prefix="cpf.integration.soap", mutability=CpfConfigMutability.RESTART_REQUIRED, secretSeparated=false)
@ConfigurationProperties("cpf.integration.soap")
public class CpfSoapProperties {
    private Duration connectTimeout=Duration.ofSeconds(2);
    private Duration readTimeout=Duration.ofSeconds(5);
    public Duration getConnectTimeout(){return connectTimeout;}
    public void setConnectTimeout(Duration v){if(v==null||v.isZero()||v.isNegative())throw new IllegalArgumentException("connectTimeout must be positive");connectTimeout=v;}
    public Duration getReadTimeout(){return readTimeout;}
    public void setReadTimeout(Duration v){if(v==null||v.isZero()||v.isNegative())throw new IllegalArgumentException("readTimeout must be positive");readTimeout=v;}
}

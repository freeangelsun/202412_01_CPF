package com.cpf.security.serviceidentity;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties("cpf.security.service-identity")
/** CpfServiceIdentityProperties 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public class CpfServiceIdentityProperties {
    private boolean enabled;
    private String serviceId;
    private String activeKeyId;
    private String activeSecret;
    private String previousKeyId;
    private String previousSecret;
    /** 설정값의 의미와 기본 동작을 명확히 하여 운영 설정 영향을 추적한다. */
    private Duration ttl = Duration.ofMinutes(2);
    private Duration clockSkew = Duration.ofSeconds(30);
    public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
    public String getServiceId(){return serviceId;} public void setServiceId(String v){serviceId=v;}
    public String getActiveKeyId(){return activeKeyId;} public void setActiveKeyId(String v){activeKeyId=v;}
    public String getActiveSecret(){return activeSecret;} public void setActiveSecret(String v){activeSecret=v;}
    public String getPreviousKeyId(){return previousKeyId;} public void setPreviousKeyId(String v){previousKeyId=v;}
    public String getPreviousSecret(){return previousSecret;} public void setPreviousSecret(String v){previousSecret=v;}
    public Duration getTtl(){return ttl;} public void setTtl(Duration v){ttl=v;}
    public Duration getClockSkew(){return clockSkew;} public void setClockSkew(Duration v){clockSkew=v;}
    /** validate 작업을 CPF 표준 계약에 따라 수행한다. */
    public void validate(){
        if(!enabled)return;
        if(serviceId==null||serviceId.isBlank()||activeKeyId==null||activeKeyId.isBlank()||activeSecret==null||activeSecret.length()<32)
            throw new IllegalStateException("service-id, active-key-id and a 32+ character secret are required");
        if(ttl==null||ttl.isNegative()||ttl.isZero()||ttl.compareTo(Duration.ofMinutes(10))>0) throw new IllegalStateException("ttl must be >0 and <=10m");
    }
}

package com.cpf.platform.operations.health;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
/** cpf.platform.health 설정입니다. */
@ConfigurationProperties("cpf.platform-operations.health")
public class CpfHealthProperties {
    private boolean enabled=true; private Duration dependencyTimeout=Duration.ofSeconds(2); private Duration cacheTtl=Duration.ofSeconds(3);
    private int maxConcurrentChecks=8; private String systemId="cpf"; private String instanceId="local"; private String version="unknown"; private String buildSha="unknown"; private boolean maintenance;
    private String reportUrl; private String reportToken; private Duration reportInterval=Duration.ofSeconds(20);
    public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
    public Duration getDependencyTimeout(){return dependencyTimeout;} public void setDependencyTimeout(Duration v){dependencyTimeout=positive(v,"dependencyTimeout");}
    public Duration getCacheTtl(){return cacheTtl;} public void setCacheTtl(Duration v){cacheTtl=nonNegative(v,"cacheTtl");}
    public int getMaxConcurrentChecks(){return maxConcurrentChecks;} public void setMaxConcurrentChecks(int v){if(v<1)throw new IllegalArgumentException("maxConcurrentChecks must be >= 1");maxConcurrentChecks=v;}
    public String getSystemId(){return systemId;} public void setSystemId(String v){systemId=text(v,"systemId");}
    public String getInstanceId(){return instanceId;} public void setInstanceId(String v){instanceId=text(v,"instanceId");}
    public String getVersion(){return version;} public void setVersion(String v){version=v;}
    public String getBuildSha(){return buildSha;} public void setBuildSha(String v){buildSha=v;}
    public boolean isMaintenance(){return maintenance;} public void setMaintenance(boolean v){maintenance=v;}
    public String getReportUrl(){return reportUrl;} public void setReportUrl(String v){reportUrl=blankToNull(v);}
    public String getReportToken(){return reportToken;} public void setReportToken(String v){reportToken=blankToNull(v);}
    public Duration getReportInterval(){return reportInterval;} public void setReportInterval(Duration v){reportInterval=positive(v,"reportInterval");}
    /** 명시 설정이 없을 때 CPF/Spring runtime이 이미 알고 있는 식별자를 사용합니다. */
    public void applyRuntimeIdentity(Environment environment){
        if(environment==null)return;
        if("cpf".equals(systemId)){
            String resolved=first(environment.getProperty("cpf.system-code"),environment.getProperty("cpf.system.id"),environment.getProperty("spring.application.name"));
            if(resolved!=null) systemId=resolved;
        }
        if("local".equals(instanceId)){
            String resolved=first(environment.getProperty("cpf.instance-id"),environment.getProperty("cpf.runtime.instance-id"),environment.getProperty("HOSTNAME"));
            if(resolved!=null) instanceId=resolved;
        }
    }
    private static String first(String... values){for(String v:values)if(v!=null&&!v.isBlank())return v.trim();return null;}
    /** ADM Health 보고에 필요한 URL과 Token이 모두 설정되었는지 반환합니다. */
    public boolean reportingEnabled(){return reportUrl!=null && reportToken!=null;}
    CpfHealthConfig toConfig(){return new CpfHealthConfig(dependencyTimeout,cacheTtl,maxConcurrentChecks,systemId,instanceId,version,buildSha,maintenance);}
    private static Duration positive(Duration v,String n){if(v==null||v.isZero()||v.isNegative())throw new IllegalArgumentException(n+" must be positive");return v;}
    private static Duration nonNegative(Duration v,String n){if(v==null||v.isNegative())throw new IllegalArgumentException(n+" must not be negative");return v;}
    private static String text(String v,String n){if(v==null||v.isBlank())throw new IllegalArgumentException(n+" required");return v.trim();}
    private static String blankToNull(String v){return v==null||v.isBlank()?null:v.trim();}
}

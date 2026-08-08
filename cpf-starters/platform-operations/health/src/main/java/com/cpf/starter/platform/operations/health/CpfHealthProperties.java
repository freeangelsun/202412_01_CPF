package com.cpf.starter.platform.operations.health;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("cpf.platform.health")
public class CpfHealthProperties {
    private boolean enabled = true;
    private Duration dependencyTimeout = Duration.ofSeconds(2);
    private Duration cacheTtl = Duration.ofSeconds(3);
    private int maxConcurrentChecks = 8;
    private String systemId = "cpf";
    private String instanceId = "local";
    private String version = "unknown";
    private String buildSha = "unknown";
    private boolean maintenance;
    private String reportUrl;
    private String reportToken;
    private Duration reportInterval = Duration.ofSeconds(20);

    public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
    public Duration getDependencyTimeout(){return dependencyTimeout;} public void setDependencyTimeout(Duration v){dependencyTimeout=positive(v,"dependencyTimeout");}
    public Duration getCacheTtl(){return cacheTtl;} public void setCacheTtl(Duration v){cacheTtl=positive(v,"cacheTtl");}
    public int getMaxConcurrentChecks(){return maxConcurrentChecks;} public void setMaxConcurrentChecks(int v){if(v<1)throw new IllegalArgumentException("maxConcurrentChecks must be >= 1");maxConcurrentChecks=v;}
    public String getSystemId(){return systemId;} public void setSystemId(String v){systemId=text(v,"systemId");}
    public String getInstanceId(){return instanceId;} public void setInstanceId(String v){instanceId=text(v,"instanceId");}
    public String getVersion(){return version;} public void setVersion(String v){version=v==null?"unknown":v;}
    public String getBuildSha(){return buildSha;} public void setBuildSha(String v){buildSha=v==null?"unknown":v;}
    public boolean isMaintenance(){return maintenance;} public void setMaintenance(boolean v){maintenance=v;}
    public String getReportUrl(){return reportUrl;} public void setReportUrl(String v){reportUrl=blankToNull(v);}
    public String getReportToken(){return reportToken;} public void setReportToken(String v){reportToken=blankToNull(v);}
    public Duration getReportInterval(){return reportInterval;} public void setReportInterval(Duration v){reportInterval=positive(v,"reportInterval");}
    public boolean reportingEnabled(){return reportUrl!=null && reportToken!=null;}
    private static Duration positive(Duration v,String n){if(v==null||v.isZero()||v.isNegative())throw new IllegalArgumentException(n+" must be positive");return v;}
    private static String text(String v,String n){if(v==null||v.isBlank())throw new IllegalArgumentException(n+" required");return v.trim();}
    private static String blankToNull(String v){return v==null||v.isBlank()?null:v.trim();}
}

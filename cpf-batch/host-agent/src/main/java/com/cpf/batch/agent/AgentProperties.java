package com.cpf.batch.agent;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.*;

@ConfigurationProperties("cpf.agent")
public class AgentProperties {
    private String artifactRepositoryBaseUrl;
    private String artifactPublicKeyPath;
    private boolean requireSignature=true;
    private long maxArtifactBytes=536870912L;
    private long maxLogArchiveBytes=1073741824L;
    private long processTimeoutSeconds=60L; private long maxProcessOutputBytes=1048576L; private long logArchiveTtlSeconds=600L;
    private List<String> allowedClientSubjects=new ArrayList<>();
    private Map<String,ServiceDefinition> services=new LinkedHashMap<>();
    public String getArtifactRepositoryBaseUrl(){return artifactRepositoryBaseUrl;} public void setArtifactRepositoryBaseUrl(String v){artifactRepositoryBaseUrl=v;}
    public String getArtifactPublicKeyPath(){return artifactPublicKeyPath;} public void setArtifactPublicKeyPath(String v){artifactPublicKeyPath=v;}
    public boolean isRequireSignature(){return requireSignature;} public void setRequireSignature(boolean v){requireSignature=v;}
    public long getMaxArtifactBytes(){return maxArtifactBytes;} public void setMaxArtifactBytes(long v){maxArtifactBytes=v;}
    public long getMaxLogArchiveBytes(){return maxLogArchiveBytes;} public void setMaxLogArchiveBytes(long v){maxLogArchiveBytes=v;}
    public long getProcessTimeoutSeconds(){return processTimeoutSeconds;} public void setProcessTimeoutSeconds(long v){processTimeoutSeconds=v;}
    public long getMaxProcessOutputBytes(){return maxProcessOutputBytes;} public void setMaxProcessOutputBytes(long v){maxProcessOutputBytes=v;}
    public long getLogArchiveTtlSeconds(){return logArchiveTtlSeconds;} public void setLogArchiveTtlSeconds(long v){logArchiveTtlSeconds=v;}
    public List<String> getAllowedClientSubjects(){return allowedClientSubjects;} public void setAllowedClientSubjects(List<String> v){allowedClientSubjects=v==null?new ArrayList<>():v;}
    public Map<String,ServiceDefinition> getServices(){return services;} public void setServices(Map<String,ServiceDefinition> v){services=v;}
    public static class ServiceDefinition {
        private String serviceId,artifactId,installRoot,logRoot,systemdUnit,windowsStartScript,healthUrl,runtimeControlUrl,runtimeMode="embedded-bootjar",environmentCode,releaseChannel="stable";
        public String getServiceId(){return serviceId;} public void setServiceId(String v){serviceId=v;}
        public String getArtifactId(){return artifactId;} public void setArtifactId(String v){artifactId=v;}
        public String getInstallRoot(){return installRoot;} public void setInstallRoot(String v){installRoot=v;}
        public String getLogRoot(){return logRoot;} public void setLogRoot(String v){logRoot=v;}
        public String getSystemdUnit(){return systemdUnit;} public void setSystemdUnit(String v){systemdUnit=v;}
        public String getWindowsStartScript(){return windowsStartScript;} public void setWindowsStartScript(String v){windowsStartScript=v;}
        public String getHealthUrl(){return healthUrl;} public void setHealthUrl(String v){healthUrl=v;}
        public String getRuntimeControlUrl(){return runtimeControlUrl;} public void setRuntimeControlUrl(String v){runtimeControlUrl=v;}
        public String getRuntimeMode(){return runtimeMode;} public void setRuntimeMode(String v){runtimeMode=v;}
        public String getEnvironmentCode(){return environmentCode;} public void setEnvironmentCode(String v){environmentCode=v;}
        public String getReleaseChannel(){return releaseChannel;} public void setReleaseChannel(String v){releaseChannel=v;}
    }
}

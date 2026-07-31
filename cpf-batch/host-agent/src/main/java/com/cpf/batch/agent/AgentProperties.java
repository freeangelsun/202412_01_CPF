package com.cpf.batch.agent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Host Agent 제품 설정입니다. 임시 디렉터리가 아니라 명시된 영속 경로를 사용합니다. */
@ConfigurationProperties("cpf.agent")
public class AgentProperties {
    private String artifactRepositoryBaseUrl;
    private String artifactPublicKeyPath;
    private String artifactStateMacKeyBase64;
    private Map<String, TrustedKey> artifactTrustStore = new LinkedHashMap<>();
    private List<String> artifactAllowedHosts = new ArrayList<>();
    private List<String> artifactPinnedAddresses = new ArrayList<>();
    private List<String> artifactAllowedCidrs = new ArrayList<>();
    private Set<Integer> artifactAllowedPorts = new LinkedHashSet<>(Set.of(443));
    private boolean allowHttpLoopback;
    private List<String> artifactAllowedContentTypes = new ArrayList<>(List.of(
            "application/java-archive", "application/octet-stream", "application/zip"));
    private boolean requireRepositoryDigestHeader = true;
    private boolean allowPrivateRepositoryAddresses;
    private String artifactProxyHost;
    private int artifactProxyPort;
    private List<String> artifactProxyPinnedAddresses = new ArrayList<>();
    private List<String> artifactProxyAllowedCidrs = new ArrayList<>();
    private boolean allowPrivateProxyAddresses;
    private int artifactConnectTimeoutSeconds = 20;
    private int artifactReadTimeoutSeconds = 300;
    private String commandLedgerRoot = "./data/agent-command-ledger";
    private boolean requireSignature = true;
    private long maxArtifactBytes = 536_870_912L;
    private long maxLogArchiveBytes = 1_073_741_824L;
    private long processTimeoutSeconds = 60L;
    private long maxProcessOutputBytes = 1_048_576L;
    private long logArchiveTtlSeconds = 600L;
    private long commandLedgerRetentionSeconds = 604_800L;
    private List<String> allowedClientSubjects = new ArrayList<>();
    private Map<String, ServiceDefinition> services = new LinkedHashMap<>();

    public String getArtifactRepositoryBaseUrl() { return artifactRepositoryBaseUrl; }
    public void setArtifactRepositoryBaseUrl(String value) { artifactRepositoryBaseUrl = value; }
    public String getArtifactPublicKeyPath() { return artifactPublicKeyPath; }
    public void setArtifactPublicKeyPath(String value) { artifactPublicKeyPath = value; }
    public String getArtifactStateMacKeyBase64() { return artifactStateMacKeyBase64; }
    public void setArtifactStateMacKeyBase64(String value) { artifactStateMacKeyBase64 = value; }
    public Map<String, TrustedKey> getArtifactTrustStore() { return artifactTrustStore; }
    public void setArtifactTrustStore(Map<String, TrustedKey> value) { artifactTrustStore = value == null ? new LinkedHashMap<>() : value; }
    public List<String> getArtifactAllowedHosts() { return artifactAllowedHosts; }
    public void setArtifactAllowedHosts(List<String> value) { artifactAllowedHosts = value == null ? new ArrayList<>() : value; }
    public List<String> getArtifactPinnedAddresses() { return artifactPinnedAddresses; }
    public void setArtifactPinnedAddresses(List<String> value) { artifactPinnedAddresses = value == null ? new ArrayList<>() : new ArrayList<>(value); }
    public List<String> getArtifactAllowedCidrs() { return artifactAllowedCidrs; }
    public void setArtifactAllowedCidrs(List<String> value) { artifactAllowedCidrs = value == null ? new ArrayList<>() : new ArrayList<>(value); }
    public Set<Integer> getArtifactAllowedPorts() { return artifactAllowedPorts; }
    public void setArtifactAllowedPorts(Set<Integer> value) { artifactAllowedPorts = value == null ? new LinkedHashSet<>() : new LinkedHashSet<>(value); }
    public boolean isAllowHttpLoopback() { return allowHttpLoopback; }
    public void setAllowHttpLoopback(boolean value) { allowHttpLoopback = value; }
    public List<String> getArtifactAllowedContentTypes() { return artifactAllowedContentTypes; }
    public void setArtifactAllowedContentTypes(List<String> value) { artifactAllowedContentTypes = value == null ? new ArrayList<>() : value; }
    public boolean isRequireRepositoryDigestHeader() { return requireRepositoryDigestHeader; }
    public void setRequireRepositoryDigestHeader(boolean value) { requireRepositoryDigestHeader = value; }
    public boolean isAllowPrivateRepositoryAddresses() { return allowPrivateRepositoryAddresses; }
    public void setAllowPrivateRepositoryAddresses(boolean value) { allowPrivateRepositoryAddresses = value; }
    public String getArtifactProxyHost() { return artifactProxyHost; }
    public void setArtifactProxyHost(String value) { artifactProxyHost = value; }
    public int getArtifactProxyPort() { return artifactProxyPort; }
    public void setArtifactProxyPort(int value) { artifactProxyPort = value; }
    public List<String> getArtifactProxyPinnedAddresses() { return artifactProxyPinnedAddresses; }
    public void setArtifactProxyPinnedAddresses(List<String> value) { artifactProxyPinnedAddresses = value == null ? new ArrayList<>() : new ArrayList<>(value); }
    public List<String> getArtifactProxyAllowedCidrs() { return artifactProxyAllowedCidrs; }
    public void setArtifactProxyAllowedCidrs(List<String> value) { artifactProxyAllowedCidrs = value == null ? new ArrayList<>() : new ArrayList<>(value); }
    public boolean isAllowPrivateProxyAddresses() { return allowPrivateProxyAddresses; }
    public void setAllowPrivateProxyAddresses(boolean value) { allowPrivateProxyAddresses = value; }
    public int getArtifactConnectTimeoutSeconds() { return artifactConnectTimeoutSeconds; }
    public void setArtifactConnectTimeoutSeconds(int value) { artifactConnectTimeoutSeconds = value; }
    public int getArtifactReadTimeoutSeconds() { return artifactReadTimeoutSeconds; }
    public void setArtifactReadTimeoutSeconds(int value) { artifactReadTimeoutSeconds = value; }
    public String getCommandLedgerRoot() { return commandLedgerRoot; }
    public void setCommandLedgerRoot(String value) { commandLedgerRoot = value; }
    public boolean isRequireSignature() { return requireSignature; }
    public void setRequireSignature(boolean value) { requireSignature = value; }
    public long getMaxArtifactBytes() { return maxArtifactBytes; }
    public void setMaxArtifactBytes(long value) { maxArtifactBytes = value; }
    public long getMaxLogArchiveBytes() { return maxLogArchiveBytes; }
    public void setMaxLogArchiveBytes(long value) { maxLogArchiveBytes = value; }
    public long getProcessTimeoutSeconds() { return processTimeoutSeconds; }
    public void setProcessTimeoutSeconds(long value) { processTimeoutSeconds = value; }
    public long getMaxProcessOutputBytes() { return maxProcessOutputBytes; }
    public void setMaxProcessOutputBytes(long value) { maxProcessOutputBytes = value; }
    public long getLogArchiveTtlSeconds() { return logArchiveTtlSeconds; }
    public void setLogArchiveTtlSeconds(long value) { logArchiveTtlSeconds = value; }
    public long getCommandLedgerRetentionSeconds() { return commandLedgerRetentionSeconds; }
    public void setCommandLedgerRetentionSeconds(long value) { commandLedgerRetentionSeconds = value; }
    public List<String> getAllowedClientSubjects() { return allowedClientSubjects; }
    public void setAllowedClientSubjects(List<String> value) { allowedClientSubjects = value == null ? new ArrayList<>() : value; }
    public Map<String, ServiceDefinition> getServices() { return services; }
    public void setServices(Map<String, ServiceDefinition> value) { services = value == null ? new LinkedHashMap<>() : value; }


    public static class TrustedKey {
        private String publicKeyPath;
        private java.time.Instant notBefore;
        private java.time.Instant notAfter;
        private boolean revoked;
        public String getPublicKeyPath() { return publicKeyPath; }
        public void setPublicKeyPath(String value) { publicKeyPath = value; }
        public java.time.Instant getNotBefore() { return notBefore; }
        public void setNotBefore(java.time.Instant value) { notBefore = value; }
        public java.time.Instant getNotAfter() { return notAfter; }
        public void setNotAfter(java.time.Instant value) { notAfter = value; }
        public boolean isRevoked() { return revoked; }
        public void setRevoked(boolean value) { revoked = value; }
    }

    public static class ServiceDefinition {
        private String serviceId;
        private String artifactId;
        private String installRoot;
        private String logRoot;
        private String systemdUnit;
        private String windowsStartScript;
        private String healthUrl;
        private String runtimeControlUrl;
        private String runtimeMode = "embedded-bootjar";
        private String environmentCode;
        private String releaseChannel = "stable";
        public String getServiceId() { return serviceId; }
        public void setServiceId(String value) { serviceId = value; }
        public String getArtifactId() { return artifactId; }
        public void setArtifactId(String value) { artifactId = value; }
        public String getInstallRoot() { return installRoot; }
        public void setInstallRoot(String value) { installRoot = value; }
        public String getLogRoot() { return logRoot; }
        public void setLogRoot(String value) { logRoot = value; }
        public String getSystemdUnit() { return systemdUnit; }
        public void setSystemdUnit(String value) { systemdUnit = value; }
        public String getWindowsStartScript() { return windowsStartScript; }
        public void setWindowsStartScript(String value) { windowsStartScript = value; }
        public String getHealthUrl() { return healthUrl; }
        public void setHealthUrl(String value) { healthUrl = value; }
        public String getRuntimeControlUrl() { return runtimeControlUrl; }
        public void setRuntimeControlUrl(String value) { runtimeControlUrl = value; }
        public String getRuntimeMode() { return runtimeMode; }
        public void setRuntimeMode(String value) { runtimeMode = value; }
        public String getEnvironmentCode() { return environmentCode; }
        public void setEnvironmentCode(String value) { environmentCode = value; }
        public String getReleaseChannel() { return releaseChannel; }
        public void setReleaseChannel(String value) { releaseChannel = value; }
    }
}

package com.cpf.batch.worker;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.*;

/**
 * 승인 Shell/File Executor의 고정 Catalog입니다.
 * Runtime 요청으로 임의 Path, Command, Interpreter 또는 Secret 전달 방식을 주입할 수 없습니다.
 */
@ConfigurationProperties("cpf.batch.worker.operations")
public class WorkerOperationalProperties {
    private Map<String, ShellDefinition> scripts = new LinkedHashMap<>();
    private Map<String, PathAlias> pathAliases = new LinkedHashMap<>();

    public Map<String, ShellDefinition> getScripts() { return scripts; }
    public void setScripts(Map<String, ShellDefinition> scripts) { this.scripts = scripts == null ? new LinkedHashMap<>() : scripts; }
    public Map<String, PathAlias> getPathAliases() { return pathAliases; }
    public void setPathAliases(Map<String, PathAlias> pathAliases) { this.pathAliases = pathAliases == null ? new LinkedHashMap<>() : pathAliases; }

    public static class ShellDefinition {
        private String scriptId;
        private String version = "1";
        private String executable;
        private String interpreter;
        private String interpreterVersion;
        private List<String> fixedArguments = new ArrayList<>();
        private List<String> allowedParameters = new ArrayList<>();
        private List<String> sensitiveParameters = new ArrayList<>();
        private List<String> allowedEnvironmentVariables = new ArrayList<>();
        private String parameterDeliveryMode = "PARAMETER_FILE";
        private String workingDirectoryAlias;
        private String sha256;
        private String signature;
        private String runAsIdentity;
        private int timeoutSeconds = 600;
        private int gracefulShutdownSeconds = 10;
        private long maxOutputBytes = 1_048_576;
        private int maxOutputLinesPerSecond = 2_000;
        private boolean terminateProcessTree = true;
        private Set<Integer> successExitCodes = new LinkedHashSet<>(Set.of(0));
        private Set<Integer> retryableExitCodes = new LinkedHashSet<>();

        public String getScriptId() { return scriptId; }
        public void setScriptId(String scriptId) { this.scriptId = scriptId; }
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        public String getExecutable() { return executable; }
        public void setExecutable(String executable) { this.executable = executable; }
        public String getInterpreter() { return interpreter; }
        public void setInterpreter(String interpreter) { this.interpreter = interpreter; }
        public String getInterpreterVersion() { return interpreterVersion; }
        public void setInterpreterVersion(String interpreterVersion) { this.interpreterVersion = interpreterVersion; }
        public List<String> getFixedArguments() { return fixedArguments; }
        public void setFixedArguments(List<String> fixedArguments) { this.fixedArguments = safeList(fixedArguments); }
        public List<String> getAllowedParameters() { return allowedParameters; }
        public void setAllowedParameters(List<String> allowedParameters) { this.allowedParameters = safeList(allowedParameters); }
        public List<String> getSensitiveParameters() { return sensitiveParameters; }
        public void setSensitiveParameters(List<String> sensitiveParameters) { this.sensitiveParameters = safeList(sensitiveParameters); }
        public List<String> getAllowedEnvironmentVariables() { return allowedEnvironmentVariables; }
        public void setAllowedEnvironmentVariables(List<String> allowedEnvironmentVariables) { this.allowedEnvironmentVariables = safeList(allowedEnvironmentVariables); }
        public String getParameterDeliveryMode() { return parameterDeliveryMode; }
        public void setParameterDeliveryMode(String parameterDeliveryMode) { this.parameterDeliveryMode = parameterDeliveryMode; }
        public String getWorkingDirectoryAlias() { return workingDirectoryAlias; }
        public void setWorkingDirectoryAlias(String workingDirectoryAlias) { this.workingDirectoryAlias = workingDirectoryAlias; }
        public String getSha256() { return sha256; }
        public void setSha256(String sha256) { this.sha256 = sha256; }
        public String getSignature() { return signature; }
        public void setSignature(String signature) { this.signature = signature; }
        public String getRunAsIdentity() { return runAsIdentity; }
        public void setRunAsIdentity(String runAsIdentity) { this.runAsIdentity = runAsIdentity; }
        public int getTimeoutSeconds() { return timeoutSeconds; }
        public void setTimeoutSeconds(int timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
        public int getGracefulShutdownSeconds() { return gracefulShutdownSeconds; }
        public void setGracefulShutdownSeconds(int gracefulShutdownSeconds) { this.gracefulShutdownSeconds = gracefulShutdownSeconds; }
        public long getMaxOutputBytes() { return maxOutputBytes; }
        public void setMaxOutputBytes(long maxOutputBytes) { this.maxOutputBytes = maxOutputBytes; }
        public int getMaxOutputLinesPerSecond() { return maxOutputLinesPerSecond; }
        public void setMaxOutputLinesPerSecond(int maxOutputLinesPerSecond) { this.maxOutputLinesPerSecond = maxOutputLinesPerSecond; }
        public boolean isTerminateProcessTree() { return terminateProcessTree; }
        public void setTerminateProcessTree(boolean terminateProcessTree) { this.terminateProcessTree = terminateProcessTree; }
        public Set<Integer> getSuccessExitCodes() { return successExitCodes; }
        public void setSuccessExitCodes(Set<Integer> successExitCodes) { this.successExitCodes = safeSet(successExitCodes, Set.of(0)); }
        public Set<Integer> getRetryableExitCodes() { return retryableExitCodes; }
        public void setRetryableExitCodes(Set<Integer> retryableExitCodes) { this.retryableExitCodes = safeSet(retryableExitCodes, Set.of()); }

        private static List<String> safeList(List<String> values) {
            return values == null ? new ArrayList<>() : new ArrayList<>(values);
        }

        private static Set<Integer> safeSet(Set<Integer> values, Set<Integer> defaults) {
            return values == null || values.isEmpty() ? new LinkedHashSet<>(defaults) : new LinkedHashSet<>(values);
        }
    }

    public static class PathAlias {
        private String root;
        private String provider = "LOCAL";
        private String endpointCode;
        private String protocol;
        private String host;
        private int port;
        private String remoteBasePath;
        private String credentialScope = "default";
        private String credentialId;
        private String credentialVersion = "latest";
        private int timeoutSeconds = 30;
        private Map<String, String> attributes = new LinkedHashMap<>();
        private boolean sharedDurable;
        private boolean symlinkAllowed;
        private boolean malwareScanRequired;
        private long maxFileSizeBytes = 10L * 1024 * 1024 * 1024;
        private List<String> allowedExtensions = new ArrayList<>();
        private int stableWindowSeconds = 5;
        private String completionMarkerSuffix;

        public String getRoot() { return root; }
        public void setRoot(String root) { this.root = root; }
        public String getProvider() { return provider; }
        public void setProvider(String provider) { this.provider = provider; }
        public String getEndpointCode() { return endpointCode; }
        public void setEndpointCode(String endpointCode) { this.endpointCode = endpointCode; }
        public String getProtocol() { return protocol; }
        public void setProtocol(String protocol) { this.protocol = protocol; }
        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        public String getRemoteBasePath() { return remoteBasePath; }
        public void setRemoteBasePath(String remoteBasePath) { this.remoteBasePath = remoteBasePath; }
        public String getCredentialScope() { return credentialScope; }
        public void setCredentialScope(String credentialScope) { this.credentialScope = credentialScope; }
        public String getCredentialId() { return credentialId; }
        public void setCredentialId(String credentialId) { this.credentialId = credentialId; }
        public String getCredentialVersion() { return credentialVersion; }
        public void setCredentialVersion(String credentialVersion) { this.credentialVersion = credentialVersion; }
        public int getTimeoutSeconds() { return timeoutSeconds; }
        public void setTimeoutSeconds(int timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
        public Map<String, String> getAttributes() { return attributes; }
        public void setAttributes(Map<String, String> attributes) {
            this.attributes = attributes == null ? new LinkedHashMap<>() : new LinkedHashMap<>(attributes);
        }
        public boolean isSharedDurable() { return sharedDurable; }
        public void setSharedDurable(boolean sharedDurable) { this.sharedDurable = sharedDurable; }
        public boolean isSymlinkAllowed() { return symlinkAllowed; }
        public void setSymlinkAllowed(boolean symlinkAllowed) { this.symlinkAllowed = symlinkAllowed; }
        public boolean isMalwareScanRequired() { return malwareScanRequired; }
        public void setMalwareScanRequired(boolean malwareScanRequired) { this.malwareScanRequired = malwareScanRequired; }
        public long getMaxFileSizeBytes() { return maxFileSizeBytes; }
        public void setMaxFileSizeBytes(long maxFileSizeBytes) { this.maxFileSizeBytes = maxFileSizeBytes; }
        public List<String> getAllowedExtensions() { return allowedExtensions; }
        public void setAllowedExtensions(List<String> allowedExtensions) { this.allowedExtensions = allowedExtensions == null ? new ArrayList<>() : new ArrayList<>(allowedExtensions); }
        public int getStableWindowSeconds() { return stableWindowSeconds; }
        public void setStableWindowSeconds(int stableWindowSeconds) { this.stableWindowSeconds = stableWindowSeconds; }
        public String getCompletionMarkerSuffix() { return completionMarkerSuffix; }
        public void setCompletionMarkerSuffix(String completionMarkerSuffix) { this.completionMarkerSuffix = completionMarkerSuffix; }
    }
}

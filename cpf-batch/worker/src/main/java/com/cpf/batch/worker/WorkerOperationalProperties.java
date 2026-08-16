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
    private Map<String, String> trustedSigningKeys = new LinkedHashMap<>();
    private OutboundHttp outboundHttp = new OutboundHttp();

    public Map<String, ShellDefinition> getScripts() { return scripts; }
    public void setScripts(Map<String, ShellDefinition> scripts) { this.scripts = scripts == null ? new LinkedHashMap<>() : scripts; }
    public Map<String, PathAlias> getPathAliases() { return pathAliases; }
    public Map<String, String> getTrustedSigningKeys() { return trustedSigningKeys; }
    public void setTrustedSigningKeys(Map<String, String> trustedSigningKeys) { this.trustedSigningKeys = trustedSigningKeys == null ? new LinkedHashMap<>() : new LinkedHashMap<>(trustedSigningKeys); }
    public void setPathAliases(Map<String, PathAlias> pathAliases) { this.pathAliases = pathAliases == null ? new LinkedHashMap<>() : pathAliases; }
    public OutboundHttp getOutboundHttp() { return outboundHttp; }
    public void setOutboundHttp(OutboundHttp value) { this.outboundHttp = value == null ? new OutboundHttp() : value; }

    /** PROTOCOL_ADAPTER 외부 HTTP 호출의 제품 보안 계약입니다. 기본값은 비활성입니다. */
    public static class OutboundHttp {
        private boolean enabled;
        private boolean allowHttpLoopback;
        private boolean allowPrivateAddresses;
        private boolean requireDnsPin = true;
        private long maxRequestBytes = 1_048_576L;
        private long maxResponseBytes = 10_485_760L;
        private int connectTimeoutSeconds = 10;
        private int readTimeoutSeconds = 60;
        private int maxAttempts = 3;
        private long retryBackoffMillis = 100L;
        private int maxResponseHeaderCount = 100;
        private int maxResponseHeaderBytes = 65_536;
        private List<String> allowedHosts = new ArrayList<>();
        private Set<Integer> allowedPorts = new LinkedHashSet<>(Set.of(443));
        private Map<String, List<String>> hostPins = new LinkedHashMap<>();
        private List<String> allowedCidrs = new ArrayList<>();
        private Set<String> allowedMethods = new LinkedHashSet<>(Set.of("GET", "POST", "PUT", "PATCH", "DELETE"));
        private Set<String> allowedRequestHeaders = new LinkedHashSet<>(Set.of(
                "content-type", "accept", "x-cpf-idempotency-key", "x-cpf-reconcile-key"));
        private Set<String> allowedResponseContentTypes = new LinkedHashSet<>(Set.of(
                "application/json", "application/problem+json", "text/plain"));

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public boolean isAllowHttpLoopback() { return allowHttpLoopback; }
        public void setAllowHttpLoopback(boolean value) { this.allowHttpLoopback = value; }
        public boolean isAllowPrivateAddresses() { return allowPrivateAddresses; }
        public void setAllowPrivateAddresses(boolean value) { this.allowPrivateAddresses = value; }
        public boolean isRequireDnsPin() { return requireDnsPin; }
        public void setRequireDnsPin(boolean value) { this.requireDnsPin = value; }
        public long getMaxRequestBytes() { return maxRequestBytes; }
        public void setMaxRequestBytes(long value) { this.maxRequestBytes = value; }
        public long getMaxResponseBytes() { return maxResponseBytes; }
        public void setMaxResponseBytes(long value) { this.maxResponseBytes = value; }
        public int getConnectTimeoutSeconds() { return connectTimeoutSeconds; }
        public void setConnectTimeoutSeconds(int value) { this.connectTimeoutSeconds = value; }
        public int getReadTimeoutSeconds() { return readTimeoutSeconds; }
        public void setReadTimeoutSeconds(int value) { this.readTimeoutSeconds = value; }
        public int getMaxAttempts() { return maxAttempts; }
        public void setMaxAttempts(int value) { this.maxAttempts = value; }
        public long getRetryBackoffMillis() { return retryBackoffMillis; }
        public void setRetryBackoffMillis(long value) { this.retryBackoffMillis = value; }
        public int getMaxResponseHeaderCount() { return maxResponseHeaderCount; }
        public void setMaxResponseHeaderCount(int value) { this.maxResponseHeaderCount = value; }
        public int getMaxResponseHeaderBytes() { return maxResponseHeaderBytes; }
        public void setMaxResponseHeaderBytes(int value) { this.maxResponseHeaderBytes = value; }
        public List<String> getAllowedHosts() { return allowedHosts; }
        public void setAllowedHosts(List<String> value) { this.allowedHosts = value == null ? new ArrayList<>() : new ArrayList<>(value); }
        public Set<Integer> getAllowedPorts() { return allowedPorts; }
        public void setAllowedPorts(Set<Integer> value) { this.allowedPorts = value == null ? new LinkedHashSet<>() : new LinkedHashSet<>(value); }
        public Map<String, List<String>> getHostPins() { return hostPins; }
        public void setHostPins(Map<String, List<String>> value) {
            this.hostPins = value == null ? new LinkedHashMap<>() : new LinkedHashMap<>(value);
        }
        public List<String> getAllowedCidrs() { return allowedCidrs; }
        public void setAllowedCidrs(List<String> value) {
            this.allowedCidrs = value == null ? new ArrayList<>() : new ArrayList<>(value);
        }
        public Set<String> getAllowedMethods() { return allowedMethods; }
        public void setAllowedMethods(Set<String> value) {
            this.allowedMethods = normalizeUpper(value);
        }
        public Set<String> getAllowedRequestHeaders() { return allowedRequestHeaders; }
        public void setAllowedRequestHeaders(Set<String> value) {
            this.allowedRequestHeaders = normalizeLower(value);
        }
        public Set<String> getAllowedResponseContentTypes() { return allowedResponseContentTypes; }
        public void setAllowedResponseContentTypes(Set<String> value) {
            this.allowedResponseContentTypes = normalizeLower(value);
        }

        /** 제품 프로필이 시작될 때 잘못된 외부 호출 정책을 fail-closed 합니다. */
        public void validate() {
            if (maxRequestBytes < 0 || maxResponseBytes < 0) throw new IllegalStateException("outbound HTTP size cap is invalid");
            if (connectTimeoutSeconds < 1 || readTimeoutSeconds < 1) throw new IllegalStateException("outbound HTTP timeout is invalid");
            if (maxAttempts < 1 || maxAttempts > 10) throw new IllegalStateException("outbound HTTP maxAttempts is invalid");
            if (retryBackoffMillis < 0 || retryBackoffMillis > 60_000) throw new IllegalStateException("outbound HTTP retry backoff is invalid");
            if (maxResponseHeaderCount < 1 || maxResponseHeaderCount > 500 || maxResponseHeaderBytes < 1024) {
                throw new IllegalStateException("outbound HTTP response header budget is invalid");
            }
            if (enabled && (allowedHosts.isEmpty() || allowedPorts.isEmpty() || allowedMethods.isEmpty())) {
                throw new IllegalStateException("enabled outbound HTTP requires host/port/method allowlists");
            }
            if (enabled && requireDnsPin && hostPins.isEmpty()) {
                throw new IllegalStateException("enabled outbound HTTP requires DNS pins");
            }
            allowedPorts.forEach(port -> { if (port == null || port < 1 || port > 65535) throw new IllegalStateException("outbound HTTP port is invalid"); });
        }

        private static Set<String> normalizeLower(Set<String> values) {
            LinkedHashSet<String> normalized = new LinkedHashSet<>();
            if (values != null) values.stream().filter(Objects::nonNull).map(String::trim).filter(v -> !v.isEmpty())
                    .map(v -> v.toLowerCase(Locale.ROOT)).forEach(normalized::add);
            return normalized;
        }

        private static Set<String> normalizeUpper(Set<String> values) {
            LinkedHashSet<String> normalized = new LinkedHashSet<>();
            if (values != null) values.stream().filter(Objects::nonNull).map(String::trim).filter(v -> !v.isEmpty())
                    .map(v -> v.toUpperCase(Locale.ROOT)).forEach(normalized::add);
            return normalized;
        }
    }

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
        private String verificationMode = "SIGNATURE";
        private String signature;
        private String signatureKeyId;
        private String signatureAlgorithm = "SHA256withRSA";
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
        public String getVerificationMode() { return verificationMode; }
        public void setVerificationMode(String verificationMode) { this.verificationMode = verificationMode == null ? "SIGNATURE" : verificationMode.trim().toUpperCase(Locale.ROOT); }
        public String getSignature() { return signature; }
        public void setSignature(String signature) { this.signature = signature; }
        public String getSignatureKeyId() { return signatureKeyId; }
        public void setSignatureKeyId(String signatureKeyId) { this.signatureKeyId = signatureKeyId; }
        public String getSignatureAlgorithm() { return signatureAlgorithm; }
        public void setSignatureAlgorithm(String signatureAlgorithm) { this.signatureAlgorithm = signatureAlgorithm; }
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

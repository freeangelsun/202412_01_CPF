package com.cpf.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 설치자가 허용하는 Gateway 안전 상한입니다.
 *
 * <p>ADM Runtime Policy는 이 상한을 확대할 수 없고 더 보수적으로만 축소할 수 있습니다.</p>
 */
@Component
@ConfigurationProperties("cpf.gateway")
public class CpfGatewaySafetyProperties {
    private Duration routeRefresh = Duration.ofSeconds(30);
    private Duration policyRefresh = Duration.ofSeconds(15);
    private Duration healthProbeInterval = Duration.ofSeconds(30);
    private Duration staleAfter = Duration.ofSeconds(90);
    private Duration connectTimeoutCap = Duration.ofSeconds(10);
    private Duration responseTimeoutCap = Duration.ofSeconds(60);
    private Duration overallTimeoutCap = Duration.ofSeconds(90);
    private int retryCountCap = 2;
    private long requestBodyBytesCap = 10L * 1024 * 1024;
    private long responseBodyBytesCap = 10L * 1024 * 1024;
    private int headerCountCap = 100;
    private int headerBytesCap = 64 * 1024;
    private boolean rawBodyCaptureAllowed;
    private long logSpoolBytesCap = 2L * 1024 * 1024 * 1024;
    private String logSpoolDirectory = "./data/gateway-log-spool";
    private String bootstrapMode = "FAIL_CLOSED";
    private String environmentCode = "local";
    private String instanceId = "gateway";
    private String zoneCode = "";
    private boolean allowPrivateTargets = true;
    private boolean allowPublicTargets;
    private boolean allowDnsTargets = true;
    private boolean requireTlsTargets = true;
    private Set<Integer> allowedTargetPorts = new LinkedHashSet<>(Set.of(443, 8443, 9443));
    private Set<String> allowedTargetCidrs = new LinkedHashSet<>();
    private int rateLimitCounterEntriesCap = 100_000;
    private String rateLimitCounterMode = "LOCAL";
    private boolean rateLimitFailClosedOnCounterFailure = true;
    private boolean requireDistributedRateLimitCounter;
    private int dataPlanePort;
    private boolean requireTlsIngress;
    private Set<String> allowedIngressProtocols = new LinkedHashSet<>(Set.of("HTTP/1.1", "HTTP/2.0"));
    private boolean maintenanceMode;
    private Duration maintenanceRetryAfter = Duration.ofSeconds(60);
    private Duration maintenanceRetryAfterCap = Duration.ofMinutes(15);
    private Set<String> trustedContextHeaders = new LinkedHashSet<>(Set.of(
            "accept", "content-type", "idempotency-key", "traceparent", "tracestate",
            "x-api-version", "x-channel-id", "x-client-id", "x-operation-reason", "x-transaction-id",
            "x-original-channel-code", "x-channel-code", "x-request-type", "x-cpf-idempotency-key"));

    public void validate() {
        positive(routeRefresh,"routeRefresh"); positive(policyRefresh,"policyRefresh");
        positive(healthProbeInterval,"healthProbeInterval"); positive(staleAfter,"staleAfter");
        positive(connectTimeoutCap,"connectTimeoutCap"); positive(responseTimeoutCap,"responseTimeoutCap"); positive(overallTimeoutCap,"overallTimeoutCap");
        if (overallTimeoutCap.compareTo(connectTimeoutCap) < 0 || overallTimeoutCap.compareTo(responseTimeoutCap) < 0) throw new IllegalStateException("overallTimeoutCap must cover connect/response caps");
        if (retryCountCap < 0 || retryCountCap > 10) throw new IllegalStateException("retryCountCap out of range");
        if (requestBodyBytesCap < 0 || responseBodyBytesCap < 0 || headerCountCap < 1 || headerBytesCap < 1024 || logSpoolBytesCap < 1024) throw new IllegalStateException("Gateway size caps are invalid");
        if (logSpoolDirectory == null || logSpoolDirectory.isBlank()) throw new IllegalStateException("Gateway logSpoolDirectory is required");
        if (!"FAIL_CLOSED".equalsIgnoreCase(bootstrapMode) && !"LAST_KNOWN_GOOD".equalsIgnoreCase(bootstrapMode)) throw new IllegalStateException("Unsupported bootstrapMode");
        if (environmentCode == null || environmentCode.isBlank()) throw new IllegalStateException("Gateway environmentCode is required");
        if (instanceId == null || instanceId.isBlank()) throw new IllegalStateException("Gateway instanceId is required");
        if (zoneCode == null) zoneCode = "";
        if (!allowPrivateTargets && !allowPublicTargets) throw new IllegalStateException("Gateway private/public target가 모두 금지됐습니다.");
        if (allowedTargetPorts == null || allowedTargetPorts.isEmpty() || allowedTargetPorts.stream().anyMatch(v -> v == null || v < 1 || v > 65_535)) throw new
                IllegalStateException("Gateway allowedTargetPorts is invalid");
        if (allowedTargetCidrs == null) allowedTargetCidrs = new LinkedHashSet<>();
        if (trustedContextHeaders == null || trustedContextHeaders.isEmpty()) throw new IllegalStateException("trustedContextHeaders must not be empty");
        if (dataPlanePort < 0 || dataPlanePort > 65_535) throw new IllegalStateException("dataPlanePort out of range");
        positive(maintenanceRetryAfter, "maintenanceRetryAfter");
        positive(maintenanceRetryAfterCap, "maintenanceRetryAfterCap");
        if (maintenanceRetryAfter.compareTo(maintenanceRetryAfterCap) > 0) throw new IllegalStateException("maintenanceRetryAfter exceeds cap");
        if (allowedIngressProtocols == null || allowedIngressProtocols.isEmpty()) throw new IllegalStateException("allowedIngressProtocols must not be empty");
        LinkedHashSet<String> normalizedProtocols = new LinkedHashSet<>();
        for (String protocol : allowedIngressProtocols) {
            if (protocol == null || protocol.isBlank()) throw new IllegalStateException("allowedIngressProtocols contains blank value");
            String normalized = protocol.trim().toUpperCase(java.util.Locale.ROOT);
            if ("HTTP/2".equals(normalized)) normalized = "HTTP/2.0";
            if (!"HTTP/1.1".equals(normalized) && !"HTTP/2.0".equals(normalized)) {
                throw new IllegalStateException("Unsupported ingress protocol: " + protocol);
            }
            normalizedProtocols.add(normalized);
        }
        allowedIngressProtocols = normalizedProtocols;
        if (rateLimitCounterEntriesCap < 100 || rateLimitCounterEntriesCap > 5_000_000) throw new IllegalStateException("rateLimitCounterEntriesCap out of range");
        String counterMode = rateLimitCounterMode == null ? "" : rateLimitCounterMode.trim().toUpperCase(java.util.Locale.ROOT);
        if (!"LOCAL".equals(counterMode) && !"JDBC".equals(counterMode)) throw new IllegalStateException("Unsupported rateLimitCounterMode");
        rateLimitCounterMode = counterMode;
        String normalizedEnvironment = environmentCode.trim().toLowerCase(java.util.Locale.ROOT);
        if (("prod".equals(normalizedEnvironment) || "production".equals(normalizedEnvironment))
                && (!requireDistributedRateLimitCounter || !"JDBC".equals(rateLimitCounterMode))) {
            throw new IllegalStateException("Production Gateway requires distributed rate-limit counter");
        }
        if (("prod".equals(normalizedEnvironment) || "production".equals(normalizedEnvironment))
                && (dataPlanePort < 1 || !requireTlsIngress)) {
            throw new IllegalStateException("Production Gateway requires explicit TLS Data Plane listener");
        }
    }

    private static void positive(Duration value,String name){if(value==null||value.isZero()||value.isNegative())throw new IllegalStateException(name+" must be positive");}
    public Duration getRouteRefresh(){return routeRefresh;} public void setRouteRefresh(Duration v){routeRefresh=v;}
    public Duration getPolicyRefresh(){return policyRefresh;} public void setPolicyRefresh(Duration v){policyRefresh=v;}
    public Duration getHealthProbeInterval(){return healthProbeInterval;} public void setHealthProbeInterval(Duration v){healthProbeInterval=v;}
    public Duration getStaleAfter(){return staleAfter;} public void setStaleAfter(Duration v){staleAfter=v;}
    public Duration getConnectTimeoutCap(){return connectTimeoutCap;} public void setConnectTimeoutCap(Duration v){connectTimeoutCap=v;}
    public Duration getResponseTimeoutCap(){return responseTimeoutCap;} public void setResponseTimeoutCap(Duration v){responseTimeoutCap=v;}
    public Duration getOverallTimeoutCap(){return overallTimeoutCap;} public void setOverallTimeoutCap(Duration v){overallTimeoutCap=v;}
    public int getRetryCountCap(){return retryCountCap;} public void setRetryCountCap(int v){retryCountCap=v;}
    public long getRequestBodyBytesCap(){return requestBodyBytesCap;} public void setRequestBodyBytesCap(long v){requestBodyBytesCap=v;}
    public long getResponseBodyBytesCap(){return responseBodyBytesCap;} public void setResponseBodyBytesCap(long v){responseBodyBytesCap=v;}
    public int getHeaderCountCap(){return headerCountCap;} public void setHeaderCountCap(int v){headerCountCap=v;}
    public int getHeaderBytesCap(){return headerBytesCap;} public void setHeaderBytesCap(int v){headerBytesCap=v;}
    public boolean isRawBodyCaptureAllowed(){return rawBodyCaptureAllowed;} public void setRawBodyCaptureAllowed(boolean v){rawBodyCaptureAllowed=v;}
    public long getLogSpoolBytesCap(){return logSpoolBytesCap;} public void setLogSpoolBytesCap(long v){logSpoolBytesCap=v;}
    public String getLogSpoolDirectory(){return logSpoolDirectory;} public void setLogSpoolDirectory(String v){logSpoolDirectory=v;}
    public String getBootstrapMode(){return bootstrapMode;} public void setBootstrapMode(String v){bootstrapMode=v;}
    public String getEnvironmentCode(){return environmentCode;} public void setEnvironmentCode(String v){environmentCode=v;}
    public String getInstanceId(){return instanceId;} public void setInstanceId(String v){instanceId=v;}
    public String getZoneCode(){return zoneCode;} public void setZoneCode(String v){zoneCode=v==null?"":v.trim();}
    public boolean isAllowPrivateTargets(){return allowPrivateTargets;} public void setAllowPrivateTargets(boolean v){allowPrivateTargets=v;}
    public boolean isAllowPublicTargets(){return allowPublicTargets;} public void setAllowPublicTargets(boolean v){allowPublicTargets=v;}
    public boolean isAllowDnsTargets(){return allowDnsTargets;} public void setAllowDnsTargets(boolean v){allowDnsTargets=v;}
    public boolean isRequireTlsTargets(){return requireTlsTargets;} public void setRequireTlsTargets(boolean v){requireTlsTargets=v;}
    public Set<Integer> getAllowedTargetPorts(){return Set.copyOf(allowedTargetPorts);} public void setAllowedTargetPorts(Set<Integer> v){allowedTargetPorts=v==null?new LinkedHashSet<>():new LinkedHashSet<>(v);}
    public Set<String> getAllowedTargetCidrs(){return Set.copyOf(allowedTargetCidrs);} public void setAllowedTargetCidrs(Set<String> v){allowedTargetCidrs=v==null?new LinkedHashSet<>():new LinkedHashSet<>(v);}
    public int getRateLimitCounterEntriesCap(){return rateLimitCounterEntriesCap;} public void setRateLimitCounterEntriesCap(int v){rateLimitCounterEntriesCap=v;}
    public String getRateLimitCounterMode(){return rateLimitCounterMode;} public void setRateLimitCounterMode(String v){rateLimitCounterMode=v;}
    public boolean isRateLimitFailClosedOnCounterFailure(){return rateLimitFailClosedOnCounterFailure;} public void setRateLimitFailClosedOnCounterFailure(boolean v){rateLimitFailClosedOnCounterFailure=v;}
    public boolean isRequireDistributedRateLimitCounter(){return requireDistributedRateLimitCounter;} public void setRequireDistributedRateLimitCounter(boolean v){requireDistributedRateLimitCounter=v;}
    public int getDataPlanePort(){return dataPlanePort;} public void setDataPlanePort(int v){dataPlanePort=v;}
    public boolean isRequireTlsIngress(){return requireTlsIngress;} public void setRequireTlsIngress(boolean v){requireTlsIngress=v;}
    public Set<String> getAllowedIngressProtocols(){return Set.copyOf(allowedIngressProtocols);}
    public void setAllowedIngressProtocols(Set<String> values){allowedIngressProtocols=values==null?new LinkedHashSet<>():new LinkedHashSet<>(values);}
    public boolean isMaintenanceMode(){return maintenanceMode;} public void setMaintenanceMode(boolean v){maintenanceMode=v;}
    public Duration getMaintenanceRetryAfter(){return maintenanceRetryAfter;} public void setMaintenanceRetryAfter(Duration v){maintenanceRetryAfter=v;}
    public Duration getMaintenanceRetryAfterCap(){return maintenanceRetryAfterCap;} public void setMaintenanceRetryAfterCap(Duration v){maintenanceRetryAfterCap=v;}
    public Set<String> getTrustedContextHeaders(){return Set.copyOf(trustedContextHeaders);}
    public void setTrustedContextHeaders(Set<String> values){trustedContextHeaders=new LinkedHashSet<>();
            if(values!=null)values.stream().filter(v->v!=null&&!v.isBlank()).map(v->v.toLowerCase(java.util.Locale.ROOT)).forEach(trustedContextHeaders::add);}
}

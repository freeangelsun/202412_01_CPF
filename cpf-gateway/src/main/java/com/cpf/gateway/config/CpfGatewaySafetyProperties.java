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
    private boolean allowPublicTargets;
    private Set<String> trustedContextHeaders = new LinkedHashSet<>(Set.of(
            "accept", "content-type", "idempotency-key", "traceparent", "tracestate",
            "x-api-version", "x-channel-id", "x-client-id", "x-operation-reason", "x-transaction-id"));

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
        if (trustedContextHeaders == null || trustedContextHeaders.isEmpty()) throw new IllegalStateException("trustedContextHeaders must not be empty");
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
    public boolean isAllowPublicTargets(){return allowPublicTargets;} public void setAllowPublicTargets(boolean v){allowPublicTargets=v;}
    public Set<String> getTrustedContextHeaders(){return Set.copyOf(trustedContextHeaders);}
    public void setTrustedContextHeaders(Set<String> values){trustedContextHeaders=new LinkedHashSet<>();if(values!=null)values.stream().filter(v->v!=null&&!v.isBlank()).map(v->v.toLowerCase(java.util.Locale.ROOT)).forEach(trustedContextHeaders::add);}
}

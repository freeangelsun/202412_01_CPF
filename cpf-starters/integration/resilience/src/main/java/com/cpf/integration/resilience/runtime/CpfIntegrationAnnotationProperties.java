package com.cpf.integration.resilience.runtime;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Developer Annotation에서 생성되는 기본 Resilience policy의 안전 한계입니다. */
@ConfigurationProperties("cpf.integration.annotation")
public class CpfIntegrationAnnotationProperties {
    private boolean enabled = true;
    private long defaultTimeoutMillis = 3000;
    private int circuitFailureThreshold = 5;
    private long circuitOpenMillis = 30000;
    private int bulkheadMaxConcurrent = 100;
    /** 설정값의 의미와 기본 동작을 명확히 하여 운영 설정 영향을 추적한다. */
    private int rateLimitPermits = 1000;
    private long rateLimitWindowMillis = 1000;
    public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
    public long getDefaultTimeoutMillis(){return defaultTimeoutMillis;} public void setDefaultTimeoutMillis(long v){defaultTimeoutMillis=positive(v,"defaultTimeoutMillis");}
    public int getCircuitFailureThreshold(){return circuitFailureThreshold;} public void setCircuitFailureThreshold(int v){circuitFailureThreshold=positive(v,"circuitFailureThreshold");}
    public long getCircuitOpenMillis(){return circuitOpenMillis;} public void setCircuitOpenMillis(long v){circuitOpenMillis=positive(v,"circuitOpenMillis");}
    public int getBulkheadMaxConcurrent(){return bulkheadMaxConcurrent;} public void setBulkheadMaxConcurrent(int v){bulkheadMaxConcurrent=positive(v,"bulkheadMaxConcurrent");}
    public int getRateLimitPermits(){return rateLimitPermits;} public void setRateLimitPermits(int v){rateLimitPermits=positive(v,"rateLimitPermits");}
    public long getRateLimitWindowMillis(){return rateLimitWindowMillis;} public void setRateLimitWindowMillis(long v){rateLimitWindowMillis=positive(v,"rateLimitWindowMillis");}
    private static int positive(int v,String n){if(v<1)throw new IllegalArgumentException(n+" must be positive");return v;}
    private static long positive(long v,String n){if(v<1)throw new IllegalArgumentException(n+" must be positive");return v;}
}

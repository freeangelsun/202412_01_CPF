package com.cpf.integration.ai;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Provider-neutral AI runtime policy. No credential is stored in this properties object. */
@ConfigurationProperties("cpf.integration.ai")
/** CpfAiProperties 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public class CpfAiProperties {
    private boolean enabled;
    private List<String> providerOrder = new ArrayList<>();
    private int maxAttempts = 3;
    private int retryAttemptsPerProvider = 2;
    private Duration timeout = Duration.ofSeconds(10);
    private Duration retryBackoff = Duration.ofMillis(100);
    /** 설정값의 의미와 기본 동작을 명확히 하여 운영 설정 영향을 추적한다. */
    private int circuitFailureThreshold = 3;
    private Duration circuitOpenDuration = Duration.ofSeconds(30);
    private int requestsPerMinute = 60;
    private long estimatedTokensPerMinute = 120000;
    private int maxPayloadChars = 200000;
    private long maxRequestedOutputTokens = 32768;
    public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
    public List<String> getProviderOrder(){return providerOrder;} public void setProviderOrder(List<String> v){providerOrder=v==null?new ArrayList<>():new ArrayList<>(v);}
    public int getMaxAttempts(){return maxAttempts;} public void setMaxAttempts(int v){if(v<1||v>10)throw new IllegalArgumentException("max-attempts must be 1..10");maxAttempts=v;}
    public int getRetryAttemptsPerProvider(){return retryAttemptsPerProvider;} public void setRetryAttemptsPerProvider(int v){if(v<1||v>5)throw new IllegalArgumentException("retry-attempts-per-provider must be 1..5");retryAttemptsPerProvider=v;}
    public Duration getTimeout(){return timeout;} public void setTimeout(Duration v){if(v==null||v.isZero()||v.isNegative())throw new IllegalArgumentException("timeout positive");timeout=v;}
    public Duration getRetryBackoff(){return retryBackoff;} public void setRetryBackoff(Duration v){if(v==null||v.isNegative())throw new IllegalArgumentException("retry-backoff must not be negative");retryBackoff=v;}
    public int getCircuitFailureThreshold(){return circuitFailureThreshold;} public void setCircuitFailureThreshold(int v){if(v<1||v>100)throw new IllegalArgumentException("circuit-failure-threshold must be 1..100");circuitFailureThreshold=v;}
    public Duration getCircuitOpenDuration(){return circuitOpenDuration;} public void setCircuitOpenDuration(Duration v){if(v==null||v.isZero()||v.isNegative())throw new IllegalArgumentException("circuit-open-duration positive");circuitOpenDuration=v;}
    public int getRequestsPerMinute(){return requestsPerMinute;} public void setRequestsPerMinute(int v){if(v<1)throw new IllegalArgumentException("requests-per-minute must be >= 1");requestsPerMinute=v;}
    public long getEstimatedTokensPerMinute(){return estimatedTokensPerMinute;} public void setEstimatedTokensPerMinute(long v){if(v<1)throw new IllegalArgumentException("estimated-tokens-per-minute must be >= 1");estimatedTokensPerMinute=v;}
    public int getMaxPayloadChars(){return maxPayloadChars;} public void setMaxPayloadChars(int v){if(v<1)throw new IllegalArgumentException("max-payload-chars must be >= 1");maxPayloadChars=v;}
    public long getMaxRequestedOutputTokens(){return maxRequestedOutputTokens;} public void setMaxRequestedOutputTokens(long v){if(v<1)throw new IllegalArgumentException("max-requested-output-tokens must be >= 1");maxRequestedOutputTokens=v;}
}

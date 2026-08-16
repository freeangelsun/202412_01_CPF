package com.cpf.integration.http.internal.servicecall;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * CPF 서비스 호출 엔진 기본 설정입니다.
 *
 * <p>업무 모듈은 이 설정을 직접 읽기보다 {@link CpfServiceCallEngine}과 {@code CpfWebClient}를 통해
 * timeout, retry, circuit 정책을 일관되게 적용합니다.</p>
 */
@ConfigurationProperties(prefix = "cpf.service-call")
public class CpfServiceCallProperties {
    /** 서비스 호출 엔진 사용 여부입니다. 기본값 true이며 운영에서는 레지스트리/DB 준비 상태와 함께 검증합니다. */
    private boolean enabled = true;
    /** 기본 호출 Timeout(ms)입니다. 개별 호출의 bounded override가 없을 때 사용합니다. */
    private int defaultTimeoutMillis = 3000;
    /** 기본 재시도 횟수입니다. 멱등/재시도 가능 조건을 만족한 기술 실패에만 적용합니다. */
    private int defaultRetryCount = 1;
    /** 운영 호출이력 조회 상한입니다. 과도한 조회 부하를 막기 위한 안전 한계입니다. */
    private int maxHistoryQueryLimit = 500;
    /** 설정값의 의미와 기본 동작을 명확히 하여 운영 설정 영향을 추적한다. */
    private int maxRetryCount = 3;
    /** 연속 기술 실패 후 Circuit을 OPEN으로 전환하는 임계값입니다. */
    private int circuitOpenFailureThreshold = 3;
    /** OPEN Circuit의 Half-open 재확인 대기시간(ms)입니다. */
    private long circuitOpenRetryAfterMillis = 30000;
    /** 첫 재시도 Backoff(ms)입니다. */
    private long retryBackoffMillis = 100;
    /** 재시도 Backoff 최대값(ms)입니다. */
    private long maxRetryBackoffMillis = 2000;
    /** Local/dev 호환 endpoint fallback 여부입니다. 운영 Profile에서는 명시적 정책으로 잠가야 합니다. */
    private boolean fallbackToConfiguredEndpoint = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getDefaultTimeoutMillis() {
        return defaultTimeoutMillis;
    }

    public void setDefaultTimeoutMillis(int defaultTimeoutMillis) {
        this.defaultTimeoutMillis = defaultTimeoutMillis;
    }

    public int getDefaultRetryCount() {
        return defaultRetryCount;
    }

    public void setDefaultRetryCount(int defaultRetryCount) {
        this.defaultRetryCount = defaultRetryCount;
    }

    public int getMaxHistoryQueryLimit() {
        return maxHistoryQueryLimit;
    }

    public void setMaxHistoryQueryLimit(int maxHistoryQueryLimit) {
        this.maxHistoryQueryLimit = maxHistoryQueryLimit;
    }

    public int getMaxRetryCount() {
        return maxRetryCount;
    }

    public void setMaxRetryCount(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public int getCircuitOpenFailureThreshold() {
        return circuitOpenFailureThreshold;
    }

    public void setCircuitOpenFailureThreshold(int circuitOpenFailureThreshold) {
        this.circuitOpenFailureThreshold = circuitOpenFailureThreshold;
    }

    public long getCircuitOpenRetryAfterMillis() {
        return circuitOpenRetryAfterMillis;
    }

    public void setCircuitOpenRetryAfterMillis(long circuitOpenRetryAfterMillis) {
        this.circuitOpenRetryAfterMillis = circuitOpenRetryAfterMillis;
    }

    public long getRetryBackoffMillis() {
        return retryBackoffMillis;
    }

    public void setRetryBackoffMillis(long retryBackoffMillis) {
        this.retryBackoffMillis = retryBackoffMillis;
    }

    public long getMaxRetryBackoffMillis() {
        return maxRetryBackoffMillis;
    }

    public void setMaxRetryBackoffMillis(long maxRetryBackoffMillis) {
        this.maxRetryBackoffMillis = maxRetryBackoffMillis;
    }

    public boolean isFallbackToConfiguredEndpoint() {
        return fallbackToConfiguredEndpoint;
    }

    public void setFallbackToConfiguredEndpoint(boolean fallbackToConfiguredEndpoint) {
        this.fallbackToConfiguredEndpoint = fallbackToConfiguredEndpoint;
    }

    /** 부정확한 Timeout/Retry/Circuit 설정을 기동 전에 차단합니다. */
    public void validate() {
        if (defaultTimeoutMillis <= 0) throw new IllegalStateException("cpf.service-call.default-timeout-millis must be > 0");
        if (defaultRetryCount < 0 || maxRetryCount < 0 || defaultRetryCount > maxRetryCount)
            throw new IllegalStateException("cpf.service-call retry count is invalid");
        if (maxHistoryQueryLimit <= 0) throw new IllegalStateException("cpf.service-call.max-history-query-limit must be > 0");
        if (circuitOpenFailureThreshold <= 0) throw new IllegalStateException("cpf.service-call.circuit-open-failure-threshold must be > 0");
        if (circuitOpenRetryAfterMillis < 0 || retryBackoffMillis < 0 || maxRetryBackoffMillis < retryBackoffMillis)
            throw new IllegalStateException("cpf.service-call backoff/circuit timing is invalid");
    }
}

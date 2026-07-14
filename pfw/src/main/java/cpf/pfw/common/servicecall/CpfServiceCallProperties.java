package cpf.pfw.common.servicecall;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * CPF 서비스 호출 엔진 기본 설정입니다.
 *
 * <p>업무 모듈은 이 설정을 직접 읽기보다 {@link CpfServiceCallEngine}과 {@code CpfWebClient}를 통해
 * timeout, retry, circuit 정책을 일관되게 적용합니다.</p>
 */
@ConfigurationProperties(prefix = "cpf.service-call")
public class CpfServiceCallProperties {
    private boolean enabled = true;
    private int defaultTimeoutMillis = 3000;
    private int defaultRetryCount = 1;
    private int maxHistoryQueryLimit = 500;
    private int maxRetryCount = 3;
    private int circuitOpenFailureThreshold = 3;
    private long circuitOpenRetryAfterMillis = 30000;
    private long retryBackoffMillis = 100;
    private long maxRetryBackoffMillis = 2000;
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
}

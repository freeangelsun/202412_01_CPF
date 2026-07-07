package cpf.pfw.common.servicecall;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * CPF 서비스 호출 엔진 기본 설정입니다.
 */
@ConfigurationProperties(prefix = "cpf.service-call")
public class CpfServiceCallProperties {
    private boolean enabled = true;
    private int defaultTimeoutMillis = 3000;
    private int defaultRetryCount = 0;
    private int maxHistoryQueryLimit = 500;

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
}

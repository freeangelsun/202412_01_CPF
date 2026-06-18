package cpf.adm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * CPF 기능 설명입니다.
 */
@ConfigurationProperties(prefix = "cpf.adm.password-policy")
public class AdmPasswordPolicyProperties {
    private int expireDays = 90;
    private int minLength = 10;
    private int requiredCategoryCount = 3;
    private int maxFailCount = 5;
    private int historyCount = 3;

    public int getExpireDays() {
        return expireDays;
    }

    public void setExpireDays(int expireDays) {
        this.expireDays = expireDays;
    }

    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public int getRequiredCategoryCount() {
        return requiredCategoryCount;
    }

    public void setRequiredCategoryCount(int requiredCategoryCount) {
        this.requiredCategoryCount = requiredCategoryCount;
    }

    public int getMaxFailCount() {
        return maxFailCount;
    }

    public void setMaxFailCount(int maxFailCount) {
        this.maxFailCount = maxFailCount;
    }

    public int getHistoryCount() {
        return historyCount;
    }

    public void setHistoryCount(int historyCount) {
        this.historyCount = historyCount;
    }
}


package cpf.pfw.common.logging;

import java.time.Duration;

/**
 * CPF 기능 설명입니다.
 *
 * CPF 기능 설명입니다.
 */
public class DynamicLogLevelRequest {
    private String transactionId;
    private String businessTransactionId;
    private String moduleId;
    private CpfLogLevel logLevel = CpfLogLevel.DEBUG;
    private Duration ttl = Duration.ofMinutes(10);
    private String reason = "CPF 처리 기준입니다.";
    private String requestUser = "SYSTEM";

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getBusinessTransactionId() {
        return businessTransactionId;
    }

    public void setBusinessTransactionId(String businessTransactionId) {
        this.businessTransactionId = businessTransactionId;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public CpfLogLevel getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(CpfLogLevel logLevel) {
        this.logLevel = logLevel;
    }

    public Duration getTtl() {
        return ttl;
    }

    public void setTtl(Duration ttl) {
        this.ttl = ttl;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getRequestUser() {
        return requestUser;
    }

    public void setRequestUser(String requestUser) {
        this.requestUser = requestUser;
    }
}


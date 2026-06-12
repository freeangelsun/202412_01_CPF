package fps.pfw.common.logging;

import java.time.Duration;

/**
 * 동적 거래 로그레벨 등록 요청입니다.
 *
 * <p>ADM 운영 화면이나 샘플 컨트롤러가 이 요청 객체를 만들어
 * {@link DynamicTransactionLogLevelService}에 전달합니다.</p>
 */
public class DynamicLogLevelRequest {
    private String transactionId;
    private String businessTransactionId;
    private String moduleId;
    private FpsLogLevel logLevel = FpsLogLevel.DEBUG;
    private Duration ttl = Duration.ofMinutes(10);
    private String reason = "운영 진단";
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

    public FpsLogLevel getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(FpsLogLevel logLevel) {
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

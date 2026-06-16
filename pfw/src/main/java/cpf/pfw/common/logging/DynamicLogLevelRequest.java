package cpf.pfw.common.logging;

import java.time.Duration;

/**
 * ?숈쟻 嫄곕옒 濡쒓렇?덈꺼 ?깅줉 ?붿껌?낅땲??
 *
 * <p>ADM ?댁쁺 ?붾㈃?대굹 ?섑뵆 而⑦듃濡ㅻ윭媛 ???붿껌 媛앹껜瑜?留뚮뱾?? * {@link DynamicTransactionLogLevelService}???꾨떖?⑸땲??</p>
 */
public class DynamicLogLevelRequest {
    private String transactionId;
    private String businessTransactionId;
    private String moduleId;
    private FpsLogLevel logLevel = FpsLogLevel.DEBUG;
    private Duration ttl = Duration.ofMinutes(10);
    private String reason = "?댁쁺 吏꾨떒";
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


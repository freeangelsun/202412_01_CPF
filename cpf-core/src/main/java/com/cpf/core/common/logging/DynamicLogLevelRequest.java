package com.cpf.core.common.logging;

import java.time.Duration;

/**
 * 동적 거래 로그 레벨 등록 요청입니다.
 *
 * <p>운영자가 특정 트랜잭션 또는 업무 거래를 짧은 기간 동안 더 자세히 추적할 때 사용합니다.
 * 둘 중 하나의 식별자는 반드시 필요하며, TTL이 지나면 런타임 규칙에서 제거됩니다.</p>
 */
public class DynamicLogLevelRequest {
    private String transactionId;
    private String businessTransactionId;
    private String moduleId;
    private CpfLogLevel logLevel = CpfLogLevel.DEBUG;
    private Duration ttl = Duration.ofMinutes(10);
    private String reason;
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


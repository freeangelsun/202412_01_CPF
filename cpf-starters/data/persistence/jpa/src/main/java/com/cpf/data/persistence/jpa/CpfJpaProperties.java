package com.cpf.data.persistence.jpa;
import com.cpf.core.api.config.CpfConfigMutability;
import com.cpf.core.api.config.CpfConfigPolicy;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** CPF JPA Provider의 안전 기본값입니다. */
@CpfConfigPolicy(prefix="cpf.data.persistence.jpa", mutability=CpfConfigMutability.RESTART_REQUIRED, secretSeparated=false)
@ConfigurationProperties("cpf.data.persistence.jpa")
public class CpfJpaProperties {
    private int queryTimeoutMs = 3000;
    private long slowQueryThresholdMs = 500;
    private int bulkFlushSize = 100;
    private int maxBulkSize = 1000;
    /** 설정값의 의미와 기본 동작을 명확히 하여 운영 설정 영향을 추적한다. */
    private boolean requireJta;

    public int getQueryTimeoutMs() { return queryTimeoutMs; }
    public void setQueryTimeoutMs(int value) { if (value < 1) throw new IllegalArgumentException("queryTimeoutMs는 1 이상이어야 합니다."); queryTimeoutMs = value; }
    public long getSlowQueryThresholdMs() { return slowQueryThresholdMs; }
    public void setSlowQueryThresholdMs(long value) { if (value < 1) throw new IllegalArgumentException("slowQueryThresholdMs는 1 이상이어야 합니다."); slowQueryThresholdMs = value; }
    public int getBulkFlushSize() { return bulkFlushSize; }
    public void setBulkFlushSize(int value) { if (value < 1 || value > 10000) throw new IllegalArgumentException("bulkFlushSize는 1~10000 범위여야 합니다."); bulkFlushSize = value; }
    public int getMaxBulkSize() { return maxBulkSize; }
    public void setMaxBulkSize(int value) { if (value < 1 || value > 100000) throw new IllegalArgumentException("maxBulkSize는 1~100000 범위여야 합니다."); maxBulkSize = value; }
    public boolean isRequireJta() { return requireJta; }
    public void setRequireJta(boolean value) { requireJta = value; }
}

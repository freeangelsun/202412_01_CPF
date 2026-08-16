package com.cpf.data.cache.rediscommon;

import java.time.Duration;

/** Shared validated configuration used by Redis-protocol cache providers. */
/** CpfRedisLikeProviderProperties 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public class CpfRedisLikeProviderProperties {
    private boolean enabled;
    private String keyPrefix = "cpf:";
    private String invalidationChannel = "cpf.cache.invalidate";
    private Duration defaultTtl = Duration.ofMinutes(10);
    private boolean required = true;
    private boolean tls;
    private long maximumPayloadBytes = 1_048_576;
    private long scanCount = 500;
    public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
    public String getKeyPrefix(){return keyPrefix;} public void setKeyPrefix(String v){keyPrefix=v;}
    public String getInvalidationChannel(){return invalidationChannel;} public void setInvalidationChannel(String v){invalidationChannel=v;}
    public Duration getDefaultTtl(){return defaultTtl;} public void setDefaultTtl(Duration v){defaultTtl=v;}
    public boolean isRequired(){return required;} public void setRequired(boolean v){required=v;}
    public boolean isTls(){return tls;} public void setTls(boolean v){tls=v;}
    public long getMaximumPayloadBytes(){return maximumPayloadBytes;} public void setMaximumPayloadBytes(long v){maximumPayloadBytes=v;}
    public long getScanCount(){return scanCount;} public void setScanCount(long v){scanCount=v;}
    /** validate 작업을 CPF 표준 계약에 따라 수행한다. */
    public void validate(String providerName) {
        if (!enabled) return;
        if (keyPrefix == null || keyPrefix.isBlank() || !keyPrefix.matches("[A-Za-z0-9._:-]{1,120}")) throw new IllegalStateException(providerName+" key-prefix format is invalid");
        if (invalidationChannel == null || !invalidationChannel.matches("[A-Za-z0-9._:-]{1,180}")) throw new IllegalStateException(providerName+" invalidation-channel format is invalid");
        if (defaultTtl == null || defaultTtl.isNegative() || defaultTtl.isZero()) throw new IllegalStateException(providerName+" default-ttl must be positive");
        if (maximumPayloadBytes < 1 || maximumPayloadBytes > 64L*1024*1024) throw new IllegalStateException(providerName+" maximum-payload-bytes out of range");
        if (scanCount < 10 || scanCount > 10_000) throw new IllegalStateException(providerName+" scan-count out of range");
    }
}

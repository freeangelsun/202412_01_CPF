package com.cpf.integration.realtime;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "cpf.integration.realtime")
/** CpfRealtimeProperties 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public class CpfRealtimeProperties {
    private boolean enabled;
    private String streamPath = "/cpf/realtime/stream";
    private String pollPath = "/cpf/realtime/events";
    private int replayCapacity = 2_000;
    private int subscriberQueueCapacity = 128;
    private int maxConnectionsPerTenant = 100;
    /** 설정값의 의미와 기본 동작을 명확히 하여 운영 설정 영향을 추적한다. */
    private int maxSubscribeAttemptsPerSecond = 30;
    private int pollLimit = 200;
    private Duration emitterTimeout = Duration.ofMinutes(30);
    private Duration heartbeatInterval = Duration.ofSeconds(15);
    private Duration drainTimeout = Duration.ofSeconds(20);

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getStreamPath() { return streamPath; }
    public void setStreamPath(String streamPath) { this.streamPath = streamPath; }
    public String getPollPath() { return pollPath; }
    public void setPollPath(String pollPath) { this.pollPath = pollPath; }
    public int getReplayCapacity() { return replayCapacity; }
    public void setReplayCapacity(int replayCapacity) { this.replayCapacity = positive(replayCapacity, "replayCapacity"); }
    public int getSubscriberQueueCapacity() { return subscriberQueueCapacity; }
    public void setSubscriberQueueCapacity(int value) { this.subscriberQueueCapacity = positive(value, "subscriberQueueCapacity"); }
    public int getMaxConnectionsPerTenant() { return maxConnectionsPerTenant; }
    public void setMaxConnectionsPerTenant(int value) { this.maxConnectionsPerTenant = positive(value, "maxConnectionsPerTenant"); }
    public int getMaxSubscribeAttemptsPerSecond() { return maxSubscribeAttemptsPerSecond; }
    public void setMaxSubscribeAttemptsPerSecond(int value) { this.maxSubscribeAttemptsPerSecond = positive(value, "maxSubscribeAttemptsPerSecond"); }
    public int getPollLimit() { return pollLimit; }
    public void setPollLimit(int value) { this.pollLimit = positive(value, "pollLimit"); }
    public Duration getEmitterTimeout() { return emitterTimeout; }
    public void setEmitterTimeout(Duration value) { emitterTimeout = duration(value, "emitterTimeout"); }
    public Duration getHeartbeatInterval() { return heartbeatInterval; }
    public void setHeartbeatInterval(Duration value) { heartbeatInterval = duration(value, "heartbeatInterval"); }
    public Duration getDrainTimeout() { return drainTimeout; }
    public void setDrainTimeout(Duration value) { drainTimeout = duration(value, "drainTimeout"); }

    private static int positive(int value, String name) {
        if (value < 1) throw new IllegalArgumentException(name);
        return value;
    }
    private static Duration duration(Duration value, String name) {
        if (value == null || value.isNegative() || value.isZero()) throw new IllegalArgumentException(name);
        return value;
    }
}

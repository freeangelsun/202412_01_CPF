package com.cpf.starter.integration.realtime;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("cpf.integration.realtime")
public class CpfRealtimeProperties {
    private boolean enabled;
    private Duration timeout = Duration.ofMinutes(30);
    private Duration heartbeat = Duration.ofSeconds(15);
    private int maxClients = 1000;
    private int maxClientsPerTopic = 200;
    private int replayHistorySize = 256;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Duration getTimeout() { return timeout; }
    public void setTimeout(Duration timeout) { this.timeout = requirePositive(timeout, "timeout"); }
    public Duration getHeartbeat() { return heartbeat; }
    public void setHeartbeat(Duration heartbeat) { this.heartbeat = requirePositive(heartbeat, "heartbeat"); }
    public int getMaxClients() { return maxClients; }
    public void setMaxClients(int value) { maxClients = positive(value, "maxClients"); }
    public int getMaxClientsPerTopic() { return maxClientsPerTopic; }
    public void setMaxClientsPerTopic(int value) { maxClientsPerTopic = positive(value, "maxClientsPerTopic"); }
    public int getReplayHistorySize() { return replayHistorySize; }
    public void setReplayHistorySize(int value) { replayHistorySize = positive(value, "replayHistorySize"); }

    private static int positive(int value, String name) { if (value < 1) throw new IllegalArgumentException(name + " must be >= 1"); return value; }
    private static Duration requirePositive(Duration value, String name) { if (value == null || value.isZero() || value.isNegative()) throw new IllegalArgumentException(name + " must be positive"); return value; }
}

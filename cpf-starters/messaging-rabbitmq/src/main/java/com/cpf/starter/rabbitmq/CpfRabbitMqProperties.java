package com.cpf.starter.rabbitmq;

import java.time.Duration;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("cpf.messaging.rabbitmq")
public class CpfRabbitMqProperties {
    private boolean enabled;
    private String bindingName = "rabbitmq";
    private boolean defaultBinding;
    private String exchange;
    private String exchangeType = "topic";
    private String queue;
    private String routingKey = "#";
    private boolean durable = true;
    private boolean quorum = true;
    private int prefetch = 50;
    private int concurrency = 1;
    private int maxPayloadBytes = 1_048_576;
    private Duration confirmTimeout = Duration.ofSeconds(10);

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean value) { enabled = value; }
    public String getBindingName() { return bindingName; }
    public void setBindingName(String value) { bindingName = value; }
    public boolean isDefaultBinding() { return defaultBinding; }
    public void setDefaultBinding(boolean value) { defaultBinding = value; }
    public String getExchange() { return exchange; }
    public void setExchange(String value) { exchange = value; }
    public String getExchangeType() { return exchangeType; }
    public void setExchangeType(String value) { exchangeType = value; }
    public String getQueue() { return queue; }
    public void setQueue(String value) { queue = value; }
    public String getRoutingKey() { return routingKey; }
    public void setRoutingKey(String value) { routingKey = value; }
    public boolean isDurable() { return durable; }
    public void setDurable(boolean value) { durable = value; }
    public boolean isQuorum() { return quorum; }
    public void setQuorum(boolean value) { quorum = value; }
    public int getPrefetch() { return prefetch; }
    public void setPrefetch(int value) { prefetch = value; }
    public int getConcurrency() { return concurrency; }
    public void setConcurrency(int value) { concurrency = value; }
    public int getMaxPayloadBytes() { return maxPayloadBytes; }
    public void setMaxPayloadBytes(int value) { maxPayloadBytes = value; }
    public Duration getConfirmTimeout() { return confirmTimeout; }
    public void setConfirmTimeout(Duration value) { confirmTimeout = value; }

    public void validate() {
        if (!enabled) {
            return;
        }
        if (isBlank(bindingName) || isBlank(exchange) || isBlank(queue)) {
            throw new IllegalStateException("binding-name, exchange and queue are required");
        }
        if (!Set.of("direct", "topic", "fanout", "headers").contains(exchangeType)) {
            throw new IllegalStateException("Unsupported exchange-type: " + exchangeType);
        }
        if (prefetch < 1 || concurrency < 1 || maxPayloadBytes < 1) {
            throw new IllegalStateException(
                    "prefetch, concurrency and max-payload-bytes must be positive");
        }
        if (confirmTimeout == null || confirmTimeout.isZero() || confirmTimeout.isNegative()) {
            throw new IllegalStateException("confirm-timeout must be positive");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

package com.cpf.messaging.jms;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("cpf.messaging.jms")
/** CpfJmsProperties 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public class CpfJmsProperties {
    private boolean enabled;
    private String bindingName = "jms";
    private boolean defaultBinding;
    private String destination;
    private boolean pubSubDomain;
    private boolean sessionTransacted = true;
    /** 설정값의 의미와 기본 동작을 명확히 하여 운영 설정 영향을 추적한다. */
    private int acknowledgementMode = 2;
    private int maxPayloadBytes = 1_048_576;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean value) { enabled = value; }
    public String getBindingName() { return bindingName; }
    public void setBindingName(String value) { bindingName = value; }
    public boolean isDefaultBinding() { return defaultBinding; }
    public void setDefaultBinding(boolean value) { defaultBinding = value; }
    public String getDestination() { return destination; }
    public void setDestination(String value) { destination = value; }
    public boolean isPubSubDomain() { return pubSubDomain; }
    public void setPubSubDomain(boolean value) { pubSubDomain = value; }
    public boolean isSessionTransacted() { return sessionTransacted; }
    public void setSessionTransacted(boolean value) { sessionTransacted = value; }
    public int getAcknowledgementMode() { return acknowledgementMode; }
    public void setAcknowledgementMode(int value) { acknowledgementMode = value; }
    public int getMaxPayloadBytes() { return maxPayloadBytes; }
    public void setMaxPayloadBytes(int value) { maxPayloadBytes = value; }

    /** validate 작업을 CPF 표준 계약에 따라 수행한다. */
    public void validate() {
        if (!enabled) {
            return;
        }
        if (bindingName == null || bindingName.isBlank()
                || destination == null || destination.isBlank()) {
            throw new IllegalStateException("JMS binding-name and destination are required");
        }
        if (maxPayloadBytes < 1) {
            throw new IllegalStateException("JMS max-payload-bytes must be positive");
        }
    }
}

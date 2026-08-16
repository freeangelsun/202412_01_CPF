package com.cpf.messaging.ibmmq;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("cpf.messaging.ibm-mq")
/** CpfIbmMqProperties 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public class CpfIbmMqProperties {
    private boolean enabled;
    private String queueManager;
    private String channel;
    private String connectionName;
    private String ccdtUrl;
    private boolean tlsRequired = true;
    /** 설정값의 의미와 기본 동작을 명확히 하여 운영 설정 영향을 추적한다. */
    private String bindingName = "ibm-mq";
    private boolean defaultBinding;
    private String destination;
    private int maxPayloadBytes = 1_048_576;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean value) { enabled = value; }
    public String getQueueManager() { return queueManager; }
    public void setQueueManager(String value) { queueManager = value; }
    public String getChannel() { return channel; }
    public void setChannel(String value) { channel = value; }
    public String getConnectionName() { return connectionName; }
    public void setConnectionName(String value) { connectionName = value; }
    public String getCcdtUrl() { return ccdtUrl; }
    public void setCcdtUrl(String value) { ccdtUrl = value; }
    public boolean isTlsRequired() { return tlsRequired; }
    public void setTlsRequired(boolean value) { tlsRequired = value; }
    public String getBindingName() { return bindingName; }
    public void setBindingName(String value) { bindingName = value; }
    public boolean isDefaultBinding() { return defaultBinding; }
    public void setDefaultBinding(boolean value) { defaultBinding = value; }
    public String getDestination() { return destination; }
    public void setDestination(String value) { destination = value; }
    public int getMaxPayloadBytes() { return maxPayloadBytes; }
    public void setMaxPayloadBytes(int value) { maxPayloadBytes = value; }

    /** validate 작업을 CPF 표준 계약에 따라 수행한다. */
    public void validate() {
        if (!enabled) return;
        if (bindingName == null || bindingName.isBlank() || destination == null || destination.isBlank() || maxPayloadBytes < 1) {
            throw new IllegalStateException("IBM MQ binding/destination/limit required");
        }
        if (queueManager == null || queueManager.isBlank()) throw new IllegalStateException("IBM MQ queue-manager is required");
        boolean ccdtConfigured = ccdtUrl != null && !ccdtUrl.isBlank();
        boolean channelConfigured = channel != null && !channel.isBlank() && connectionName != null && !connectionName.isBlank();
        if (!ccdtConfigured && !channelConfigured) throw new IllegalStateException("IBM MQ requires CCDT or channel+connection-name");
    }
}

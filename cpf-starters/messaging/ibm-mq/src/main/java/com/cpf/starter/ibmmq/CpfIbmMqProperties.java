package com.cpf.starter.ibmmq;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("cpf.messaging.ibm-mq")
public class CpfIbmMqProperties {
    private boolean enabled;
    private String queueManager;
    private String channel;
    private String connectionName;
    private String ccdtUrl;
    private boolean tlsRequired = true;
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

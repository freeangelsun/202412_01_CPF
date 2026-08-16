package com.cpf.integration.tcp;

import java.nio.charset.Charset;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("cpf.integration.tcp")
/** CpfTcpProperties 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public class CpfTcpProperties {
    public enum Mode { CLIENT, SERVER }
    public enum Frame { FIXED, LENGTH_HEADER, STX_ETX, CRLF }

    private boolean enabled;
    private Mode mode = Mode.CLIENT;
    private String host = "127.0.0.1";
    /** 설정값의 의미와 기본 동작을 명확히 하여 운영 설정 영향을 추적한다. */
    private int port;
    private int poolSize = 4;
    private Duration connectTimeout = Duration.ofSeconds(3);
    private Duration responseTimeout = Duration.ofSeconds(10);
    private Duration idleTimeout = Duration.ofSeconds(60);
    private Frame frame = Frame.LENGTH_HEADER;
    private int fixedLength;
    /** 설정값의 의미와 기본 동작을 명확히 하여 운영 설정 영향을 추적한다. */
    private int maxFrameBytes = 1024 * 1024;
    private String charset = "UTF-8";
    private boolean tls;
    private boolean mutualTls;
    private String keyStore;
    private String trustStore;
    private String keyStorePassword;
    /** 설정값의 의미와 기본 동작을 명확히 하여 운영 설정 영향을 추적한다. */
    private String trustStorePassword;
    private int maxPending = 10_000;
    private int maxOrphans = 1_000;
    private int maxUnknownResults = 10_000;
    private String unknownResultJournal = "runtime/cpf/tcp/unknown-results.journal";
    private Duration reconnectInitial = Duration.ofMillis(200);
    private Duration reconnectMax = Duration.ofSeconds(30);
    /** 설정값의 의미와 기본 동작을 명확히 하여 운영 설정 영향을 추적한다. */
    private double reconnectJitter = 0.2;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean value) { enabled = value; }
    public Mode getMode() { return mode; }
    public void setMode(Mode value) { mode = value; }
    public String getHost() { return host; }
    public void setHost(String value) { host = value; }
    public int getPort() { return port; }
    public void setPort(int value) { port = value; }
    public int getPoolSize() { return poolSize; }
    public void setPoolSize(int value) { poolSize = value; }
    public Duration getConnectTimeout() { return connectTimeout; }
    public void setConnectTimeout(Duration value) { connectTimeout = value; }
    public Duration getResponseTimeout() { return responseTimeout; }
    public void setResponseTimeout(Duration value) { responseTimeout = value; }
    public Duration getIdleTimeout() { return idleTimeout; }
    public void setIdleTimeout(Duration value) { idleTimeout = value; }
    public Frame getFrame() { return frame; }
    public void setFrame(Frame value) { frame = value; }
    public int getFixedLength() { return fixedLength; }
    public void setFixedLength(int value) { fixedLength = value; }
    public int getMaxFrameBytes() { return maxFrameBytes; }
    public void setMaxFrameBytes(int value) { maxFrameBytes = value; }
    public String getCharset() { return charset; }
    public void setCharset(String value) { charset = value; }
    public boolean isTls() { return tls; }
    public void setTls(boolean value) { tls = value; }
    public boolean isMutualTls() { return mutualTls; }
    public void setMutualTls(boolean value) { mutualTls = value; }
    public String getKeyStore() { return keyStore; }
    public void setKeyStore(String value) { keyStore = value; }
    public String getTrustStore() { return trustStore; }
    public void setTrustStore(String value) { trustStore = value; }
    public String getKeyStorePassword() { return keyStorePassword; }
    public void setKeyStorePassword(String value) { keyStorePassword = value; }
    public String getTrustStorePassword() { return trustStorePassword; }
    public void setTrustStorePassword(String value) { trustStorePassword = value; }
    public int getMaxPending() { return maxPending; }
    public void setMaxPending(int value) { maxPending = value; }
    public int getMaxOrphans() { return maxOrphans; }
    public void setMaxOrphans(int value) { maxOrphans = value; }
    public int getMaxUnknownResults() { return maxUnknownResults; }
    public void setMaxUnknownResults(int value) { maxUnknownResults = value; }
    public String getUnknownResultJournal() { return unknownResultJournal; }
    public void setUnknownResultJournal(String value) { unknownResultJournal = value; }
    public Duration getReconnectInitial() { return reconnectInitial; }
    public void setReconnectInitial(Duration value) { reconnectInitial = value; }
    public Duration getReconnectMax() { return reconnectMax; }
    public void setReconnectMax(Duration value) { reconnectMax = value; }
    public double getReconnectJitter() { return reconnectJitter; }
    public void setReconnectJitter(double value) { reconnectJitter = value; }

    /** resolvedCharset 작업을 CPF 표준 계약에 따라 수행한다. */
    public Charset resolvedCharset() { return Charset.forName(charset); }

    public void validate() {
        if (!enabled) return;
        if (port < 1 || port > 65_535 || poolSize < 1 || poolSize > 256 || maxFrameBytes < 1) {
            throw new IllegalStateException("invalid TCP port/pool/max-frame");
        }
        if (frame == Frame.FIXED && (fixedLength < 1 || fixedLength > maxFrameBytes)) {
            throw new IllegalStateException("fixed-length is required for FIXED frame");
        }
        if (mutualTls && !tls) throw new IllegalStateException("mutual-tls requires tls");
        if (tls && (keyStore == null || trustStore == null || keyStorePassword == null || trustStorePassword == null)) {
            throw new IllegalStateException("TLS requires key-store/trust-store and secret references");
        }
        if (maxPending < 1 || maxOrphans < 1 || maxUnknownResults < 1) {
            throw new IllegalStateException("invalid TCP tracking limits");
        }
        if (unknownResultJournal == null || unknownResultJournal.isBlank()) {
            throw new IllegalStateException("unknown-result-journal is required for durable UNKNOWN results");
        }
        validateTimeout(connectTimeout, "connect-timeout");
        validateTimeout(responseTimeout, "response-timeout");
        validateTimeout(idleTimeout, "idle-timeout");
        new CpfTcpReconnectPolicy(reconnectInitial, reconnectMax, reconnectJitter);
        resolvedCharset();
    }

    private static void validateTimeout(Duration timeout, String name) {
        if (timeout == null || timeout.isZero() || timeout.isNegative()
                || timeout.toMillis() > Integer.MAX_VALUE) {
            throw new IllegalStateException(name + " must be between 1ms and " + Integer.MAX_VALUE + "ms");
        }
    }
}

package com.cpf.starter.sftp;

import java.nio.file.Path;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuration contract for the CPF SFTP capability. */
@ConfigurationProperties("cpf.integration.sftp")
public class CpfSftpProperties {
    private boolean enabled;
    private String host;
    private int port = 22;
    private String username;
    private String passwordSecret;
    /** Legacy binding trap. A populated raw password is always rejected. */
    private String password;
    private Duration connectTimeout = Duration.ofSeconds(10);
    private Duration operationTimeout = Duration.ofSeconds(30);
    private int bufferBytes = 65_536;
    private long maxTransferBytes = 1024L * 1024 * 1024;
    private boolean ledgerRequired = true;
    private String localRoot = ".";
    private String remoteRoot = "/";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordSecret() {
        return passwordSecret;
    }

    public void setPasswordSecret(String passwordSecret) {
        this.passwordSecret = passwordSecret;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getOperationTimeout() {
        return operationTimeout;
    }

    public void setOperationTimeout(Duration operationTimeout) {
        this.operationTimeout = operationTimeout;
    }

    public int getBufferBytes() {
        return bufferBytes;
    }

    public void setBufferBytes(int bufferBytes) {
        this.bufferBytes = bufferBytes;
    }

    public long getMaxTransferBytes() {
        return maxTransferBytes;
    }

    public void setMaxTransferBytes(long maxTransferBytes) {
        this.maxTransferBytes = maxTransferBytes;
    }

    public boolean isLedgerRequired() {
        return ledgerRequired;
    }

    public void setLedgerRequired(boolean ledgerRequired) {
        this.ledgerRequired = ledgerRequired;
    }

    public String getLocalRoot() {
        return localRoot;
    }

    public void setLocalRoot(String localRoot) {
        this.localRoot = localRoot;
    }

    public String getRemoteRoot() {
        return remoteRoot;
    }

    public void setRemoteRoot(String remoteRoot) {
        this.remoteRoot = remoteRoot;
    }

    public void validate() {
        if (!enabled) {
            return;
        }
        if (host == null || host.isBlank() || username == null || username.isBlank()) {
            throw new IllegalStateException("SFTP host and username are required");
        }
        if (password != null && !password.isBlank()) {
            throw new IllegalStateException(
                    "Raw cpf.integration.sftp.password is forbidden; use password-secret=provider:key");
        }
        if (passwordSecret == null
                || !passwordSecret.matches("^[A-Za-z0-9_.-]+:.+$")) {
            throw new IllegalStateException(
                    "SFTP password-secret=provider:key is required");
        }
        if (port < 1 || port > 65_535) {
            throw new IllegalStateException("SFTP port is outside 1..65535");
        }
        if (bufferBytes < 4_096 || maxTransferBytes < 1) {
            throw new IllegalStateException("SFTP transfer limits are invalid");
        }
        if (!ledgerRequired) {
            throw new IllegalStateException("SFTP durable transfer ledger cannot be disabled");
        }
        requirePositive(connectTimeout, "connect-timeout");
        requirePositive(operationTimeout, "operation-timeout");
        if (localRoot == null || localRoot.isBlank()
                || remoteRoot == null || remoteRoot.isBlank()) {
            throw new IllegalStateException("SFTP local-root and remote-root are required");
        }
        Path.of(localRoot).toAbsolutePath().normalize();
    }

    private static void requirePositive(Duration duration, String name) {
        if (duration == null || duration.isZero() || duration.isNegative()) {
            throw new IllegalStateException(name + " must be positive");
        }
    }
}

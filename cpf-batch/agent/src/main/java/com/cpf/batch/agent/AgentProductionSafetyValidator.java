package com.cpf.batch.agent;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;

/** Production Host Agent의 TLS, Trust, 영속 Command Ledger 설정을 fail-closed 검증합니다. */
public final class AgentProductionSafetyValidator implements ApplicationRunner {
    private final Environment environment;
    private final AgentProperties properties;

    public AgentProductionSafetyValidator(Environment environment, AgentProperties properties) {
        this.environment = environment;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments arguments) {
        boolean production = Arrays.stream(environment.getActiveProfiles()).anyMatch("prod"::equalsIgnoreCase);
        if (!production) return;
        if (!environment.getProperty("server.ssl.enabled", Boolean.class, false)) {
            throw new IllegalStateException("Host Agent prod requires TLS");
        }
        if (!"need".equalsIgnoreCase(environment.getProperty("server.ssl.client-auth", ""))) {
            throw new IllegalStateException("Host Agent prod requires mTLS");
        }
        if (properties.getAllowedClientSubjects().isEmpty()) {
            throw new IllegalStateException("Host Agent prod requires approved mTLS client subjects");
        }
        if (!properties.isRequireSignature()) throw new IllegalStateException("Artifact signature is required");
        if (properties.getArtifactRepositoryBaseUrl() == null
                || !properties.getArtifactRepositoryBaseUrl().startsWith("https://")) {
            throw new IllegalStateException("HTTPS artifact repository required");
        }
        if (properties.getArtifactTrustStore().isEmpty()) {
            throw new IllegalStateException("Artifact trust store is required");
        }
        Instant now = Instant.now();
        for (var entry : properties.getArtifactTrustStore().entrySet()) {
            AgentProperties.TrustedKey key = entry.getValue();
            if (entry.getKey() == null || entry.getKey().isBlank() || key == null || key.isRevoked()) {
                throw new IllegalStateException("Artifact trust store contains an invalid or revoked key");
            }
            if (key.getNotBefore() == null || key.getNotAfter() == null
                    || !key.getNotBefore().isBefore(key.getNotAfter()) || !now.isBefore(key.getNotAfter())) {
                throw new IllegalStateException("Artifact trust key validity window is invalid");
            }
            Path keyPath = Path.of(key.getPublicKeyPath()).toAbsolutePath().normalize();
            if (!Files.isRegularFile(keyPath, LinkOption.NOFOLLOW_LINKS) || Files.isSymbolicLink(keyPath)) {
                throw new IllegalStateException("Trusted Ed25519 public key file is required");
            }
        }
        try {
            byte[] macKey = Base64.getDecoder().decode(properties.getArtifactStateMacKeyBase64());
            if (macKey.length < 32) throw new IllegalStateException("Artifact state MAC key must be at least 256 bits");
        } catch (RuntimeException failure) {
            throw new IllegalStateException("A valid artifact state MAC key is required", failure);
        }
        Path ledger = Path.of(properties.getCommandLedgerRoot());
        if (!ledger.isAbsolute()) throw new IllegalStateException("Host Agent prod requires an absolute command ledger path");
        Path normalized = ledger.normalize();
        Path temporary = Path.of(System.getProperty("java.io.tmpdir")).toAbsolutePath().normalize();
        if (normalized.startsWith(temporary)) throw new IllegalStateException("Command ledger cannot use an OS temporary directory");
        if (Files.exists(normalized) && Files.isSymbolicLink(normalized)) {
            throw new IllegalStateException("Command ledger root cannot be a symbolic link");
        }
        if (properties.getCommandLedgerRetentionSeconds() < 86_400L) {
            throw new IllegalStateException("Production command ledger retention must be at least one day");
        }
        if (properties.getServices().isEmpty()) throw new IllegalStateException("Approved service catalog is required");
        var serviceIds = new HashSet<String>();
        for (var service : properties.getServices().values()) {
            if (service.getServiceId() == null || service.getServiceId().isBlank()
                    || !serviceIds.add(service.getServiceId())) {
                throw new IllegalStateException("Approved service catalog contains a blank or duplicate serviceId");
            }
        }
    }
}

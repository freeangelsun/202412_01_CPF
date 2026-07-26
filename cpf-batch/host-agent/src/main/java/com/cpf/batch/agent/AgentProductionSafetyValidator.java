package com.cpf.batch.agent;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;

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
        if (!production) {
            return;
        }
        if (!environment.getProperty("server.ssl.enabled", Boolean.class, false)) {
            throw new IllegalStateException("Host Agent prod requires TLS");
        }
        if (!"need".equalsIgnoreCase(environment.getProperty("server.ssl.client-auth", ""))) {
            throw new IllegalStateException("Host Agent prod requires mTLS");
        }
        if (properties.getAllowedClientSubjects().isEmpty()) {
            throw new IllegalStateException("Host Agent prod requires approved mTLS client subjects");
        }
        if (!properties.isRequireSignature()) {
            throw new IllegalStateException("signature required");
        }
        if (properties.getArtifactRepositoryBaseUrl() == null
                || !properties.getArtifactRepositoryBaseUrl().startsWith("https://")) {
            throw new IllegalStateException("HTTPS artifact repository required");
        }
        if (properties.getArtifactPublicKeyPath() == null
                || !Files.isRegularFile(Path.of(properties.getArtifactPublicKeyPath()), LinkOption.NOFOLLOW_LINKS)
                || Files.isSymbolicLink(Path.of(properties.getArtifactPublicKeyPath()))) {
            throw new IllegalStateException("Trusted Ed25519 public key file is required");
        }
        if (properties.getServices().isEmpty()) {
            throw new IllegalStateException("Approved service catalog is required");
        }
        var serviceIds = new HashSet<String>();
        for (var service : properties.getServices().values()) {
            if (service.getServiceId() == null || service.getServiceId().isBlank()
                    || !serviceIds.add(service.getServiceId())) {
                throw new IllegalStateException("Approved service catalog contains a blank or duplicate serviceId");
            }
        }
    }
}

package com.cpf.batch.agent.internal;

import com.cpf.batch.agent.AgentProperties;
import com.cpf.batch.api.AgentArtifactRequest;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArtifactInstallerValidationTest {
    @TempDir Path temp;

    @Test
    void rejectsNonCanonicalOrMismatchedMavenCoordinatesThroughInstallBoundary() {
        ArtifactInstaller installer = installer("embedded-bootjar");

        assertThatThrownBy(() -> installer.install(request("../private:demo", "embedded-bootjar")))
                .isInstanceOf(SecurityException.class)
                .hasMessage("ARTIFACT_SERVICE_MISMATCH");
        assertThatThrownBy(() -> installer.install(request("com.cpf.demo:other", "embedded-bootjar")))
                .isInstanceOf(SecurityException.class)
                .hasMessage("ARTIFACT_SERVICE_MISMATCH");
    }

    @Test
    void runtimeModeMustMatchTheApprovedServiceCatalog() {
        assertThatThrownBy(() -> installer("embedded-bootjar")
                .install(request("com.cpf.demo:demo", "external-tomcat-war")))
                .isInstanceOf(SecurityException.class)
                .hasMessage("ARTIFACT_RUNTIME_MODE_MISMATCH");
        assertThatThrownBy(() -> installer("container")
                .install(request("com.cpf.demo:demo", "container")))
                .isInstanceOf(SecurityException.class)
                .hasMessage("ARTIFACT_RUNTIME_MODE_UNSUPPORTED");
    }

    private ArtifactInstaller installer(String approvedRuntimeMode) {
        AgentProperties properties = new AgentProperties();
        properties.setArtifactRepositoryBaseUrl("https://repo.example.test/");
        properties.setArtifactStateMacKeyBase64(Base64.getEncoder().encodeToString(new byte[32]));
        AgentProperties.ServiceDefinition service = new AgentProperties.ServiceDefinition();
        service.setServiceId("demo");
        service.setArtifactId("demo");
        service.setInstallRoot(temp.resolve(approvedRuntimeMode).toString());
        service.setRuntimeMode(approvedRuntimeMode);
        service.setEnvironmentCode("qa");
        service.setReleaseChannel("stable");
        properties.setServices(Map.of("demo", service));
        return new ArtifactInstaller(
                properties, new ArtifactVerifier(properties), new ArtifactStateStore(properties));
    }

    private static AgentArtifactRequest request(String coordinate, String runtimeMode) {
        return new AgentArtifactRequest(
                "demo", coordinate, "1.0.0", "0".repeat(64), "pending", runtimeMode,
                "vault://demo/config", "operator", "approved validation", 1L,
                "qa", "stable", "key-1");
    }
}

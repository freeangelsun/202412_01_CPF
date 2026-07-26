package com.cpf.batch.agent.internal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ArtifactInstallerValidationTest {
    @Test
    void acceptsOnlyCanonicalMavenCoordinates() {
        var coordinate = ArtifactInstaller.parseCoordinate(
                "com.cpf.batch:cpf-batch-worker");

        assertEquals("com.cpf.batch", coordinate.groupId());
        assertEquals("cpf-batch-worker", coordinate.artifactId());
        assertThrows(SecurityException.class,
                () -> ArtifactInstaller.parseCoordinate("../private:cpf-batch-worker"));
        assertThrows(SecurityException.class,
                () -> ArtifactInstaller.parseCoordinate(
                        "com.cpf.batch:cpf-batch-worker?classifier=evil"));
    }

    @Test
    void runtimeModeMustMatchTheApprovedServiceCatalog() {
        assertEquals(".jar", ArtifactInstaller.artifactExtension(
                "embedded-bootjar", "embedded-bootjar"));
        assertEquals(".war", ArtifactInstaller.artifactExtension(
                "external-tomcat-war", "external-tomcat-war"));
        assertThrows(SecurityException.class, () -> ArtifactInstaller.artifactExtension(
                "external-tomcat-war", "embedded-bootjar"));
        assertThrows(SecurityException.class, () -> ArtifactInstaller.artifactExtension(
                "container", "container"));
    }
}

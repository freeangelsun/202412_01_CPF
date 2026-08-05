package com.cpf.core.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CpfRuntimeControlAutoConfigurationTopologyTest {

    @Test
    void localControlPlaneIsDefaultWhenRemoteBaseUrlIsAbsent() {
        assertTrue(CpfRuntimeControlAutoConfiguration.usesLocalControlPlane(null));
        assertTrue(CpfRuntimeControlAutoConfiguration.usesLocalControlPlane("  "));
    }

    @Test
    void explicitRemoteBaseUrlSuppressesLocalControlPlaneBeans() {
        assertFalse(CpfRuntimeControlAutoConfiguration.usesLocalControlPlane("https://adm.internal"));
    }

    @Test
    void blankBaseUrlNeverActivatesRemoteTopology() {
        assertTrue(CpfRuntimeControlAutoConfiguration.usesLocalControlPlane(null));
        assertTrue(CpfRuntimeControlAutoConfiguration.usesLocalControlPlane("   "));
        assertFalse(CpfRuntimeControlAutoConfiguration.usesLocalControlPlane("https://adm.example"));
    }

    @Test
    void inboxDirectoryRejectsTraversalAndAbsoluteInstanceIdentity() {
        java.nio.file.Path base = java.nio.file.Path.of("runtime", "cpf-inbox");
        org.junit.jupiter.api.Assertions.assertEquals(
                base.toAbsolutePath().normalize().resolve("instance-1"),
                CpfRuntimeControlAutoConfiguration.runtimeInboxDirectory(base.toString(), " instance-1 "));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class,
                () -> CpfRuntimeControlAutoConfiguration.runtimeInboxDirectory(base.toString(), "../escape"));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class,
                () -> CpfRuntimeControlAutoConfiguration.runtimeInboxDirectory(base.toString(), "/absolute"));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class,
                () -> CpfRuntimeControlAutoConfiguration.runtimeInboxDirectory(" ", "instance-1"));
    }
}

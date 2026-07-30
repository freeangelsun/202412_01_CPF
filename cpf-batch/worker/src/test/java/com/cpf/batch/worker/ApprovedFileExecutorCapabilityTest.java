package com.cpf.batch.worker;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ApprovedFileExecutorCapabilityTest {
    @Test
    void remoteProviderDoesNotAdvertiseWatchScanOrClaim() {
        WorkerOperationalProperties properties = new WorkerOperationalProperties();
        WorkerOperationalProperties.PathAlias remote = new WorkerOperationalProperties.PathAlias();
        remote.setProvider("SFTP");
        remote.setHost("sftp.internal");
        remote.setCredentialId("batch-sftp");
        properties.setPathAliases(java.util.Map.of("REMOTE_INBOX", remote));
        var capability = new ApprovedFileExecutor(properties).capabilities("REMOTE_INBOX");
        assertFalse(capability.watchSupported());
        assertFalse(capability.restartScanSupported());
        assertFalse(capability.claimSupported());
        assertFalse(capability.transferSupported());
    }

    @Test
    void localSharedProviderAdvertisesRecoverableFunctions() {
        WorkerOperationalProperties properties = new WorkerOperationalProperties();
        WorkerOperationalProperties.PathAlias local = new WorkerOperationalProperties.PathAlias();
        local.setProvider("SHARED_FS");
        local.setRoot(System.getProperty("java.io.tmpdir"));
        local.setSharedDurable(true);
        properties.setPathAliases(java.util.Map.of("SHARED", local));
        var capability = new ApprovedFileExecutor(properties).capabilities("SHARED");
        assertTrue(capability.watchSupported());
        assertTrue(capability.restartScanSupported());
        assertTrue(capability.claimSupported());
        assertTrue(capability.transferSupported());
    }
}

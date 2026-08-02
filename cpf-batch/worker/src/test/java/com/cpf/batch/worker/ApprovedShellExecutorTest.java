package com.cpf.batch.worker;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ApprovedShellExecutorTest {

    @Test
    void deliversSensitiveParameterOutsideCommandLineAndVerifiesArtifact() throws Exception {
        Path java = javaExecutable();
        WorkerOperationalProperties properties = new WorkerOperationalProperties();
        WorkerOperationalProperties.ShellDefinition definition = new WorkerOperationalProperties.ShellDefinition();
        definition.setScriptId("JAVA_VERSION");
        definition.setExecutable(java.toString());
        definition.setFixedArguments(List.of(
                "-cp",
                Path.of(StdinConsumer.class.getProtectionDomain()
                                .getCodeSource().getLocation().toURI()).toString(),
                StdinConsumer.class.getName()));
        definition.setAllowedParameters(List.of("credential"));
        definition.setSensitiveParameters(List.of("credential"));
        definition.setParameterDeliveryMode("STDIN_JSON");
        definition.setSha256(sha256(java));
        definition.setVerificationMode("HASH_ONLY");
        definition.setTimeoutSeconds(10);
        properties.setScripts(Map.of("java-version", definition));

        ApprovedShellExecutor.Result result = new ApprovedShellExecutor(properties)
                .execute("java-version", Map.of("credential", "vault:batch/prod"));

        assertTrue(result.success(), result.output());
        assertEquals("SUCCESS", result.status());
        assertFalse(result.output().contains("vault:batch/prod"));
        assertEquals(64, result.artifactHash().length());
    }

    @Test
    void rejectsOversizedStdinJsonBeforeStartingTheApprovedProcess() throws Exception {
        Path java = javaExecutable();
        WorkerOperationalProperties properties = new WorkerOperationalProperties();
        WorkerOperationalProperties.ShellDefinition definition = new WorkerOperationalProperties.ShellDefinition();
        definition.setScriptId("STDIN_LIMIT");
        definition.setExecutable(java.toString());
        definition.setFixedArguments(List.of(
                "-cp",
                Path.of(StdinConsumer.class.getProtectionDomain()
                                .getCodeSource().getLocation().toURI()).toString(),
                StdinConsumer.class.getName()));
        definition.setAllowedParameters(List.of("credential"));
        definition.setSensitiveParameters(List.of("credential"));
        definition.setParameterDeliveryMode("STDIN_JSON");
        definition.setSha256(sha256(java));
        definition.setVerificationMode("HASH_ONLY");
        properties.setScripts(Map.of("stdin-limit", definition));

        SecurityException failure = assertThrows(SecurityException.class,
                () -> new ApprovedShellExecutor(properties)
                        .execute("stdin-limit", Map.of("credential", "x".repeat(1_048_577))));

        assertTrue(failure.getMessage().contains("byte limit"));
    }


    @Test
    void rejectsCatalogThatDisablesProcessTreeTermination() throws Exception {
        Path java = javaExecutable();
        WorkerOperationalProperties properties = new WorkerOperationalProperties();
        WorkerOperationalProperties.ShellDefinition definition = new WorkerOperationalProperties.ShellDefinition();
        definition.setExecutable(java.toString());
        definition.setFixedArguments(List.of("-version"));
        definition.setSha256(sha256(java));
        definition.setTerminateProcessTree(false);
        properties.setScripts(Map.of("unsafe", definition));

        assertThrows(IllegalArgumentException.class,
                () -> new ApprovedShellExecutor(properties).execute("unsafe", Map.of()));
    }

    @Test
    void rejectsUnknownParameterDeliveryMode() throws Exception {
        Path java = javaExecutable();
        WorkerOperationalProperties properties = new WorkerOperationalProperties();
        WorkerOperationalProperties.ShellDefinition definition = new WorkerOperationalProperties.ShellDefinition();
        definition.setExecutable(java.toString());
        definition.setFixedArguments(List.of("-version"));
        definition.setSha256(sha256(java));
        definition.setParameterDeliveryMode("INLINE_SCRIPT");
        properties.setScripts(Map.of("unsafe", definition));

        assertThrows(IllegalArgumentException.class,
                () -> new ApprovedShellExecutor(properties).execute("unsafe", Map.of()));
    }

    @Test
    void failsClosedWhenCatalogHashIsMissing() {
        Path java = javaExecutable();
        WorkerOperationalProperties properties = new WorkerOperationalProperties();
        WorkerOperationalProperties.ShellDefinition definition = new WorkerOperationalProperties.ShellDefinition();
        definition.setExecutable(java.toString());
        definition.setFixedArguments(List.of("-version"));
        properties.setScripts(Map.of("java-version", definition));

        assertThrows(SecurityException.class,
                () -> new ApprovedShellExecutor(properties).execute("java-version", Map.of()));
    }

    private static Path javaExecutable() {
        String executable = System.getProperty("os.name", "").toLowerCase().contains("win") ? "java.exe" : "java";
        return Path.of(System.getProperty("java.home"), "bin", executable).toAbsolutePath().normalize();
    }

    private static String sha256(Path path) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream input = Files.newInputStream(path)) {
            byte[] buffer = new byte[8192];
            for (int read; (read = input.read(buffer)) >= 0;) {
                if (read > 0) digest.update(buffer, 0, read);
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    /** Child process fixture that consumes the product STDIN_JSON delivery without echoing it. */
    public static final class StdinConsumer {
        public static void main(String[] args) throws Exception {
            byte[] buffer = new byte[1024];
            while (System.in.read(buffer) >= 0) {
                // Consume the complete JSON document; sensitive data must never be echoed.
            }
        }
    }
}

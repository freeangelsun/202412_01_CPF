package com.cpf.batch.worker;

import com.cpf.batch.runtime.SensitiveTextSanitizer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 승인된 Script Catalog만 실행합니다.
 *
 * <p>임의 Command String, Shell Expansion, Pipe/Redirect를 허용하지 않으며 Parameter는 기본적으로
 * 권한이 제한된 임시 파일로 전달합니다. Hash/Signature 검증, 출력 상한, Process Tree 종료,
 * 결과 불명 판정을 하나의 실행 경계에서 처리합니다.</p>
 */
@Component
public class ApprovedShellExecutor {
    private static final int MAX_STDIN_JSON_BYTES = 1_048_576;
    private final WorkerOperationalProperties properties;
    private final ObjectMapper objectMapper;
    private volatile List<ScriptArtifactVerifier> artifactVerifiers = List.of(new Sha256ScriptArtifactVerifier());

    @Autowired
    public ApprovedShellExecutor(WorkerOperationalProperties properties, ObjectMapper objectMapper) {
        this.properties = Objects.requireNonNull(properties, "properties");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
    }

    /** Source compatibility for isolated unit tests. */
    public ApprovedShellExecutor(WorkerOperationalProperties properties) {
        this(properties, new ObjectMapper());
    }

    @Autowired(required = false)
    void setArtifactVerifiers(List<ScriptArtifactVerifier> artifactVerifiers) {
        if (artifactVerifiers != null && !artifactVerifiers.isEmpty()) {
            this.artifactVerifiers = List.copyOf(artifactVerifiers);
        }
    }

    public Result execute(String scriptKey, Map<String, Object> parameters) throws Exception {
        WorkerOperationalProperties.ShellDefinition definition = properties.getScripts().get(scriptKey);
        if (definition == null) {
            throw new SecurityException("Approved script not found: " + scriptKey);
        }
        validateCatalogDefinition(scriptKey, definition);
        Map<String, Object> safeParameters = parameters == null ? Map.of() : Map.copyOf(parameters);
        validateParameters(definition, safeParameters);

        Path executable = Path.of(Objects.requireNonNull(definition.getExecutable(), "executable"))
                .toAbsolutePath().normalize();
        if (!Files.isRegularFile(executable) || Files.isSymbolicLink(executable)) {
            throw new IllegalStateException("Approved script artifact is missing or symbolic link");
        }

        ScriptArtifactVerifier.VerificationResult artifact = verifyArtifact(executable, definition);
        if (!artifact.valid()) {
            throw new SecurityException("Script artifact verification failed: " + artifact.code());
        }

        Path parameterFile = null;
        byte[] stdinJson = null;
        Instant startedAt = Instant.now();
        try {
            List<String> command = buildCommand(executable, definition, safeParameters);
            ProcessBuilder builder = new ProcessBuilder(command);
            configureWorkingDirectory(builder, definition);
            configureEnvironment(builder, definition);

            if ("STDIN_JSON".equalsIgnoreCase(definition.getParameterDeliveryMode())) {
                stdinJson = serializeParametersForStdin(safeParameters);
            } else if (!"COMMAND_LINE".equalsIgnoreCase(definition.getParameterDeliveryMode())) {
                parameterFile = writeParameterFile(safeParameters);
                builder.environment().put("CPF_BATCH_PARAMETER_FILE", parameterFile.toString());
            }

            Process process = builder.start();
            BoundedOutput stdout = new BoundedOutput(
                    definition.getMaxOutputBytes(), definition.getMaxOutputLinesPerSecond());
            BoundedOutput stderr = new BoundedOutput(
                    definition.getMaxOutputBytes(), definition.getMaxOutputLinesPerSecond());
            Thread outReader = Thread.ofVirtual().name("cpf-shell-stdout-" + scriptKey)
                    .start(() -> copyBounded(process.getInputStream(), stdout));
            Thread errReader = Thread.ofVirtual().name("cpf-shell-stderr-" + scriptKey)
                    .start(() -> copyBounded(process.getErrorStream(), stderr));

            AtomicReference<Throwable> stdinFailure = new AtomicReference<>();
            Thread stdinWriter = null;
            if (stdinJson != null) {
                byte[] stdinPayload = stdinJson;
                stdinWriter = Thread.ofVirtual().name("cpf-shell-stdin-" + scriptKey).start(() -> {
                    try {
                        writeParametersToStdin(process, stdinPayload);
                    } catch (Throwable failure) {
                        stdinFailure.set(failure);
                    }
                });
            } else {
                process.getOutputStream().close();
            }

            boolean finished = process.waitFor(Math.max(1, definition.getTimeoutSeconds()), TimeUnit.SECONDS);
            String status;
            boolean unknownResult = false;
            if (!finished) {
                boolean terminated = terminateProcessTree(process, definition);
                status = terminated ? "TIMEOUT" : "UNKNOWN_RESULT";
                unknownResult = !terminated;
            } else {
                status = classifyExit(definition, process.exitValue());
            }

            long joinMillis = Math.max(1, definition.getGracefulShutdownSeconds()) * 1_000L;
            if (stdinWriter != null) {
                stdinWriter.join(joinMillis);
                if (stdinWriter.isAlive()) {
                    try {
                        process.getOutputStream().close();
                    } catch (IOException ignored) {
                        // The process may already have closed its STDIN pipe.
                    }
                    stdinWriter.interrupt();
                    stdinWriter.join(joinMillis);
                    if (finished) {
                        status = "BATCH_SHELL_STDIN_INCOMPLETE";
                    }
                } else if (stdinFailure.get() != null && finished && "SUCCESS".equals(status)) {
                    status = "BATCH_SHELL_STDIN_INCOMPLETE";
                }
            }

            outReader.join(joinMillis);
            errReader.join(joinMillis);

            int exitCode = process.isAlive() ? -1 : process.exitValue();
            String sanitizedStdout = SensitiveTextSanitizer.sanitize(stdout.asText());
            String sanitizedStderr = SensitiveTextSanitizer.sanitize(stderr.asText());
            String combined = sanitizedStderr.isBlank()
                    ? sanitizedStdout
                    : sanitizedStdout + System.lineSeparator() + sanitizedStderr;
            return new Result(
                    "SUCCESS".equals(status),
                    exitCode,
                    combined,
                    status,
                    sanitizedStdout,
                    sanitizedStderr,
                    stdout.truncated() || stderr.truncated(),
                    Duration.between(startedAt, Instant.now()).toMillis(),
                    artifact.artifactHash(),
                    unknownResult);
        } finally {
            if (stdinJson != null) {
                Arrays.fill(stdinJson, (byte) 0);
            }
            secureDelete(parameterFile);
        }
    }

    private List<String> buildCommand(
            Path executable,
            WorkerOperationalProperties.ShellDefinition definition,
            Map<String, Object> parameters) {
        List<String> command = new ArrayList<>();
        if (definition.getInterpreter() != null && !definition.getInterpreter().isBlank()) {
            command.add(requireApprovedExecutable(definition.getInterpreter(), "interpreter").toString());
        }
        command.add(executable.toString());
        command.addAll(definition.getFixedArguments());

        if ("COMMAND_LINE".equalsIgnoreCase(definition.getParameterDeliveryMode())) {
            Set<String> sensitive = Set.copyOf(definition.getSensitiveParameters());
            for (String key : definition.getAllowedParameters()) {
                if (!parameters.containsKey(key)) {
                    continue;
                }
                if (sensitive.contains(key)) {
                    throw new SecurityException("Sensitive parameter cannot be delivered through command line: " + key);
                }
                command.add("--" + key);
                command.add(String.valueOf(parameters.get(key)));
            }
        }
        return List.copyOf(command);
    }

    private void configureWorkingDirectory(ProcessBuilder builder, WorkerOperationalProperties.ShellDefinition definition) {
        String alias = definition.getWorkingDirectoryAlias();
        if (alias == null || alias.isBlank()) {
            return;
        }
        WorkerOperationalProperties.PathAlias pathAlias = properties.getPathAliases().get(alias);
        if (pathAlias == null || pathAlias.getRoot() == null || pathAlias.getRoot().isBlank()) {
            throw new SecurityException("Working directory alias is not approved: " + alias);
        }
        Path root = Path.of(pathAlias.getRoot()).toAbsolutePath().normalize();
        if (!Files.isDirectory(root)) {
            throw new IllegalStateException("Approved working directory does not exist: " + alias);
        }
        builder.directory(root.toFile());
    }

    private void configureEnvironment(ProcessBuilder builder, WorkerOperationalProperties.ShellDefinition definition) {
        Map<String, String> original = new LinkedHashMap<>(builder.environment());
        builder.environment().clear();
        for (String key : definition.getAllowedEnvironmentVariables()) {
            String value = original.get(key);
            if (value != null) {
                builder.environment().put(key, value);
            }
        }
        builder.environment().put("CPF_SCRIPT_ID", blankTo(definition.getScriptId(), "UNKNOWN"));
        builder.environment().put("CPF_SCRIPT_VERSION", blankTo(definition.getVersion(), "1"));
    }

    private Path writeParameterFile(Map<String, Object> parameters) throws IOException {
        Path file = Files.createTempFile("cpf-batch-params-", ".properties");
        try {
            Files.setPosixFilePermissions(file, Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
        } catch (UnsupportedOperationException ignored) {
            file.toFile().setReadable(false, false);
            file.toFile().setWritable(false, false);
            file.toFile().setReadable(true, true);
            file.toFile().setWritable(true, true);
        }
        Properties serialized = new Properties();
        parameters.forEach((key, value) -> serialized.setProperty(key, value == null ? "" : String.valueOf(value)));
        try (OutputStream output = Files.newOutputStream(file)) {
            serialized.store(output, "CPF approved batch parameters");
        }
        return file;
    }

    private byte[] serializeParametersForStdin(Map<String, Object> parameters) throws IOException {
        byte[] serialized = objectMapper.writeValueAsBytes(parameters);
        if (serialized.length > MAX_STDIN_JSON_BYTES) {
            Arrays.fill(serialized, (byte) 0);
            throw new SecurityException("STDIN_JSON parameter payload exceeds the approved byte limit");
        }
        return serialized;
    }

    private static void writeParametersToStdin(Process process, byte[] serialized) throws IOException {
        try (OutputStream output = process.getOutputStream()) {
            output.write(serialized);
            output.write('\n');
            output.flush();
        }
    }

    private ScriptArtifactVerifier.VerificationResult verifyArtifact(
            Path executable,
            WorkerOperationalProperties.ShellDefinition definition) throws Exception {
        return artifactVerifiers.stream()
                .filter(verifier -> verifier.supports(definition))
                .findFirst()
                .orElseThrow(() -> new SecurityException("Signature verifier is not installed for approved script"))
                .verify(executable, definition);
    }

    private static void validateCatalogDefinition(String key, WorkerOperationalProperties.ShellDefinition definition) {
        if (definition.getTimeoutSeconds() <= 0
                || definition.getGracefulShutdownSeconds() <= 0
                || definition.getMaxOutputBytes() <= 0
                || definition.getMaxOutputLinesPerSecond() <= 0) {
            throw new IllegalArgumentException("Invalid approved script limits: " + key);
        }
        String deliveryMode = blankTo(definition.getParameterDeliveryMode(), "PARAMETER_FILE")
                .toUpperCase(Locale.ROOT);
        if (!Set.of("PARAMETER_FILE", "COMMAND_LINE", "STDIN_JSON").contains(deliveryMode)) {
            throw new IllegalArgumentException("Unsupported parameterDeliveryMode: " + key);
        }
        if (!definition.isTerminateProcessTree()) {
            throw new IllegalArgumentException("terminateProcessTree must be enabled: " + key);
        }
        if (!definition.getSensitiveParameters().isEmpty() && !"STDIN_JSON".equals(deliveryMode)) {
            throw new IllegalArgumentException("Sensitive parameters require STDIN_JSON delivery mode: " + key);
        }
        if (definition.getSensitiveParameters().stream().anyMatch(value -> !definition.getAllowedParameters().contains(value))) {
            throw new IllegalArgumentException("Sensitive parameter must be included in allowedParameters: " + key);
        }
        Set<Integer> overlappingExitCodes = new HashSet<>(definition.getSuccessExitCodes());
        overlappingExitCodes.retainAll(definition.getRetryableExitCodes());
        if (!overlappingExitCodes.isEmpty()) {
            throw new IllegalArgumentException("Exit code cannot be both success and retryable: " + key);
        }
        if (definition.getRunAsIdentity() != null && !definition.getRunAsIdentity().isBlank()) {
            String current = System.getProperty("user.name", "");
            if (!definition.getRunAsIdentity().equals(current)) {
                throw new SecurityException("Approved runAsIdentity does not match current worker identity");
            }
        }
    }

    private static Path requireApprovedExecutable(String configuredPath, String field) {
        Path path = Path.of(Objects.requireNonNull(configuredPath, field))
                .toAbsolutePath().normalize();
        if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) || Files.isSymbolicLink(path)) {
            throw new SecurityException("Approved " + field + " is missing or symbolic link");
        }
        return path;
    }

    private static void validateParameters(
            WorkerOperationalProperties.ShellDefinition definition,
            Map<String, Object> parameters) {
        Set<String> allowed = Set.copyOf(definition.getAllowedParameters());
        for (String key : parameters.keySet()) {
            if (key == null || !key.matches("[A-Za-z][A-Za-z0-9._-]{0,99}")) {
                throw new SecurityException("Invalid shell parameter name");
            }
            if (!allowed.contains(key)) {
                throw new SecurityException("Unapproved shell parameter: " + key);
            }
            Object value = parameters.get(key);
            if (value != null && String.valueOf(value).indexOf('\0') >= 0) {
                throw new SecurityException("NUL character is not allowed in shell parameter");
            }
        }
    }

    private static String classifyExit(WorkerOperationalProperties.ShellDefinition definition, int exitCode) {
        if (definition.getSuccessExitCodes().contains(exitCode)) {
            return "SUCCESS";
        }
        if (definition.getRetryableExitCodes().contains(exitCode)) {
            return "RETRYABLE_FAILURE";
        }
        return "BUSINESS_FAILURE";
    }

    private static boolean terminateProcessTree(Process process, WorkerOperationalProperties.ShellDefinition definition)
            throws InterruptedException {
        List<ProcessHandle> descendants = process.descendants().toList();
        process.destroy();
        if (definition.isTerminateProcessTree()) {
            descendants.forEach(ProcessHandle::destroy);
        }
        long grace = Math.max(1, definition.getGracefulShutdownSeconds());
        if (process.waitFor(grace, TimeUnit.SECONDS)) {
            return descendants.stream().noneMatch(ProcessHandle::isAlive);
        }
        if (definition.isTerminateProcessTree()) {
            descendants.stream().filter(ProcessHandle::isAlive).forEach(ProcessHandle::destroyForcibly);
        }
        process.destroyForcibly();
        return process.waitFor(grace, TimeUnit.SECONDS)
                && descendants.stream().noneMatch(ProcessHandle::isAlive);
    }

    private static void copyBounded(InputStream input, BoundedOutput output) {
        try (input) {
            byte[] buffer = new byte[8192];
            for (int read; (read = input.read(buffer)) >= 0;) {
                if (read > 0) {
                    output.write(buffer, 0, read);
                }
            }
        } catch (IOException ignored) {
            // Process 종료 중 Stream close는 정상적인 종료 경로일 수 있습니다.
        }
    }

    private static void secureDelete(Path path) {
        if (path == null) {
            return;
        }
        try {
            if (Files.exists(path)) {
                long size = Math.min(Files.size(path), 1_048_576);
                if (size > 0) {
                    try (OutputStream output = Files.newOutputStream(path)) {
                        byte[] zeros = new byte[8192];
                        for (long written = 0; written < size; written += zeros.length) {
                            output.write(zeros, 0, (int) Math.min(zeros.length, size - written));
                        }
                    }
                }
                Files.deleteIfExists(path);
            }
        } catch (IOException ignored) {
            path.toFile().deleteOnExit();
        }
    }

    private static String blankTo(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static final class BoundedOutput extends ByteArrayOutputStream {
        private final long maxBytes;
        private final int maxLinesPerSecond;
        private long rateWindowStartedAtNanos = System.nanoTime();
        private int linesInWindow;
        private boolean truncated;

        private BoundedOutput(long maxBytes, int maxLinesPerSecond) {
            this.maxBytes = Math.max(1, maxBytes);
            this.maxLinesPerSecond = Math.max(1, maxLinesPerSecond);
        }

        @Override
        public synchronized void write(byte[] bytes, int offset, int length) {
            Objects.checkFromIndexSize(offset, length, bytes.length);
            for (int index = offset; index < offset + length; index++) {
                if (size() >= maxBytes) {
                    truncated = true;
                    break;
                }
                long now = System.nanoTime();
                if (now - rateWindowStartedAtNanos >= TimeUnit.SECONDS.toNanos(1)) {
                    rateWindowStartedAtNanos = now;
                    linesInWindow = 0;
                }
                if (bytes[index] == '\n' && ++linesInWindow > maxLinesPerSecond) {
                    truncated = true;
                    break;
                }
                super.write(bytes[index]);
            }
        }

        private synchronized String asText() {
            return toString(StandardCharsets.UTF_8);
        }

        private synchronized boolean truncated() {
            return truncated;
        }
    }

    public record Result(
            boolean success,
            int exitCode,
            String output,
            String status,
            String stdout,
            String stderr,
            boolean truncated,
            long durationMs,
            String artifactHash,
            boolean unknownResult) {

        /** 기존 Test/Consumer의 3-인자 생성 Source Compatibility를 보존합니다. */
        public Result(boolean success, int exitCode, String output) {
            this(success, exitCode, output, success ? "SUCCESS" : "BUSINESS_FAILURE",
                    output, "", false, 0L, "", false);
        }
    }
}

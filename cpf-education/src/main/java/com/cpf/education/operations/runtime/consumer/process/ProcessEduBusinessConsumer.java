package com.cpf.education.operations.runtime.consumer.process;

import com.cpf.education.operations.runtime.application.EduPayloadHasher;
import com.cpf.education.operations.runtime.application.EduValidationException;
import com.cpf.education.operations.runtime.consumer.*;
import com.cpf.education.operations.runtime.model.EduExecutionCommand;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** Allowlisted repository script consumer. It never invokes a shell or concatenates user input. */
/** ProcessEduBusinessConsumer 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public final class ProcessEduBusinessConsumer implements EduBusinessConsumer {
    static final int DEFAULT_MAX_OUTPUT_BYTES = 64 * 1024;
    private static final int MAX_CONFIGURED_OUTPUT_BYTES = 1024 * 1024;
    private static final int OUTPUT_DRAIN_TIMEOUT_SECONDS = 5;

    private final Path repositoryRoot;
    private final ObjectMapper json;
    private final int maxOutputBytes;

    /** ProcessEduBusinessConsumer 작업을 CPF 표준 계약에 따라 수행한다. */
    public ProcessEduBusinessConsumer(Path repositoryRoot, ObjectMapper json) {
        this(repositoryRoot, json, DEFAULT_MAX_OUTPUT_BYTES);
    }

    ProcessEduBusinessConsumer(Path repositoryRoot, ObjectMapper json, int maxOutputBytes) {
        this.repositoryRoot = Objects.requireNonNull(repositoryRoot).toAbsolutePath().normalize();
        this.json = Objects.requireNonNull(json);
        if (maxOutputBytes < 1 || maxOutputBytes > MAX_CONFIGURED_OUTPUT_BYTES) {
            throw new IllegalArgumentException(
                    "maxOutputBytes must be between 1 and " + MAX_CONFIGURED_OUTPUT_BYTES);
        }
        this.maxOutputBytes = maxOutputBytes;
    }

    @Override
    public EduConsumerType type() {
        return EduConsumerType.PROCESS;
    }

    @Override
    public EduBusinessConsumerResult invoke(EduConsumerBinding binding,
                                            EduExecutionCommand command,
                                            long fencingToken) {
        Process process = null;
        Thread outputReader = null;
        try {
            Path script = repositoryRoot.resolve(binding.entryPoint()).normalize();
            if (!script.startsWith(repositoryRoot) || !Files.isRegularFile(script)) {
                throw new EduValidationException("allowlisted script missing: " + binding.entryPoint());
            }

            List<String> executable = commandFor(script);
            Map<String, String> environment = new LinkedHashMap<>();
            environment.put("CPF_EDU_REQUIREMENT_ID", binding.requirementId());
            environment.put("CPF_EDU_BUSINESS_KEY", command.businessKey());
            environment.put("CPF_EDU_DATA_SCOPE", command.dataScope());
            environment.put("CPF_EDU_TRACE_ID", command.traceId());
            environment.put("CPF_EDU_FENCING_TOKEN", Long.toString(fencingToken));

            ProcessBuilder builder = new ProcessBuilder(executable)
                    .directory(repositoryRoot.toFile())
                    .redirectErrorStream(true);
            Map<String,String> childEnvironment = builder.environment();
            Map<String,String> parentEnvironment = new HashMap<>(childEnvironment);
            childEnvironment.clear();
            copyAllowlistedHostEnvironment(parentEnvironment, childEnvironment);
            childEnvironment.putAll(environment);
            process = builder.start();
            try (OutputStream stdin = process.getOutputStream()) {
                json.writeValue(stdin, command.payload());
            }

            Process startedProcess = process;
            CompletableFuture<String> outputFuture = new CompletableFuture<>();
            outputReader = Thread.ofVirtual().name("cpf-edu-process-output").start(() -> {
                try {
                    outputFuture.complete(readBounded(startedProcess.getInputStream(), maxOutputBytes));
                // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
                } catch (Exception failure) {
                    outputFuture.completeExceptionally(failure);
                }
            });
            outputFuture.whenComplete((ignored, failure) -> {
                if (failure != null && startedProcess.isAlive()) {
                    startedProcess.destroyForcibly();
                }
            });

            String output = awaitProcessAndOutput(
                    startedProcess, outputFuture, binding.timeoutSeconds());
            if (process.exitValue() != 0) {
                throw new IllegalStateException("script exit=" + process.exitValue() + " output=" + sanitize(output));
            }
            return EduBusinessConsumerResult.completed("PROCESS_OK", Map.of(
                    "script", binding.entryPoint(),
                    "exitCode", process.exitValue(),
                    "outputDigest", EduPayloadHasher.sha256(output)));
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        } catch (EduValidationException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("process consumer interrupted", e);
        } catch (Exception e) {
            throw new IllegalStateException("process consumer failed: " + e.getMessage(), e);
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
            if (outputReader != null && outputReader.isAlive()) {
                outputReader.interrupt();
            }
        }
    }


    private static void copyAllowlistedHostEnvironment(Map<String,String> source, Map<String,String> target) {
        for (String key : List.of("PATH", "Path", "SystemRoot", "WINDIR", "TEMP", "TMP", "HOME", "LANG", "LC_ALL")) {
            String value = source.get(key);
            if (value != null && !value.isBlank()) target.put(key, value);
        }
    }

    private String awaitProcessAndOutput(Process process,
                                         CompletableFuture<String> outputFuture,
                                         int timeoutSeconds) throws Exception {
        long timeoutNanos = TimeUnit.SECONDS.toNanos(timeoutSeconds);
        long deadline = System.nanoTime() + timeoutNanos;
        try {
            CompletableFuture.anyOf(process.onExit(), outputFuture)
                    .get(timeoutNanos, TimeUnit.NANOSECONDS);
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        } catch (ExecutionException failure) {
            destroyAndAwait(process);
            throw outputFailure(failure.getCause());
        } catch (TimeoutException failure) {
            destroyAndAwait(process);
            throw new IllegalStateException("script timeout after " + timeoutSeconds + "s", failure);
        }

        if (outputFuture.isCompletedExceptionally()) {
            return awaitOutput(outputFuture);
        }

        long remaining = deadline - System.nanoTime();
        if (process.isAlive()
                && (remaining <= 0 || !process.waitFor(remaining, TimeUnit.NANOSECONDS))) {
            destroyAndAwait(process);
            throw new IllegalStateException("script timeout after " + timeoutSeconds + "s");
        }
        return awaitOutput(outputFuture);
    }

    private static String awaitOutput(CompletableFuture<String> outputFuture) throws Exception {
        try {
            return outputFuture.get(OUTPUT_DRAIN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        } catch (ExecutionException failure) {
            throw outputFailure(failure.getCause());
        } catch (TimeoutException failure) {
            throw new IllegalStateException("script output drain timeout", failure);
        }
    }

    private static Exception outputFailure(Throwable failure) {
        if (failure instanceof Exception exception) {
            return exception;
        }
        return new IllegalStateException("script output reader failed", failure);
    }

    private static void destroyAndAwait(Process process) throws InterruptedException {
        if (process.isAlive()) {
            process.destroyForcibly();
            process.waitFor(OUTPUT_DRAIN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }
    }

    private static String readBounded(InputStream input, int maxOutputBytes) throws IOException {
        try (InputStream source = input;
             ByteArrayOutputStream output = new ByteArrayOutputStream(Math.min(maxOutputBytes, 8192))) {
            byte[] buffer = new byte[8192];
            int total = 0;
            int read;
            while ((read = source.read(buffer)) != -1) {
                if (read == 0) {
                    continue;
                }
                if (total > maxOutputBytes - read) {
                    throw new IOException("process output exceeds " + maxOutputBytes + " bytes");
                }
                output.write(buffer, 0, read);
                total += read;
            }
            return output.toString(StandardCharsets.UTF_8);
        }
    }

    private static List<String> commandFor(Path script) {
        String name = script.getFileName().toString().toLowerCase(Locale.ROOT);
        if (name.endsWith(".ps1")) {
            return List.of("pwsh", "-NoProfile", "-NonInteractive", "-File", script.toString());
        }
        if (name.endsWith(".py")) {
            return List.of("python", script.toString());
        }
        if (name.endsWith(".sh")) {
            return List.of("bash", script.toString());
        }
        throw new EduValidationException("unsupported script type: " + name);
    }

    private static String sanitize(String text) {
        String value = text.replaceAll(
                "(?i)(password|secret|token|api[_-]?key)\\s*[:=]\\s*\\S+",
                "$1=***");
        return value.length() > 500 ? value.substring(0, 500) : value;
    }
}

package com.cpf.batch.control;

import com.cpf.batch.api.BatchApprovedLaunchRequest;
import com.cpf.batch.api.BatchCanonicalDigest;
import com.cpf.batch.api.BatchExecutionControlPort;
import com.cpf.batch.api.BatchExecutionLink;
import com.cpf.batch.api.BatchExecutionPlan;
import com.cpf.batch.api.BatchExecutionTopology;
import com.cpf.batch.api.BatchJobDefinition;
import com.cpf.batch.api.BatchStepDefinition;
import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Local-only diagnostic entry point for the optional Batch Kafka remote transport. */
@RestController
@RequestMapping("/internal/v1/batch/remote-diagnostic")
@ConditionalOnProperty(name = "cpf.batch.diagnostic.enabled", havingValue = "true")
final class BatchRemoteDiagnosticController {
    static final String TOKEN_HEADER = "X-CPF-Batch-Diagnostic-Token";
    static final String JOB_ID = "BAT.DIAGNOSTIC.REMOTE";
    static final String EXECUTOR_REFERENCE = "SERVICE:CPF_BAT_DIAGNOSTIC";
    private final BatchExecutionControlPort executions;
    private final byte[] expectedToken;

    BatchRemoteDiagnosticController(
            BatchExecutionControlPort executions,
            @Value("${cpf.batch.diagnostic.token:${CPF_BAT_DIAGNOSTIC_TOKEN:}}") String token) {
        this.executions = executions;
        if (token == null || token.isBlank() || token.length() < 32) {
            throw new IllegalStateException("CPF_BAT_DIAGNOSTIC_TOKEN must contain at least 32 characters");
        }
        this.expectedToken = token.getBytes(StandardCharsets.UTF_8);
    }

    @PostMapping("/executions")
    ResponseEntity<BatchExecutionLink> start(
            @RequestHeader(TOKEN_HEADER) String token,
            @RequestBody Command command,
            HttpServletRequest request) {
        requireLoopback(request);
        requireToken(token);
        if (command == null || command.idempotencyKey() == null
                || !command.idempotencyKey().matches("[A-Za-z0-9._:-]{8,200}")) {
            throw new IllegalArgumentException("idempotencyKey format invalid");
        }
        if (command.fencingToken() <= 0) {
            throw new IllegalArgumentException("fencingToken must be positive");
        }
        int partitions = bounded(command.partitions(), 2, 64, "partitions");
        long sleepMs = bounded(command.sleepMs(), 0L, 60_000L, "sleepMs");
        return ResponseEntity.accepted().body(executions.start(approved(
                command.idempotencyKey(), command.fencingToken(), partitions, sleepMs)));
    }

    private BatchApprovedLaunchRequest approved(
            String idempotencyKey, long fencingToken, int partitions, long sleepMs) {
        BatchStepDefinition step = new BatchStepDefinition(
                "diagnostic-remote-step",
                BatchJobDefinition.ExecutorType.SERVICE_CALL,
                EXECUTOR_REFERENCE,
                Map.of("sleepMs", Long.toString(sleepMs)),
                partitions,
                "",
                "",
                true);
        List<BatchStepDefinition> steps = List.of(step);
        String planChecksum = BatchCanonicalDigest.planHash(
                JOB_ID, 1L, BatchExecutionTopology.REMOTE_PARTITION, steps);
        BatchExecutionPlan plan = new BatchExecutionPlan(
                JOB_ID, 1L, BatchExecutionTopology.REMOTE_PARTITION, steps, planChecksum);
        String definitionChecksum = BatchCanonicalDigest.sha256(Map.of(
                "jobId", JOB_ID,
                "version", 1L,
                "executorReference", EXECUTOR_REFERENCE,
                "planChecksum", planChecksum));
        BatchJobDefinition definition = new BatchJobDefinition(
                JOB_ID,
                1L,
                "CPF remote worker diagnostic",
                BatchJobDefinition.ExecutorType.SERVICE_CALL,
                BatchJobDefinition.State.PUBLISHED,
                "BAT",
                "Local-only distributed Runtime verification",
                new BatchJobDefinition.Trigger(
                        BatchJobDefinition.TriggerType.MANUAL,
                        "",
                        "Asia/Seoul",
                        BatchJobDefinition.MisfirePolicy.FAIL_CLOSED,
                        true),
                List.of(),
                List.of(),
                new BatchJobDefinition.ResourcePolicy("DIAGNOSTIC", "local", partitions, 120L, 0L, 0),
                new BatchJobDefinition.RecoveryPolicy(
                        1, 0L, 1.0, 0L, 0, true,
                        BatchJobDefinition.UnknownResultPolicy.RECONCILE, ""),
                BatchJobDefinition.AlertPolicy.defaults(),
                EXECUTOR_REFERENCE,
                definitionChecksum,
                "CPF_RUNTIME_VERIFIER",
                "Approved local distributed runtime verification",
                OffsetDateTime.parse("2000-01-01T00:00:00+09:00"),
                null,
                1L);
        return new BatchApprovedLaunchRequest(
                definition,
                plan,
                Map.of("verification", "physical-runtime"),
                "CPF-LOCAL-DIAGNOSTIC",
                "CPF_RUNTIME_VERIFIER",
                "Local distributed runtime verification",
                idempotencyKey,
                fencingToken);
    }

    private void requireToken(String supplied) {
        byte[] actual = supplied == null ? new byte[0] : supplied.getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(expectedToken, actual)) {
            throw new SecurityException("BATCH_DIAGNOSTIC_TOKEN_INVALID");
        }
    }

    private static void requireLoopback(HttpServletRequest request) {
        try {
            String remote = request == null ? null : request.getRemoteAddr();
            if (remote == null || remote.isBlank() || !InetAddress.getByName(remote).isLoopbackAddress()) {
                throw new SecurityException("BATCH_DIAGNOSTIC_LOOPBACK_REQUIRED");
            }
        } catch (SecurityException failure) {
            throw failure;
        } catch (Exception failure) {
            throw new SecurityException("BATCH_DIAGNOSTIC_LOOPBACK_REQUIRED", failure);
        }
    }

    private static int bounded(int value, int minimum, int maximum, String name) {
        if (value < minimum || value > maximum) {
            throw new IllegalArgumentException(name + " must be between " + minimum + " and " + maximum);
        }
        return value;
    }

    private static long bounded(long value, long minimum, long maximum, String name) {
        if (value < minimum || value > maximum) {
            throw new IllegalArgumentException(name + " must be between " + minimum + " and " + maximum);
        }
        return value;
    }

    record Command(String idempotencyKey, long fencingToken, int partitions, long sleepMs) { }
}

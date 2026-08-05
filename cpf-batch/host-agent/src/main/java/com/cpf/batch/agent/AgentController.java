package com.cpf.batch.agent;

import com.cpf.batch.agent.internal.ArtifactInstaller;
import com.cpf.batch.agent.internal.LogArchiveService;
import com.cpf.batch.agent.internal.RuntimeControlProxy;
import com.cpf.batch.agent.internal.ServiceManager;
import com.cpf.batch.api.AgentArtifactRequest;
import com.cpf.batch.api.AgentCommandResult;
import com.cpf.batch.api.CommandState;
import com.cpf.batch.runtime.BatchRuntimePolicy;
import com.cpf.batch.runtime.SensitiveTextSanitizer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/** 승인된 Host Agent 명령만 실행하며 모든 변경 명령을 영속 Command Ledger로 멱등 처리합니다. */
@RestController
@RequestMapping("/api/v1/agent")
@Tag(name = "BAT Host Agent", description = "승인된 BAT Runtime 설치 및 운영 명령")
public class AgentController {
    private static final String COMMAND_ID = "X-CPF-Command-ID";
    private static final String IDEMPOTENCY_KEY = "Idempotency-Key";

    private final ApprovedCommandCatalog catalog;
    private final ArtifactInstaller installer;
    private final ServiceManager manager;
    private final RuntimeControlProxy runtime;
    private final LogArchiveService logs;
    private final AgentCommandLedger ledger;
    private final ObjectMapper canonicalJson;
    private volatile BatchRuntimePolicy runtimePolicy = new BatchRuntimePolicy();

    public AgentController(
            ApprovedCommandCatalog catalog,
            ArtifactInstaller installer,
            ServiceManager manager,
            RuntimeControlProxy runtime,
            LogArchiveService logs,
            AgentCommandLedger ledger,
            ObjectMapper objectMapper) {
        this.catalog = catalog;
        this.installer = installer;
        this.manager = manager;
        this.runtime = runtime;
        this.logs = logs;
        this.ledger = ledger;
        ObjectMapper canonicalMapper = objectMapper.copy();
        canonicalMapper.setConfig(canonicalMapper.getSerializationConfig()
                .with(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY));
        canonicalMapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
        this.canonicalJson = canonicalMapper;
    }

    @Autowired
    public void setRuntimePolicy(BatchRuntimePolicy runtimePolicy) {
        this.runtimePolicy = Objects.requireNonNull(runtimePolicy, "runtimePolicy");
    }

    @PostMapping("/artifacts/install")
    @Operation(summary = "검증된 Runtime Artifact 설치")
    AgentCommandResult install(
            @RequestHeader(COMMAND_ID) String commandId,
            @RequestHeader(IDEMPOTENCY_KEY) String idempotencyKey,
            @RequestBody AgentArtifactRequest request) {
        String fingerprint = fingerprint("INSTALL_ARTIFACT", request.serviceId(), request);
        return execute(commandId, idempotencyKey, fingerprint, request.serviceId(), "INSTALL_ARTIFACT",
                (id, startedAt) -> {
                    AgentCommandResult rejected = preflight(
                            id, request.serviceId(), "INSTALL_ARTIFACT", startedAt, "INSTALL_ARTIFACT");
                    if (rejected != null) {
                        return rejected;
                    }
                    try {
                        var installed = installer.install(request);
                        return result(id, request.serviceId(), "INSTALL_ARTIFACT", CommandState.SUCCEEDED,
                                "INSTALLED", "Artifact installed", installed.version(), startedAt);
                    } catch (SecurityException rejectedRequest) {
                        return result(id, request.serviceId(), "INSTALL_ARTIFACT", CommandState.FAILED,
                                "SECURITY_REJECTED", "Artifact request rejected", null, startedAt);
                    } catch (Exception failure) {
                        return result(id, request.serviceId(), "INSTALL_ARTIFACT", CommandState.UNKNOWN_RESULT,
                                "INSTALL_RESULT_UNKNOWN", safe(failure), null, startedAt);
                    }
                });
    }

    @PostMapping("/services/{id}/start")
    @Operation(summary = "승인된 Runtime 기동")
    AgentCommandResult start(@PathVariable String id, @RequestHeader(COMMAND_ID) String commandId,
            @RequestHeader(IDEMPOTENCY_KEY) String idempotencyKey) {
        return serviceCommand(id, "START", ServiceManager.Action.START, commandId, idempotencyKey);
    }

    @PostMapping("/services/{id}/stop")
    @Operation(summary = "승인된 Runtime 중지")
    AgentCommandResult stop(@PathVariable String id, @RequestHeader(COMMAND_ID) String commandId,
            @RequestHeader(IDEMPOTENCY_KEY) String idempotencyKey) {
        return serviceCommand(id, "STOP", ServiceManager.Action.STOP, commandId, idempotencyKey);
    }

    @PostMapping("/services/{id}/restart")
    @Operation(summary = "승인된 Runtime 재기동")
    AgentCommandResult restart(@PathVariable String id, @RequestHeader(COMMAND_ID) String commandId,
            @RequestHeader(IDEMPOTENCY_KEY) String idempotencyKey) {
        return serviceCommand(id, "RESTART", ServiceManager.Action.RESTART, commandId, idempotencyKey);
    }

    @PostMapping("/services/{id}/status")
    @Operation(summary = "Runtime 프로세스 상태 확인")
    AgentCommandResult status(@PathVariable String id, @RequestHeader(COMMAND_ID) String commandId,
            @RequestHeader(IDEMPOTENCY_KEY) String idempotencyKey) {
        return serviceCommand(id, "STATUS", ServiceManager.Action.STATUS, commandId, idempotencyKey);
    }

    @PostMapping("/services/{id}/rollback")
    @Operation(summary = "검증된 이전 Runtime Artifact로 Rollback")
    AgentCommandResult rollback(@PathVariable String id, @RequestHeader(COMMAND_ID) String commandId,
            @RequestHeader(IDEMPOTENCY_KEY) String idempotencyKey) {
        String fingerprint = fingerprint("ROLLBACK", id, null);
        return execute(commandId, idempotencyKey, fingerprint, id, "ROLLBACK", (stableId, startedAt) -> {
            AgentCommandResult rejected = preflight(stableId, id, "ROLLBACK", startedAt, "ROLLBACK");
            if (rejected != null) {
                return rejected;
            }
            RollbackPhase phase = RollbackPhase.NOT_STARTED;
            String version = null;
            try {
                var stopResult = manager.execute(id, ServiceManager.Action.STOP);
                if (stopResult.unknownResult()) {
                    return result(stableId, id, "ROLLBACK", CommandState.UNKNOWN_RESULT,
                            "STOP_RESULT_UNKNOWN", sanitize(stopResult.output()), null, startedAt);
                }
                if (!stopResult.success() || !manager.stopped(id)) {
                    return result(stableId, id, "ROLLBACK", CommandState.UNKNOWN_RESULT,
                            "STOP_NOT_CONFIRMED", "Runtime stop result is not confirmed", null, startedAt);
                }
                phase = RollbackPhase.STOP_CONFIRMED;
                version = installer.rollback(id);
                phase = RollbackPhase.ARTIFACT_SWAPPED;
                var startResult = manager.execute(id, ServiceManager.Action.START);
                if (startResult.unknownResult()) {
                    return result(stableId, id, "ROLLBACK", CommandState.PARTIALLY_ROLLED_BACK,
                            "ROLLBACK_START_RESULT_UNKNOWN", sanitize(startResult.output()), version, startedAt);
                }
                return result(stableId, id, "ROLLBACK",
                        startResult.success() ? CommandState.ROLLED_BACK : CommandState.PARTIALLY_ROLLED_BACK,
                        startResult.success() ? "ROLLED_BACK" : "ROLLBACK_START_FAILED",
                        sanitize(startResult.output()), version, startedAt);
            } catch (Exception failure) {
                if (phase == RollbackPhase.ARTIFACT_SWAPPED) {
                    return result(stableId, id, "ROLLBACK", CommandState.PARTIALLY_ROLLED_BACK,
                            "ROLLBACK_RESTART_RESULT_UNKNOWN", safe(failure), version, startedAt);
                }
                return result(stableId, id, "ROLLBACK", CommandState.UNKNOWN_RESULT,
                        phase == RollbackPhase.STOP_CONFIRMED
                                ? "ROLLBACK_ARTIFACT_RESULT_UNKNOWN"
                                : "ROLLBACK_STOP_RESULT_UNKNOWN",
                        safe(failure), version, startedAt);
            }
        });
    }

    @PostMapping("/services/{id}/drain")
    @Operation(summary = "Runtime Drain 전환")
    AgentCommandResult drain(@PathVariable String id, @RequestHeader(COMMAND_ID) String commandId,
            @RequestHeader(IDEMPOTENCY_KEY) String idempotencyKey) {
        return runtimeCommand(id, "DRAIN", "drain", commandId, idempotencyKey);
    }

    @PostMapping("/services/{id}/resume")
    @Operation(summary = "Runtime Resume 전환")
    AgentCommandResult resume(@PathVariable String id, @RequestHeader(COMMAND_ID) String commandId,
            @RequestHeader(IDEMPOTENCY_KEY) String idempotencyKey) {
        return runtimeCommand(id, "RESUME", "resume", commandId, idempotencyKey);
    }

    @GetMapping("/commands/{commandId}")
    @Operation(summary = "멱등 명령 결과 재조회")
    ResponseEntity<AgentCommandResult> command(@PathVariable String commandId) {
        return ledger.find(commandId).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/services/{id}/logs")
    @Operation(summary = "제한된 Runtime 로그 묶음 수집")
    ResponseEntity<StreamingResponseBody> logs(@PathVariable String id) throws Exception {
        requireLogCollectionPolicy();
        catalog.requireAllowed("COLLECT_LOGS");
        Path archive = logs.collect(id);
        StreamingResponseBody body = output -> {
            try (var input = java.nio.file.Files.newInputStream(archive, java.nio.file.LinkOption.NOFOLLOW_LINKS)) {
                input.transferTo(output);
                output.flush();
            } finally {
                logs.delete(archive);
            }
        };
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + archive.getFileName() + "\"")
                .body(body);
    }

    private AgentCommandResult runtimeCommand(
            String serviceId, String command, String operation, String commandId, String idempotencyKey) {
        String fingerprint = fingerprint(command, serviceId, null);
        return execute(commandId, idempotencyKey, fingerprint, serviceId, command, (stableId, startedAt) -> {
            AgentCommandResult rejected = preflight(stableId, serviceId, command, startedAt, command);
            if (rejected != null) {
                return rejected;
            }
            try {
                runtime.invoke(serviceId, operation);
                return result(stableId, serviceId, command, CommandState.SUCCEEDED,
                        command + "_ACCEPTED", "Runtime command accepted", null, startedAt);
            } catch (Exception failure) {
                return result(stableId, serviceId, command, CommandState.UNKNOWN_RESULT,
                        command + "_RESPONSE_UNKNOWN", safe(failure), null, startedAt);
            }
        });
    }

    private AgentCommandResult serviceCommand(
            String serviceId, String command, ServiceManager.Action action, String commandId, String idempotencyKey) {
        String fingerprint = fingerprint(command, serviceId, null);
        return execute(commandId, idempotencyKey, fingerprint, serviceId, command, (stableId, startedAt) -> {
            AgentCommandResult rejected = preflight(stableId, serviceId, command, startedAt, command);
            if (rejected != null) {
                return rejected;
            }
            try {
                var commandResult = manager.execute(serviceId, action);
                if (commandResult.unknownResult()) {
                    return result(stableId, serviceId, command, CommandState.UNKNOWN_RESULT,
                            "SERVICE_COMMAND_RESULT_UNKNOWN", sanitize(commandResult.output()), null, startedAt);
                }
                return result(stableId, serviceId, command,
                        commandResult.success() ? CommandState.SUCCEEDED : CommandState.FAILED,
                        commandResult.success() ? "OK" : "SERVICE_COMMAND_FAILED",
                        sanitize(commandResult.output()), null, startedAt);
            } catch (Exception failure) {
                return result(stableId, serviceId, command, CommandState.UNKNOWN_RESULT,
                        "SERVICE_COMMAND_RESULT_UNKNOWN", safe(failure), null, startedAt);
            }
        });
    }

    private AgentCommandResult execute(
            String commandId,
            String idempotencyKey,
            String fingerprint,
            String serviceId,
            String command,
            AgentCommandLedger.CommandAction action) {
        if (!Objects.equals(commandId, idempotencyKey)) {
            return result(commandId, serviceId, command, CommandState.FAILED,
                    "IDEMPOTENCY_HEADER_MISMATCH", "Command id and idempotency key must match", null, Instant.now());
        }
        try {
            return ledger.execute(commandId, fingerprint, serviceId, command, action);
        } catch (IllegalArgumentException invalid) {
            return result(commandId, serviceId, command, CommandState.FAILED,
                    "COMMAND_ID_INVALID", "Command id is invalid", null, Instant.now());
        } catch (SecurityException conflict) {
            return result(commandId, serviceId, command, CommandState.FAILED,
                    "IDEMPOTENCY_CONFLICT", "Idempotency key conflict", null, Instant.now());
        }
    }

    private AgentCommandResult preflight(
            String commandId,
            String serviceId,
            String command,
            Instant startedAt,
            String catalogCommand) {
        try {
            requireCommandPolicy();
            catalog.requireAllowed(catalogCommand);
            return null;
        } catch (Exception rejected) {
            return result(commandId, serviceId, command, CommandState.FAILED,
                    "COMMAND_REJECTED", safe(rejected), null, startedAt);
        }
    }

    private String fingerprint(String command, String serviceId, Object body) {
        try {
            byte[] payload = body == null ? new byte[0] : canonicalJson.writeValueAsBytes(body);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(command.getBytes(StandardCharsets.UTF_8));
            digest.update((byte) 0);
            digest.update(serviceId.getBytes(StandardCharsets.UTF_8));
            digest.update((byte) 0);
            digest.update(payload);
            return HexFormat.of().formatHex(digest.digest());
        } catch (Exception failure) {
            throw new IllegalStateException("Unable to fingerprint Agent command", failure);
        }
    }

    private void requireCommandPolicy() {
        if (!runtimePolicy.current().agentCommandsEnabled()) {
            throw new SecurityException("Host Agent command plane is disabled by runtime policy");
        }
    }

    private void requireLogCollectionPolicy() {
        if (!runtimePolicy.current().agentLogCollectionEnabled()) {
            throw new SecurityException("Host Agent log collection is disabled by runtime policy");
        }
    }

    private static AgentCommandResult result(
            String commandId,
            String serviceId,
            String command,
            CommandState state,
            String code,
            String message,
            String version,
            Instant startedAt) {
        Instant start = startedAt == null ? Instant.now() : startedAt;
        return new AgentCommandResult(commandId, serviceId, command, state, code,
                sanitize(message), version, start, Instant.now());
    }

    private static String safe(Exception failure) {
        return sanitize(SensitiveTextSanitizer.sanitize(failure.getMessage()));
    }

    private static String sanitize(String message) {
        String safe = SensitiveTextSanitizer.sanitize(message == null ? "" : message);
        return safe.length() > 512 ? safe.substring(0, 512) : safe;
    }

    private enum RollbackPhase {
        NOT_STARTED,
        STOP_CONFIRMED,
        ARTIFACT_SWAPPED
    }
}

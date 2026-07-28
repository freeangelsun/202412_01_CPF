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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * 승인된 Host Agent 명령만 실행하는 제한된 운영 API입니다.
 *
 * <p>임의 Shell/Path를 받지 않고 Service Catalog와 Command Catalog에서 허용된
 * 설치·기동·중지·복구·로그 수집 작업만 수행합니다.</p>
 */
@RestController
@RequestMapping("/api/v1/agent")
@Tag(name = "BAT Host Agent", description = "승인된 BAT Runtime 설치 및 운영 명령")
public class AgentController {
    private final ApprovedCommandCatalog catalog;
    private final ArtifactInstaller installer;
    private final ServiceManager manager;
    private final RuntimeControlProxy runtime;
    private final LogArchiveService logs;
    private volatile BatchRuntimePolicy runtimePolicy = new BatchRuntimePolicy();

    public AgentController(
            ApprovedCommandCatalog catalog,
            ArtifactInstaller installer,
            ServiceManager manager,
            RuntimeControlProxy runtime,
            LogArchiveService logs) {
        this.catalog = catalog;
        this.installer = installer;
        this.manager = manager;
        this.runtime = runtime;
        this.logs = logs;
    }

    /** Runtime Control에서 Agent command plane을 즉시 중지·재개할 수 있도록 실제 API gate에 연결합니다. */
    @Autowired
    public void setRuntimePolicy(BatchRuntimePolicy runtimePolicy) {
        this.runtimePolicy = Objects.requireNonNull(runtimePolicy, "runtimePolicy");
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

    @PostMapping("/artifacts/install")
    @Operation(summary = "검증된 Runtime Artifact 설치")
    AgentCommandResult install(@RequestBody AgentArtifactRequest request) {
        String commandId = UUID.randomUUID().toString();
        Instant startedAt = Instant.now();
        try {
            requireCommandPolicy();
            catalog.requireAllowed("INSTALL_ARTIFACT");
            var installed = installer.install(request);
            return result(commandId, request.serviceId(), "INSTALL_ARTIFACT",
                    CommandState.SUCCEEDED, "INSTALLED", "installed", installed.version(), startedAt);
        } catch (SecurityException exception) {
            return result(commandId, request.serviceId(), "INSTALL_ARTIFACT",
                    CommandState.FAILED, "SECURITY_REJECTED", "request rejected", null, startedAt);
        } catch (Exception exception) {
            return result(commandId, request.serviceId(), "INSTALL_ARTIFACT",
                    CommandState.FAILED, "INSTALL_FAILED",
                    SensitiveTextSanitizer.sanitize(exception.getMessage()), null, startedAt);
        }
    }

    @PostMapping("/services/{id}/start")
    @Operation(summary = "승인된 Runtime 기동")
    AgentCommandResult start(@PathVariable String id) {
        return serviceCommand(id, "START", ServiceManager.Action.START);
    }

    @PostMapping("/services/{id}/stop")
    @Operation(summary = "승인된 Runtime 중지")
    AgentCommandResult stop(@PathVariable String id) {
        return serviceCommand(id, "STOP", ServiceManager.Action.STOP);
    }

    @PostMapping("/services/{id}/restart")
    @Operation(summary = "승인된 Runtime 재기동")
    AgentCommandResult restart(@PathVariable String id) {
        return serviceCommand(id, "RESTART", ServiceManager.Action.RESTART);
    }

    @PostMapping("/services/{id}/status")
    @Operation(summary = "Runtime 프로세스 상태 확인")
    AgentCommandResult status(@PathVariable String id) {
        return serviceCommand(id, "STATUS", ServiceManager.Action.STATUS);
    }

    @PostMapping("/services/{id}/rollback")
    @Operation(summary = "검증된 이전 Runtime Artifact로 Rollback")
    AgentCommandResult rollback(@PathVariable String id) {
        String commandId = UUID.randomUUID().toString();
        Instant startedAt = Instant.now();
        try {
            requireCommandPolicy();
            catalog.requireAllowed("ROLLBACK");
            manager.execute(id, ServiceManager.Action.STOP);
            String version = installer.rollback(id);
            var startResult = manager.execute(id, ServiceManager.Action.START);
            return result(commandId, id, "ROLLBACK",
                    startResult.success() ? CommandState.SUCCEEDED : CommandState.FAILED,
                    startResult.success() ? "ROLLED_BACK" : "ROLLBACK_START_FAILED",
                    startResult.output(), version, startedAt);
        } catch (Exception exception) {
            return result(commandId, id, "ROLLBACK", CommandState.FAILED,
                    "ROLLBACK_FAILED", SensitiveTextSanitizer.sanitize(exception.getMessage()),
                    null, startedAt);
        }
    }

    @PostMapping("/services/{id}/drain")
    @Operation(summary = "Runtime Drain 전환")
    AgentCommandResult drain(@PathVariable String id) {
        return runtimeCommand(id, "DRAIN", "drain");
    }

    @PostMapping("/services/{id}/resume")
    @Operation(summary = "Runtime Resume 전환")
    AgentCommandResult resume(@PathVariable String id) {
        return runtimeCommand(id, "RESUME", "resume");
    }

    @GetMapping("/services/{id}/logs")
    @Operation(summary = "제한된 Runtime 로그 묶음 수집")
    ResponseEntity<FileSystemResource> logs(@PathVariable String id) throws Exception {
        requireLogCollectionPolicy();
        catalog.requireAllowed("COLLECT_LOGS");
        Path archive = logs.collect(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + archive.getFileName() + "\"")
                .body(new FileSystemResource(archive));
    }

    private AgentCommandResult runtimeCommand(String id, String command, String operation) {
        String commandId = UUID.randomUUID().toString();
        Instant startedAt = Instant.now();
        try {
            requireCommandPolicy();
            catalog.requireAllowed(command);
            runtime.invoke(id, operation);
            return result(commandId, id, command, CommandState.SUCCEEDED,
                    command + "_ACCEPTED", "Runtime " + operation + " accepted", null, startedAt);
        } catch (Exception exception) {
            return result(commandId, id, command, CommandState.FAILED,
                    command + "_FAILED", SensitiveTextSanitizer.sanitize(exception.getMessage()),
                    null, startedAt);
        }
    }

    private AgentCommandResult serviceCommand(String id, String command, ServiceManager.Action action) {
        String commandId = UUID.randomUUID().toString();
        Instant startedAt = Instant.now();
        try {
            requireCommandPolicy();
            catalog.requireAllowed(command);
            var commandResult = manager.execute(id, action);
            return result(commandId, id, command,
                    commandResult.success() ? CommandState.SUCCEEDED : CommandState.FAILED,
                    commandResult.success() ? "OK" : "SERVICE_COMMAND_FAILED",
                    commandResult.output(), null, startedAt);
        } catch (Exception exception) {
            return result(commandId, id, command, CommandState.FAILED,
                    "SERVICE_COMMAND_FAILED", SensitiveTextSanitizer.sanitize(exception.getMessage()),
                    null, startedAt);
        }
    }

    private AgentCommandResult result(
            String commandId,
            String serviceId,
            String command,
            CommandState state,
            String code,
            String message,
            String version,
            Instant startedAt) {
        return new AgentCommandResult(
                commandId, serviceId, command, state, code, message, version, startedAt, Instant.now());
    }
}

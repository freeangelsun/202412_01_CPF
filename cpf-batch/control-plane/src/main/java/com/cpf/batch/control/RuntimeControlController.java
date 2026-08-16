package com.cpf.batch.control;

import com.cpf.batch.api.RuntimeCommand;
import com.cpf.batch.api.RuntimeHeartbeat;
import com.cpf.batch.api.RuntimeRegistration;
import com.cpf.batch.control.internal.JdbcRuntimeCommandRepository;
import com.cpf.batch.control.internal.JdbcRuntimeRegistry;
import com.cpf.batch.control.security.BatVerifiedActorResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/batch/runtime")
public class RuntimeControlController {
    private final JdbcRuntimeRegistry registry;
    private final JdbcRuntimeCommandRepository commands;
    private final RuntimeCommandExecutor executor;
    private final BatVerifiedActorResolver actorResolver;

    RuntimeControlController(
            JdbcRuntimeRegistry registry,
            JdbcRuntimeCommandRepository commands,
            RuntimeCommandExecutor executor,
            BatVerifiedActorResolver actorResolver) {
        this.registry = registry;
        this.commands = commands;
        this.executor = executor;
        this.actorResolver = actorResolver;
    }

    @PostMapping("/registrations")
    ResponseEntity<Void> register(
            @RequestBody RuntimeRegistration registration,
            HttpServletRequest request) {
        requireRuntimeInstance(request, registration.instanceId());
        registry.register(registration);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/heartbeats")
    ResponseEntity<Void> heartbeat(
            @RequestBody RuntimeHeartbeat heartbeat,
            HttpServletRequest request) {
        requireRuntimeInstance(request, heartbeat.instanceId());
        registry.heartbeat(heartbeat);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/instances")
    List<Map<String, Object>> instances(@RequestParam(defaultValue = "30") long seconds) {
        return registry.list(Duration.ofSeconds(Math.max(5, seconds)));
    }

    @PostMapping("/commands")
    ResponseEntity<Map<String, Object>> command(
            @RequestBody RuntimeCommand command,
            HttpServletRequest request) {
        var actors = actorResolver.approved(
                request,
                command.requestedBy(),
                command.approvedBy(),
                command.approvalRequestId());
        RuntimeCommand verified = new RuntimeCommand(
                command.commandId(),
                command.idempotencyKey(),
                command.commandType(),
                command.targetType(),
                command.targetIds(),
                command.targetSnapshot(),
                command.targetSnapshotHash(),
                command.expectedVersion(),
                actors.requestedBy(),
                command.reason(),
                command.requestedAt(),
                command.approvalPolicyVersion(),
                actors.approvalRequestId(),
                actors.approvedBy(),
                command.expiresAt(),
                command.executionState(),
                command.executionAttempt(),
                command.parameters(),
                command.result(),
                command.failureStage(),
                command.beforeState(),
                command.afterState(),
                command.transactionId(),
                command.evidenceRef());
        return ResponseEntity.accepted().body(executor.execute(verified));
    }

    @GetMapping("/commands/{key}")
    ResponseEntity<Map<String, Object>> state(@PathVariable String key) {
        return commands.find(key)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private void requireRuntimeInstance(HttpServletRequest request, String bodyInstanceId) {
        String authenticatedInstanceId = actorResolver.identity(request).callerInstanceId();
        if (bodyInstanceId == null || !bodyInstanceId.equals(authenticatedInstanceId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Runtime instanceId does not match authenticated caller instance");
        }
    }
}

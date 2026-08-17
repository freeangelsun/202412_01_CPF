package com.cpf.admin.opr.batch.runtime;

import com.cpf.platform.operations.api.runtime.CpfInstanceIdentity;

import com.cpf.admin.approval.api.AdmApprovalExecutionStatus;
import com.cpf.admin.approval.api.AdmApprovedOperationCommand;
import com.cpf.admin.approval.api.AdmApprovedOperationResult;
import com.cpf.admin.approval.spi.AdmApprovalOwnerCommandPort;
import com.cpf.batch.api.BatControlHeaders;
import com.cpf.batch.api.CommandState;
import com.cpf.batch.api.DeploymentResult;
import com.cpf.batch.api.RuntimeCommand;
import com.cpf.web.api.CpfHttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * ADM Approval Engine에서 승인된 BAT 명령만 BAT Control Server로 위임합니다.
 */
@Component("BAT")
public class BatApprovalOwnerCommandPort implements AdmApprovalOwnerCommandPort {
    private static final String CALLER_SERVICE = "ADM";

    private final RestClient client;
    private final String callerInstanceId;

    public BatApprovalOwnerCommandPort(
            RestClient.Builder builder,
            @Value("${cpf.batch.control.base-url}") String baseUrl) {
        String explicitBaseUrl = requireRemoteBaseUrl(baseUrl);
        this.client = builder.baseUrl(explicitBaseUrl).build();
        this.callerInstanceId = CpfInstanceIdentity.current().instanceId();
    }

    @Override
    public boolean supports(String ownerModule, String ownerCommand) {
        if (!"BAT".equals(Objects.toString(ownerModule, "").trim())) return false;
        return Set.of("DEPLOY_PLAN", "ROLLBACK_PLAN", "START", "STOP", "RESTART", "DRAIN", "RESUME", "ROLLBACK")
                .contains(Objects.toString(ownerCommand, "").trim());
    }

    @Override
    public boolean supports(String ownerModule, String ownerCommand, String actionType, String targetType) {
        if (!supports(ownerModule, ownerCommand)) return false;
        String command = Objects.toString(ownerCommand, "").trim();
        String action = Objects.toString(actionType, "").trim();
        String target = Objects.toString(targetType, "").trim();
        if (!command.equals(action)) return false;
        return Set.of("DEPLOY_PLAN", "ROLLBACK_PLAN").contains(command)
                ? Set.of("BAT_DEPLOYMENT_PLAN", "DEPLOYMENT_PLAN").contains(target)
                : Set.of("BAT_RUNTIME", "BAT_INSTANCE", "BAT_EXECUTION", "BAT_WORKER").contains(target);
    }

    @Override
    public AdmApprovedOperationResult execute(AdmApprovedOperationCommand command) {
        if (command == null || !supports(command.ownerModule(), command.ownerCommand(), command.actionType(), command.targetType())) {
            return failed("BAT-OWNER-MISMATCH", "BAT Owner/Command/Action/Target mismatch");
        }
        try {
            String ownerCommand = command.ownerCommand();
            if (Set.of("DEPLOY_PLAN", "ROLLBACK_PLAN").contains(ownerCommand)) {
                return executeDeployment(command, ownerCommand);
            }
            if (Set.of("START", "STOP", "RESTART", "DRAIN", "RESUME", "ROLLBACK")
                    .contains(ownerCommand)) {
                return executeRuntimeCommand(command, ownerCommand);
            }
            return failed("BAT-UNSUPPORTED-COMMAND", "Unsupported BAT owner command");
        } catch (RuntimeException exception) {
            return unknown("BAT-OWNER-EXCEPTION");
        }
    }

    @Override
    public AdmApprovedOperationResult reconcile(AdmApprovedOperationCommand command) {
        if (command == null || !supports(command.ownerModule(), command.ownerCommand(), command.actionType(), command.targetType())) {
            return failed("BAT-OWNER-MISMATCH", "BAT Owner/Command/Action/Target mismatch");
        }
        try {
            boolean deployment = Set.of("DEPLOY_PLAN", "ROLLBACK_PLAN").contains(command.ownerCommand());
            RestClient.RequestHeadersSpec<?> observation = deployment
                    ? client.get().uri("/api/v1/batch/deployment-plans/{id}/executions/by-command/{key}",
                            command.targetId(), command.commandRequestId())
                    : client.get().uri("/api/v1/batch/runtime/commands/{key}", command.commandRequestId());
            Map<?, ?> state = observation
                    .headers(headers -> {
                        headers.set(BatControlHeaders.CALLER_SERVICE, CALLER_SERVICE);
                        headers.set(BatControlHeaders.CALLER_INSTANCE_ID, callerInstanceId);
                        headers.set(BatControlHeaders.OPERATOR_ID, command.approvedBy());
                        headers.set(CpfHttpHeaders.transactionId(), command.transactionId());
                        headers.set(BatControlHeaders.APPROVAL_REQUEST_ID, Long.toString(command.approvalRequestId()));
                        headers.set(BatControlHeaders.APPROVAL_REQUESTER_ID, command.requestedBy());
                    })
                    .retrieve().body(Map.class);
            if (state == null || state.isEmpty()) return unknown("BAT-RECONCILE-NOT-OBSERVED");
            String observedRequestId = Objects.toString(state.get("request_id"),
                    Objects.toString(state.get("requestId"), Objects.toString(state.get("command_request_id"), "")));
            if (!observedRequestId.isBlank() && !command.commandRequestId().equals(observedRequestId)) {
                return unknown("BAT-RECONCILE-CORRELATION-MISMATCH");
            }
            String resolvedState = Objects.toString(state.get("command_state"),
                    Objects.toString(state.get("commandState"),
                            Objects.toString(state.get("state"), "UNKNOWN_RESULT")));
            return map(CommandState.valueOf(resolvedState.toUpperCase(Locale.ROOT)),
                    "BAT-RECONCILED-" + resolvedState.toUpperCase(Locale.ROOT), "BAT owner ledger observation");
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound notObserved) {
            return unknown("BAT-RECONCILE-NOT-OBSERVED");
        } catch (IllegalArgumentException invalidState) {
            return unknown("BAT-RECONCILE-UNKNOWN-STATE");
        } catch (RuntimeException readFailure) {
            return unknown("BAT-RECONCILE-READ-FAILED");
        }
    }

    private AdmApprovedOperationResult executeDeployment(
            AdmApprovedOperationCommand command,
            String ownerCommand) {
        String endpoint = "ROLLBACK_PLAN".equals(ownerCommand)
                ? "/api/v1/batch/deployment-plans/{id}/rollback-approved"
                : "/api/v1/batch/deployment-plans/{id}/execute-approved";
        ApprovedExecution body = new ApprovedExecution(
                command.approvalRequestId(),
                command.commandRequestId(),
                0L,
                command.requestedBy(),
                command.approvedBy(),
                command.reason());
        DeploymentResult result = withApprovalHeaders(
                client.post().uri(endpoint, command.targetId()),
                command)
                .body(body)
                .retrieve()
                .body(DeploymentResult.class);
        if (result == null) {
            return unknown("BAT-NO-RESULT");
        }
        return map(result.state(), "BAT-" + result.state(), result.message());
    }

    private AdmApprovedOperationResult executeRuntimeCommand(
            AdmApprovedOperationCommand command,
            String ownerCommand) {
        Instant now = Instant.now();
        RuntimeCommand body = new RuntimeCommand(
                command.commandRequestId(),
                command.commandRequestId(),
                ownerCommand,
                command.targetType(),
                List.of(command.targetId()),
                command.targetId(),
                command.payloadHash(),
                0L,
                command.requestedBy(),
                command.reason(),
                now,
                "ADM_APPROVAL",
                Long.toString(command.approvalRequestId()),
                command.approvedBy(),
                now.plusSeconds(900),
                CommandState.APPROVED,
                0,
                Map.of(),
                null,
                null,
                null,
                null,
                command.transactionId(),
                null);
        Map<?, ?> state = withApprovalHeaders(
                client.post().uri("/api/v1/batch/runtime/commands"),
                command)
                .body(body)
                .retrieve()
                .body(Map.class);
        if (state == null) {
            return unknown("BAT-NO-RESULT");
        }
        String resolvedState = Objects.toString(
                state.get("command_state"),
                Objects.toString(state.get("commandState"), "UNKNOWN_RESULT"));
        try {
            return map(
                    CommandState.valueOf(resolvedState),
                    "BAT-" + resolvedState,
                    "BAT runtime command");
        } catch (IllegalArgumentException exception) {
            return unknown("BAT-UNKNOWN-STATE");
        }
    }

    private RestClient.RequestBodySpec withApprovalHeaders(
            RestClient.RequestBodySpec request,
            AdmApprovedOperationCommand command) {
        return request.headers(headers -> {
            headers.set(BatControlHeaders.CALLER_SERVICE, CALLER_SERVICE);
            headers.set(BatControlHeaders.CALLER_INSTANCE_ID, callerInstanceId);
            headers.set(BatControlHeaders.OPERATOR_ID, command.approvedBy());
            headers.set(CpfHttpHeaders.transactionId(), command.transactionId());
            headers.set(
                    BatControlHeaders.APPROVAL_REQUEST_ID,
                    Long.toString(command.approvalRequestId()));
            headers.set(BatControlHeaders.APPROVAL_REQUESTER_ID, command.requestedBy());
        });
    }

    private static AdmApprovedOperationResult map(
            CommandState state,
            String code,
            String message) {
        return switch (state) {
            case SUCCEEDED, ROLLED_BACK ->
                    new AdmApprovedOperationResult(AdmApprovalExecutionStatus.SUCCEEDED, code, message);
            case FAILED, PARTIALLY_ROLLED_BACK -> failed(code, message);
            default -> unknown(code);
        };
    }

    private static AdmApprovedOperationResult failed(String code, String message) {
        return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.FAILED, code, message);
    }

    private static AdmApprovedOperationResult unknown(String code) {
        return new AdmApprovedOperationResult(
                AdmApprovalExecutionStatus.UNKNOWN,
                code,
                "Owner result is unknown; reconciliation required");
    }

    private static String requireRemoteBaseUrl(String value) {
        String baseUrl = requireText(value, "cpf.batch.control.base-url");
        URI uri = URI.create(baseUrl);
        String host = Objects.toString(uri.getHost(), "").trim().toLowerCase(Locale.ROOT);
        if (!Set.of("http", "https").contains(Objects.toString(uri.getScheme(), "").toLowerCase(Locale.ROOT))
                || host.isBlank()
                || host.equals("localhost")
                || host.equals("127.0.0.1")
                || host.equals("::1")
                || host.equals("0.0.0.0")) {
            throw new IllegalArgumentException("cpf.batch.control.base-url must be an explicit non-loopback HTTP(S) endpoint");
        }
        return baseUrl;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    private record ApprovedExecution(
            long approvalRequestId,
            String commandRequestId,
            long expectedVersion,
            String requestedBy,
            String approvedBy,
            String reason) {
    }
}

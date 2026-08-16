package com.cpf.batch.control.deploy;

import com.cpf.batch.api.AgentArtifactRequest;
import com.cpf.batch.api.AgentCommandResult;
import com.cpf.batch.api.CommandState;
import com.cpf.batch.api.DeploymentCellManifest;
import com.cpf.batch.api.DeploymentResult;
import com.cpf.batch.spi.DeploymentTargetAdapter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Agent 배포 Adapter. 모든 명령은 안정 command ID로 멱등 처리하고 통신 단절 시 결과를 조회합니다. */
@Component
public final class AgentDeploymentAdapter implements DeploymentTargetAdapter {
    private static final Set<String> AGENT_MODES = Set.of("embedded-bootjar", "external-tomcat-war");
    private static final String LABEL_KEY_ID = "cpf.artifact.key-id";
    private static final String LABEL_RELEASE_SEQUENCE = "cpf.release.sequence";
    private static final String LABEL_RELEASE_CHANNEL = "cpf.release.channel";
    private final RestClient.Builder builder;

    public AgentDeploymentAdapter(RestClient.Builder builder) { this.builder = builder; }

    @Override
    public boolean supports(DeploymentCellManifest.Instance instance, String mode) {
        return instance.agentBaseUrl() != null && !instance.agentBaseUrl().isBlank()
                && mode != null && AGENT_MODES.contains(mode.toLowerCase(Locale.ROOT));
    }

    @Override
    public DeploymentResult.InstanceResult deploy(DeploymentCellManifest manifest, DeploymentCellManifest.Instance instance) {
        String commandId = commandId(manifest, instance, "INSTALL");
        try {
            AgentArtifactRequest request = new AgentArtifactRequest(
                    manifest.serviceId(),
                    manifest.artifact().coordinate(),
                    manifest.artifact().version(),
                    manifest.artifact().sha256(),
                    manifest.artifact().signatureBase64(),
                    manifest.runtimeMode(),
                    instance.configRef(),
                    "cpf-batch-control-plane",
                    "approved deployment " + manifest.cellId(),
                    requiredReleaseSequence(manifest),
                    required(manifest.environment(), "manifest.environment"),
                    requiredLabel(manifest, LABEL_RELEASE_CHANNEL),
                    requiredLabel(manifest, LABEL_KEY_ID));
            AgentCommandResult result = client(instance).post().uri("/api/v1/agent/artifacts/install")
                    .header("Idempotency-Key", commandId).header("X-CPF-Command-ID", commandId)
                    .body(request).retrieve().body(AgentCommandResult.class);
            return checked(instance, "INSTALL", commandId, result);
        } catch (RuntimeException failure) {
            return reconcile(instance, "INSTALL", commandId, failure);
        }
    }

    @Override public DeploymentResult.InstanceResult rollback(DeploymentCellManifest m, DeploymentCellManifest.Instance i) { return command(m, i, "ROLLBACK"); }
    @Override public DeploymentResult.InstanceResult start(DeploymentCellManifest m, DeploymentCellManifest.Instance i) { return command(m, i, "START"); }
    @Override public DeploymentResult.InstanceResult stop(DeploymentCellManifest m, DeploymentCellManifest.Instance i) { return command(m, i, "STOP"); }
    @Override public DeploymentResult.InstanceResult drain(DeploymentCellManifest m, DeploymentCellManifest.Instance i) { return command(m, i, "DRAIN"); }
    @Override public DeploymentResult.InstanceResult resume(DeploymentCellManifest m, DeploymentCellManifest.Instance i) { return command(m, i, "RESUME"); }

    private DeploymentResult.InstanceResult command(DeploymentCellManifest manifest, DeploymentCellManifest.Instance instance, String operation) {
        String commandId = commandId(manifest, instance, operation);
        try {
            AgentCommandResult result = client(instance).post()
                    .uri("/api/v1/agent/services/{service}/{operation}", manifest.serviceId(), operation.toLowerCase(Locale.ROOT))
                    .header("Idempotency-Key", commandId).header("X-CPF-Command-ID", commandId)
                    .retrieve().body(AgentCommandResult.class);
            return checked(instance, operation, commandId, result);
        } catch (RuntimeException failure) {
            return reconcile(instance, operation, commandId, failure);
        }
    }

    private DeploymentResult.InstanceResult reconcile(DeploymentCellManifest.Instance instance, String operation, String commandId, RuntimeException original) {
        try {
            AgentCommandResult result = client(instance).get().uri("/api/v1/agent/commands/{commandId}", commandId)
                    .retrieve().body(AgentCommandResult.class);
            return checked(instance, operation, commandId, result);
        } catch (RuntimeException reconciliationFailure) {
            return new DeploymentResult.InstanceResult(instance.instanceId(), CommandState.UNKNOWN_RESULT, operation,
                    "AGENT_COMMAND_UNRESOLVED:" + original.getClass().getSimpleName() + ":" + reconciliationFailure.getClass().getSimpleName());
        }
    }

    private DeploymentResult.InstanceResult checked(DeploymentCellManifest.Instance instance, String operation, String commandId, AgentCommandResult result) {
        if (result == null) return new DeploymentResult.InstanceResult(instance.instanceId(), CommandState.UNKNOWN_RESULT, operation, "AGENT_RESULT_EMPTY:" + commandId);
        if (!commandId.equals(result.commandId())) return new DeploymentResult.InstanceResult(instance.instanceId(), CommandState.FAILED, operation, "AGENT_COMMAND_ID_MISMATCH");
        return new DeploymentResult.InstanceResult(instance.instanceId(), result.state(), operation,
                result.resultCode() + ":" + (result.message() == null ? "" : result.message()));
    }

    private RestClient client(DeploymentCellManifest.Instance instance) { return builder.baseUrl(instance.agentBaseUrl()).build(); }

    static String commandId(DeploymentCellManifest manifest, DeploymentCellManifest.Instance instance, String operation) {
        String material = manifest.cellId() + "|" + manifest.serviceId() + "|" + instance.instanceId() + "|"
                + operation + "|" + manifest.artifact().coordinate() + "|" + manifest.artifact().version() + "|"
                + manifest.artifact().sha256() + "|" + required(manifest.environment(), "manifest.environment") + "|"
                + requiredLabel(manifest, LABEL_RELEASE_CHANNEL) + "|" + requiredReleaseSequence(manifest) + "|"
                + requiredLabel(manifest, LABEL_KEY_ID);
        try {
            return "batcmd-" + HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(material.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception impossible) { throw new IllegalStateException("SHA-256 unavailable", impossible); }
    }
    private static long requiredReleaseSequence(DeploymentCellManifest manifest) {
        String value = requiredLabel(manifest, LABEL_RELEASE_SEQUENCE);
        try {
            long sequence = Long.parseLong(value);
            if (sequence <= 0) throw new IllegalArgumentException("Release sequence must be positive");
            return sequence;
        } catch (NumberFormatException failure) {
            throw new IllegalArgumentException("Invalid " + LABEL_RELEASE_SEQUENCE + ": " + value, failure);
        }
    }

    private static String requiredLabel(DeploymentCellManifest manifest, String key) {
        return required(manifest.labels().get(key), "manifest.labels[" + key + "]");
    }

    private static String required(String value, String name) {
        if (value == null || value.isBlank() || value.indexOf('\r') >= 0 || value.indexOf('\n') >= 0) {
            throw new IllegalArgumentException(name + " is required and must not contain control characters");
        }
        return value.trim();
    }

}

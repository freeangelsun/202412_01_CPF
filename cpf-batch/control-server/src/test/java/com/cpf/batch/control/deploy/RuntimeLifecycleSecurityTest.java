package com.cpf.batch.control.deploy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cpf.batch.api.DeploymentCellManifest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

/** Agent 명령 ID 안정성, HTTPS 강제, Health path 우회 차단을 검증합니다. */
class RuntimeLifecycleSecurityTest {
    @Test
    void commandIdIncludesApprovalAndPatchIdentity() {
        String first = RuntimeLifecycleService.commandId(
                "i1", "svc", "restart", "req", "app", "APR-1", "patch");
        String same = RuntimeLifecycleService.commandId(
                "i1", "svc", "restart", "req", "app", "APR-1", "patch");
        String otherApproval = RuntimeLifecycleService.commandId(
                "i1", "svc", "restart", "req", "app", "APR-2", "patch");

        assertThat(first).isEqualTo(same).startsWith("batctl-");
        assertThat(otherApproval).isNotEqualTo(first);
    }

    @Test
    void rejectsInsecureAgentEndpoint() {
        assertThatThrownBy(() -> RuntimeLifecycleService.validatedAgentUri("http://10.1.2.3:8080"))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    void healthProbeRejectsAuthorityOverridePath() {
        HttpRuntimeHealthProbe probe = new HttpRuntimeHealthProbe(new RestClient.Builder(), "https", true);
        DeploymentCellManifest.Instance instance = new DeploymentCellManifest.Instance(
                "i", "w", "host.internal", 8443, "p", "z", "pool", List.of(), "https://agent", "cfg");

        assertThat(probe.endpoint(instance, "/actuator/health").getScheme()).isEqualTo("https");
        assertThatThrownBy(() -> probe.endpoint(instance, "//evil.example/x"))
                .isInstanceOf(SecurityException.class);
    }
}

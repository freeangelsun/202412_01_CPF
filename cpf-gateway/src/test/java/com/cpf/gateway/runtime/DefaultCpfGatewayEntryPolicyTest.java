package com.cpf.gateway.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cpf.gateway.api.CpfGatewayEntryPolicyPort;
import com.cpf.gateway.config.CpfGatewaySafetyProperties;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DefaultCpfGatewayEntryPolicyTest {
    private static final Instant NOW = Instant.parse("2026-08-05T00:00:00Z");

    @Test
    void activeTlsRequestOnDataPlaneIsAllowed() {
        CpfGatewaySafetyProperties safety = safety();
        DefaultCpfGatewayEntryPolicy policy = policy(safety);

        CpfGatewayEntryPolicyPort.Decision decision = policy.evaluate(request(true, 8443, "HTTP/2"));

        assertThat(decision.allowed()).isTrue();
        assertThat(decision.state()).isEqualTo(CpfGatewayEntryPolicyPort.State.ACTIVE);
        assertThat(decision.policyVersion()).isZero();
    }

    @Test
    void wrongPortProtocolAndPlainHttpFailClosedBeforeRouting() {
        CpfGatewaySafetyProperties safety = safety();
        DefaultCpfGatewayEntryPolicy policy = policy(safety);

        assertThat(policy.evaluate(request(true, 9443, "HTTP/1.1")).reason())
                .isEqualTo("DATA_PLANE_PORT_MISMATCH");
        assertThat(policy.evaluate(request(true, 8443, "HTTP/1.0")).httpStatus())
                .isEqualTo(505);
        assertThat(policy.evaluate(request(false, 8443, "HTTP/1.1")).httpStatus())
                .isEqualTo(426);
    }

    @Test
    void maintenanceCasIsIdempotentButRejectsStaleAndSameVersionConflict() {
        CpfGatewaySafetyProperties safety = safety();
        DefaultCpfGatewayEntryPolicy policy = policy(safety);

        CpfGatewayEntryPolicyPort.Snapshot applied = policy.replace(
                0L, 1L, CpfGatewayEntryPolicyPort.State.MAINTENANCE, Duration.ofSeconds(120));
        CpfGatewayEntryPolicyPort.Snapshot replay = policy.replace(
                1L, 1L, CpfGatewayEntryPolicyPort.State.MAINTENANCE, Duration.ofSeconds(120));
        CpfGatewayEntryPolicyPort.Decision denied = policy.evaluate(request(true, 8443, "HTTP/1.1"));

        assertThat(replay).isEqualTo(applied);
        assertThat(denied.httpStatus()).isEqualTo(503);
        assertThat(denied.retryAfter()).isEqualTo(Duration.ofSeconds(120));
        assertThatThrownBy(() -> policy.replace(
                0L, 2L, CpfGatewayEntryPolicyPort.State.ACTIVE, Duration.ZERO))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("expectedVersion");
        assertThatThrownBy(() -> policy.replace(
                1L, 1L, CpfGatewayEntryPolicyPort.State.ACTIVE, Duration.ZERO))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("same-version");
    }

    @Test
    void telemetryCountsAllowedAndDenialCategoriesWithoutIdentifiers() {
        DefaultCpfGatewayEntryPolicy policy = policy(safety());

        policy.evaluate(request(true, 8443, "HTTP/1.1"));
        policy.evaluate(request(true, 9443, "HTTP/1.1"));
        policy.evaluate(request(true, 8443, "HTTP/1.0"));
        policy.evaluate(request(false, 8443, "HTTP/1.1"));
        policy.replace(0L, 1L, CpfGatewayEntryPolicyPort.State.MAINTENANCE, Duration.ofSeconds(60));
        policy.evaluate(request(true, 8443, "HTTP/1.1"));

        CpfGatewayEntryPolicyPort.Telemetry telemetry = policy.telemetry();
        assertThat(telemetry.allowed()).isEqualTo(1L);
        assertThat(telemetry.denied()).isEqualTo(4L);
        assertThat(telemetry.portDenied()).isEqualTo(1L);
        assertThat(telemetry.protocolDenied()).isEqualTo(1L);
        assertThat(telemetry.tlsDenied()).isEqualTo(1L);
        assertThat(telemetry.maintenanceDenied()).isEqualTo(1L);
        assertThat(telemetry.observedAt()).isEqualTo(NOW);
    }

    @Test
    void retryAfterCannotExceedInstallationCap() {
        DefaultCpfGatewayEntryPolicy policy = policy(safety());
        assertThatThrownBy(() -> policy.replace(
                0L, 1L, CpfGatewayEntryPolicyPort.State.MAINTENANCE, Duration.ofMinutes(16)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static CpfGatewaySafetyProperties safety() {
        CpfGatewaySafetyProperties safety = new CpfGatewaySafetyProperties();
        safety.setDataPlanePort(8443);
        safety.setRequireTlsIngress(true);
        safety.setAllowedIngressProtocols(Set.of("HTTP/1.1", "HTTP/2.0"));
        safety.setMaintenanceRetryAfter(Duration.ofSeconds(60));
        safety.setMaintenanceRetryAfterCap(Duration.ofMinutes(15));
        safety.validate();
        return safety;
    }

    private static DefaultCpfGatewayEntryPolicy policy(CpfGatewaySafetyProperties safety) {
        return new DefaultCpfGatewayEntryPolicy(safety, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private static CpfGatewayEntryPolicyPort.Request request(
            boolean secure, int port, String protocol) {
        return new CpfGatewayEntryPolicyPort.Request(
                "/cpf/execute/OACCAC0001", "POST", protocol, secure, port, "127.0.0.1", NOW);
    }
}

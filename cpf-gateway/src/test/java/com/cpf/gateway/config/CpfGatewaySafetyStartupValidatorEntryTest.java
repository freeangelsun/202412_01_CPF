package com.cpf.gateway.config;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cpf.core.api.gateway.CpfGatewayRateLimitCounterPort;
import com.cpf.gateway.control.CpfGatewayControlSecurityProperties;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class CpfGatewaySafetyStartupValidatorEntryTest {
    @Test
    void matchingTlsDataPlaneStarts() {
        CpfGatewaySafetyProperties safety = safety("local");
        MockEnvironment environment = new MockEnvironment()
                .withProperty("server.port", "8443")
                .withProperty("server.ssl.enabled", "true");

        assertThatCode(() -> new CpfGatewaySafetyStartupValidator(
                safety, counter(), environment, new CpfGatewayControlSecurityProperties()).validate())
                .doesNotThrowAnyException();
    }

    @Test
    void listenerMismatchAndDisabledTlsFailFast() {
        CpfGatewaySafetyProperties safety = safety("local");
        MockEnvironment mismatch = new MockEnvironment()
                .withProperty("server.port", "8070")
                .withProperty("server.ssl.enabled", "true");
        assertThatThrownBy(() -> new CpfGatewaySafetyStartupValidator(
                safety, counter(), mismatch, new CpfGatewayControlSecurityProperties()).validate())
                .hasMessageContaining("data-plane-port");

        MockEnvironment noTls = new MockEnvironment()
                .withProperty("server.port", "8443")
                .withProperty("server.ssl.enabled", "false");
        assertThatThrownBy(() -> new CpfGatewaySafetyStartupValidator(
                safety, counter(), noTls, new CpfGatewayControlSecurityProperties()).validate())
                .hasMessageContaining("server.ssl.enabled=false");
    }

    @Test
    void controlPlaneMustUseDifferentTlsPortInProduction() {
        CpfGatewaySafetyProperties safety = safety("production");
        CpfGatewayControlSecurityProperties control = control(8443, false);
        MockEnvironment environment = new MockEnvironment()
                .withProperty("server.port", "8443")
                .withProperty("server.ssl.enabled", "true");

        assertThatThrownBy(() -> new CpfGatewaySafetyStartupValidator(
                safety, counter(), environment, control).validate())
                .hasMessageContaining("ports must differ");

        control = control(9070, false);
        CpfGatewayControlSecurityProperties plainControl = control;
        assertThatThrownBy(() -> new CpfGatewaySafetyStartupValidator(
                safety, counter(), environment, plainControl).validate())
                .hasMessageContaining("Control Plane requires TLS");
    }

    private static CpfGatewaySafetyProperties safety(String environmentCode) {
        CpfGatewaySafetyProperties safety = new CpfGatewaySafetyProperties();
        safety.setEnvironmentCode(environmentCode);
        safety.setDataPlanePort(8443);
        safety.setRequireTlsIngress(true);
        if ("production".equals(environmentCode)) {
            safety.setRequireDistributedRateLimitCounter(true);
            safety.setRateLimitCounterMode("JDBC");
        }
        return safety;
    }

    private static CpfGatewayRateLimitCounterPort counter() {
        CpfGatewayRateLimitCounterPort counter = mock(CpfGatewayRateLimitCounterPort.class);
        when(counter.distributed()).thenReturn(true);
        when(counter.health()).thenReturn(new CpfGatewayRateLimitCounterPort.CounterHealth(
                true, 0L, "READY", Instant.EPOCH));
        return counter;
    }

    private static CpfGatewayControlSecurityProperties control(int port, boolean tls) {
        CpfGatewayControlSecurityProperties control = new CpfGatewayControlSecurityProperties();
        control.setEnabled(true);
        control.setListenerPort(port);
        control.setSharedSecret("0123456789abcdef0123456789abcdef");
        control.setTlsEnabled(tls);
        if (tls) {
            control.setKeyStore("/run/secrets/control.p12");
            control.setKeyStorePassword("secret-reference-value");
        }
        return control;
    }
}

package com.cpf.batch.runtime;

import com.cpf.batch.api.RuntimeRole;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class RuntimeIdentityFactoryTest {
    @Test
    void springPropertiesDefineTheRuntimeIdentityAndEndpoints() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("server.port", "19092")
                .withProperty("cpf.framework.was-id", "BAT-WAS-02")
                .withProperty("cpf.batch.runtime.host-alias", "home-notebook")
                .withProperty("cpf.batch.runtime.zone", "home")
                .withProperty("cpf.batch.runtime.pool", "general")
                .withProperty("cpf.batch.runtime.capabilities", "GENERAL, FILE,GENERAL")
                .withProperty("spring.profiles.active", "local")
                .withProperty("cpf.batch.runtime.config-version", "config-7");

        var registration = RuntimeIdentityFactory.fromEnvironment(
                environment, RuntimeRole.WORKER, "cpf-batch-worker", 18092);

        assertThat(registration.instanceId()).isEqualTo(com.cpf.foundation.runtime.CpfInstanceIdentity.current().instanceId());
        assertThat(registration.wasId()).isEqualTo("BAT-WAS-02");
        assertThat(registration.hostAlias()).isEqualTo("home-notebook");
        assertThat(registration.zone()).isEqualTo("home");
        assertThat(registration.pool()).isEqualTo("general");
        assertThat(registration.profile()).isEqualTo("local");
        assertThat(registration.configVersion()).isEqualTo("config-7");
        assertThat(registration.capabilities()).containsExactly("GENERAL", "FILE", "GENERAL");
        assertThat(registration.endpoints())
                .containsEntry("base", "http://127.0.0.1:19092")
                .containsEntry("health", "http://127.0.0.1:19092/actuator/health/readiness");
    }

    @Test
    void environmentStyleKeysRemainSupportedAsFallback() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("CPF_RUNTIME_INSTANCE_ID", "scheduler-env-01")
                .withProperty("CPF_PORT", "18091")
                .withProperty("SPRING_PROFILES_ACTIVE", "stg");

        var registration = RuntimeIdentityFactory.fromEnvironment(
                environment, RuntimeRole.SCHEDULER, "cpf-batch-scheduler", 18091);

        assertThat(registration.instanceId()).isEqualTo("scheduler-env-01");
        assertThat(registration.profile()).isEqualTo("stg");
        assertThat(registration.endpoints().get("base")).endsWith(":18091");
    }

    @Test
    void batchRuntimeUsesTheCanonicalCentralServiceIdentity() {
        for (RuntimeRole role : RuntimeRole.values()) {
            if (role == RuntimeRole.CENTER_CUT_RUNNER) continue;
            var registration = RuntimeIdentityFactory.fromBatchEnvironment(
                    new MockEnvironment(), role, 18080);

            assertThat(registration.serviceId()).isEqualTo("BAT");
            assertThat(registration.runtimeRole()).isEqualTo(role);
        }
    }
    @Test
    void centerCutRuntimeUsesIndependentCecIdentity() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("cpf.framework.module-id", "CEC");
        var registration = RuntimeIdentityFactory.fromCenterCutEnvironment(environment, 8183);
        assertThat(registration.serviceId()).isEqualTo("CEC");
        assertThat(registration.moduleId()).isEqualTo("CEC");
        assertThat(registration.runtimeRole()).isEqualTo(RuntimeRole.CENTER_CUT_RUNNER);
    }

}

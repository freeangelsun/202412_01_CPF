package com.cpf.batch.testkit;

import com.cpf.batch.api.RuntimeRegistration;
import com.cpf.batch.api.RuntimeRole;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class RuntimeRegistrationFixture {
    private RuntimeRegistrationFixture() {}

    public static RuntimeRegistration ready(RuntimeRole role, String instanceId) {
        return new RuntimeRegistration(role, "test-" + role.name().toLowerCase(), instanceId, "BAT",
                instanceId, "test-host", "test-zone", "test-pool", "test", "TEST_SHA",
                "TEST_CHECKSUM", "test", List.of(role.name()), Map.of(), "test", "*", "v1", Instant.now());
    }
}

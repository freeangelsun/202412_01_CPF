package com.cpf.starter.observability;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import java.util.function.Supplier;

/** CPF 식별자를 Micrometer Observation에 연결하는 얇은 경계입니다. */
public final class CpfObservationSupport {
    private final ObservationRegistry registry;
    public CpfObservationSupport(ObservationRegistry registry) { this.registry = registry; }
    public <T> T observe(String operation, String transactionId, String systemCode, Supplier<T> action) {
        Observation observation = Observation.createNotStarted(operation, registry)
                .lowCardinalityKeyValue("cpf.system.code", safe(systemCode))
                .highCardinalityKeyValue("cpf.transaction.id", safe(transactionId));
        return observation.observe(action);
    }
    private static String safe(String value) { return value == null || value.isBlank() ? "UNKNOWN" : value; }
}

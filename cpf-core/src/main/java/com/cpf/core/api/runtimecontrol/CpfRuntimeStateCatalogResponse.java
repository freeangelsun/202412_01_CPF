package com.cpf.core.api.runtimecontrol;

import java.util.Set;

/** ADM/OpenAPI가 사용하는 Runtime 상태값 Catalog입니다. */
public record CpfRuntimeStateCatalogResponse(
        Set<String> changeStates,
        Set<String> deliveryStates,
        Set<String> ackStates,
        Set<String> driftStates) {
    public CpfRuntimeStateCatalogResponse {
        changeStates = changeStates == null ? Set.of() : Set.copyOf(changeStates);
        deliveryStates = deliveryStates == null ? Set.of() : Set.copyOf(deliveryStates);
        ackStates = ackStates == null ? Set.of() : Set.copyOf(ackStates);
        driftStates = driftStates == null ? Set.of() : Set.copyOf(driftStates);
    }
}

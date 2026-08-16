package com.cpf.platform.operations.runtimecontrol;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/** Java/API/SQL/ADM가 동일한 상태값을 사용하도록 노출하는 canonical catalog입니다. */
public final class CpfRuntimeStateCatalog {
    private static final Set<String> ACK = names(CpfRuntimeAckState.values());
    private static final Set<String> DELIVERY = names(CpfRuntimeDeliveryState.values());
    private static final Set<String> CHANGE = names(CpfRuntimeChangeState.values());
    private static final Set<String> DRIFT = names(CpfRuntimeDriftState.values());

    private CpfRuntimeStateCatalog() { }

    public static Set<String> ackStates() { return ACK; }
    public static Set<String> deliveryStates() { return DELIVERY; }
    public static Set<String> changeStates() { return CHANGE; }
    public static Set<String> driftStates() { return DRIFT; }

    public static boolean isAckState(String value) {
        return value != null && ACK.contains(value.trim().toUpperCase(java.util.Locale.ROOT));
    }

    private static Set<String> names(Enum<?>[] values) {
        return Arrays.stream(values).map(Enum::name).collect(Collectors.toUnmodifiableSet());
    }
}

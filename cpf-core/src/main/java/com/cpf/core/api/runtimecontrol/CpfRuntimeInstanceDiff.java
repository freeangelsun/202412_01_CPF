package com.cpf.core.api.runtimecontrol;

/** Runtime 변경 전후 instance별 desired/actual 차이입니다. */
public record CpfRuntimeInstanceDiff(
        String instanceId,
        long currentDesiredVersion,
        long currentActualVersion,
        String currentDesiredHash,
        String currentActualHash,
        String currentDriftState,
        String newPayloadHash,
        boolean changed,
        String restartImpact) {
}

package com.cpf.platform.operations.runtimecontrol;

/** Runtime Agent가 durable Inbox를 근거로 재보고하는 기능별 실제 상태입니다. */
public record CpfRuntimeActualState(
        String changeType,
        long actualVersion,
        String actualHash,
        String sourceDeliveryId) {
}

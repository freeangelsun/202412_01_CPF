package com.cpf.platform.operations.runtimecontrol;

/** Delivery state별 건수입니다. */
public record CpfRuntimeDeliveryCount(String state, long count) {
    public CpfRuntimeDeliveryCount {
        if (count < 0) throw new IllegalArgumentException("Delivery count는 음수일 수 없습니다.");
    }
}

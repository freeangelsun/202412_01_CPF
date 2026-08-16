package com.cpf.platform.operations.runtimecontrol;

/** Restart impact별 대상 건수입니다. */
public record CpfRuntimeImpactCount(String impact, int count) {
    public CpfRuntimeImpactCount {
        if (count < 0) throw new IllegalArgumentException("Impact count는 음수일 수 없습니다.");
    }
}

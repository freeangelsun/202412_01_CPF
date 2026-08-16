package com.cpf.platform.operations.runtimecontrol;

/**
 * Runtime 기능별 적용 SPI입니다. 로그/캐시/Gateway/BAT/공통코드 등은 이 SPI를 구현하여
 * 동일 Control Plane delivery/ACK/drift 계약을 공유합니다.
 */
public interface CpfRuntimeChangeApplier {
    String changeType();

    /** 지원 payload schema version입니다. */
    default int payloadSchemaVersion() { return 1; }

    /** 변경 적용의 재기동 영향입니다. */
    default CpfRuntimeRestartImpact restartImpact() { return CpfRuntimeRestartImpact.HOT_APPLY; }

    /** 동일 delivery를 다시 적용해도 side effect가 중복되지 않는지 명시합니다. */
    default boolean supportsIdempotentReplay() { return false; }

    /** 최신 desired snapshot 한 건만으로 version gap을 따라잡을 수 있는지 명시합니다. */
    default boolean snapshotCapable() { return true; }

    CpfRuntimeApplyResult apply(CpfRuntimeDelivery delivery);
}

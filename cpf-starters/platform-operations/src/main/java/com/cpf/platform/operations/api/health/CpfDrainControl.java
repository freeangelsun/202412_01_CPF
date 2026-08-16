package com.cpf.platform.operations.api.health;
import java.time.Duration;
/** 신규 요청 유입 차단과 in-flight drain을 제어하는 계약입니다. */
public interface CpfDrainControl {
    CpfDrainState state();
    long inFlight();
    boolean tryEnter();
    void leave();
    CpfDrainState beginDrain(Duration timeout);
    void resume();
}

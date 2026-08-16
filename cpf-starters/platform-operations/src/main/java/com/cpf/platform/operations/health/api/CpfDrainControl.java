package com.cpf.platform.operations.health.api;
import java.time.Duration;
/** CpfDrainControl 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfDrainControl {
 CpfDrainState state(); long inFlight(); boolean tryEnter(); void leave(); CpfDrainState beginDrain(Duration timeout); void resume();
}

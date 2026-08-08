package com.cpf.core.api.health;
import java.time.Duration;
public interface CpfDrainControl {
 CpfDrainState state(); long inFlight(); boolean tryEnter(); void leave(); CpfDrainState beginDrain(Duration timeout); void resume();
}
